package dev.voidpulsar.lc_claim_economy.opc;

import dev.voidpulsar.lc_claim_economy.LcClaimEconomy;
import dev.voidpulsar.lc_claim_economy.teams.OpcPartySyncService;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import xaero.pac.common.event.api.OPACServerAddonRegisterEvent;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

import java.util.stream.Stream;

/**
 * OP&C-side entry point. Only ever constructed and registered when
 * {@link dev.voidpulsar.lc_claim_economy.compat.ModCompat#isOpcAvailable()}
 * is true - see the guard in {@code LcClaimEconomy}'s constructor. This
 * class's method signatures reference OP&C types directly, so it must never
 * be classloaded when OP&C isn't installed.
 */
public final class OpcIntegration {
    private final OpcClaimEconomyListener claimListener = new OpcClaimEconomyListener();
    private long nextPartySyncTick = -1L;

    public OpcIntegration() {
    }

    @SubscribeEvent
    public void onAddonRegister(OPACServerAddonRegisterEvent event) {
        event.getClaimsManagerTrackerAPI().register(claimListener);
        LcClaimEconomy.LOGGER.info("Registered OP&C claim economy listener");
    }

    /**
     * OP&C does not expose fine-grained party create/join/leave events in
     * its public API, so party <-> LC team bank sync is reconciled on a
     * periodic tick instead, reusing a similar cadence to the FTB upkeep
     * tick.
     * <p>
     * Note: this does not seed {@link OpcClaimEconomyListener}'s
     * known-owner map from OP&C's existing claim state at startup, since
     * OP&C's public API doesn't expose a confirmed way to enumerate all
     * currently-claimed chunks in a dimension (only single-chunk lookups by
     * position). In practice this means claims that already existed before
     * this mod was added are not charged/refunded until something about
     * them changes (a genuinely new claim, unclaim, forceload toggle,
     * etc.), which is a safe default - it just means pre-existing claims
     * are grandfathered in rather than retroactively charged.
     */
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long periodTicks = 20L * 60L; // once a minute; party membership changes aren't time-sensitive

        if (nextPartySyncTick < 0L) {
            nextPartySyncTick = server.getTickCount() + periodTicks;
            return;
        }
        if (server.getTickCount() < nextPartySyncTick) {
            return;
        }
        nextPartySyncTick = server.getTickCount() + periodTicks;

        if (server.getPlayerList().getPlayerCount() <= 0) {
            return;
        }

        reconcileParties(server);
    }

    private void reconcileParties(MinecraftServer server) {
        IPartyManagerAPI partyManager = OpenPACServerAPI.get(server).getPartyManager();
        try (Stream<IServerPartyAPI> parties = partyManager.getAllStream()) {
            parties.forEach(party -> {
                try {
                    OpcPartySyncService.ensureLinked(server, party);
                } catch (Exception e) {
                    LcClaimEconomy.LOGGER.error("Failed to sync OP&C party {}", party.getId(), e);
                }
            });
        }
    }
}
