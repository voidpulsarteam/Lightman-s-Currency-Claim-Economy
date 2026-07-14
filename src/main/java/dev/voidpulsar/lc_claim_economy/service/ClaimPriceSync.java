package dev.voidpulsar.lc_claim_economy.service;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.voidpulsar.lc_claim_economy.bank.BankAccountHelper;
import dev.voidpulsar.lc_claim_economy.config.LcClaimEconomyConfig;
import dev.voidpulsar.lc_claim_economy.network.SyncClaimPricesPayload;
import dev.voidpulsar.lc_claim_economy.service.WarService;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClaimPriceSync {
    private ClaimPriceSync() {
    }

    public static void syncToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, createPayload(player));
    }

    public static SyncClaimPricesPayload createPayload(ServerPlayer player) {
        boolean balanceSynced = false;
        boolean balanceEmpty = true;
        String balanceText = "";
        int claimedChunks = 0;

        if (FTBTeamsAPI.api().isManagerLoaded()) {
            Team team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElse(null);
            if (team != null) {
                BankAccountHelper.ensurePartyAccountExists(player.server, team);
                IBankAccount account = BankAccountHelper.getAccountForPlayer(player.server, player);
                balanceSynced = true;
                balanceEmpty = account.getMoneyStorage().isEmpty();
                if (!balanceEmpty) {
                    String text = account.getMoneyStorage().getAllValueText().getString();
                    // Defensive cap: FriendlyByteBuf#writeUtf throws an EncoderException
                    // (which disconnects the player) if the string exceeds its max length.
                    // A normal formatted balance is at most a few dozen characters, so this
                    // never triggers in practice, but it stops a malformed/huge value from
                    // ever taking the connection down.
                    if (text.length() > 256) {
                        text = text.substring(0, 256);
                    }
                    balanceText = text;
                }
                if (FTBChunksAPI.api().isManagerLoaded()) {
                    claimedChunks = FTBChunksAPI.api().getManager().getOrCreateData(team).getClaimedChunks().size();
                }
            }
        }

        return new SyncClaimPricesPayload(
                LcClaimEconomyConfig.SERVER.claimPrice.get(),
                LcClaimEconomyConfig.SERVER.forceLoadUpkeepPrice.get(),
                LcClaimEconomyConfig.SERVER.upkeepPeriodMinutes.get(),
                LcClaimEconomyConfig.SERVER.freeChunks.get(),
                claimedChunks,
                balanceSynced,
                balanceEmpty,
                balanceText,
                LcClaimEconomyConfig.SERVER.mobGriefProtectionPrice.get(),
                LcClaimEconomyConfig.SERVER.explosionProtectionPrice.get(),
                LcClaimEconomyConfig.SERVER.pvpDisablePrice.get(),
                LcClaimEconomyConfig.SERVER.blockInteractProtectionPrice.get(),
                LcClaimEconomyConfig.SERVER.blockEditProtectionPrice.get(),
                LcClaimEconomyConfig.SERVER.entityInteractProtectionPrice.get(),
                ProtectionPricing.landChunkGroupSize(),
                WarService.isEnabled()
        );
    }
}
