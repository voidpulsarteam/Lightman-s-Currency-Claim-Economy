package dev.voidpulsar.lc_claim_economy;

import com.mojang.logging.LogUtils;
import dev.voidpulsar.lc_claim_economy.config.LcClaimEconomyConfig;
import dev.voidpulsar.lc_claim_economy.command.ClearWarsCommand;
import dev.voidpulsar.lc_claim_economy.command.SeedTestTeamsCommand;
import dev.voidpulsar.lc_claim_economy.command.UpkeepDetailsCommand;
import dev.voidpulsar.lc_claim_economy.command.UpkeepPriorityCommand;
import dev.voidpulsar.lc_claim_economy.handler.ChunkClaimHandler;
import dev.voidpulsar.lc_claim_economy.handler.ForceLoadHandler;
import dev.voidpulsar.lc_claim_economy.handler.TaxCollectorPlacementHandler;
import dev.voidpulsar.lc_claim_economy.handler.TeamLifecycleHandler;
import dev.voidpulsar.lc_claim_economy.handler.TeamPropertyHandler;
import dev.voidpulsar.lc_claim_economy.network.RequestClaimPricesPayload;
import dev.voidpulsar.lc_claim_economy.network.RequestChunkUserPermsPayload;
import dev.voidpulsar.lc_claim_economy.network.RequestLandChunksPayload;
import dev.voidpulsar.lc_claim_economy.network.RequestPendingStatePayload;
import dev.voidpulsar.lc_claim_economy.network.SyncClaimPricesPayload;
import dev.voidpulsar.lc_claim_economy.network.SyncChunkUserPermsPayload;
import dev.voidpulsar.lc_claim_economy.network.SyncLandChunksPayload;
import dev.voidpulsar.lc_claim_economy.network.SyncPendingStatePayload;
import dev.voidpulsar.lc_claim_economy.network.RequestWarStatePayload;
import dev.voidpulsar.lc_claim_economy.network.SetChunkUserPermsPayload;
import dev.voidpulsar.lc_claim_economy.network.SyncWarStatePayload;
import dev.voidpulsar.lc_claim_economy.network.ToggleChunkTypeBatchPayload;
import dev.voidpulsar.lc_claim_economy.network.ToggleChunkTypePayload;
import dev.voidpulsar.lc_claim_economy.network.ToggleWarPayload;
import dev.voidpulsar.lc_claim_economy.client.ClientPendingRefreshHandler;
import dev.voidpulsar.lc_claim_economy.service.UpkeepService;
import dev.voidpulsar.lc_claim_economy.teams.LandProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(LcClaimEconomy.MOD_ID)
public class LcClaimEconomy {
    public static final String MOD_ID = "lc_claim_economy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LcClaimEconomy(IEventBus modEventBus, ModContainer modContainer) {
        ModCompatibility.validateOrThrow();

        modContainer.registerConfig(ModConfig.Type.SERVER, LcClaimEconomyConfig.SERVER_SPEC);

        modEventBus.addListener(this::registerPayloads);

        LandProperties.register();

        if (dev.voidpulsar.lc_claim_economy.compat.ModCompat.isFtbAvailable()) {
            NeoForge.EVENT_BUS.register(new UpkeepService());
            NeoForge.EVENT_BUS.register(new TeamLifecycleHandler());
            NeoForge.EVENT_BUS.register(new TaxCollectorPlacementHandler());

            new ChunkClaimHandler();
            new TeamPropertyHandler();
            new ForceLoadHandler();

            NeoForge.EVENT_BUS.addListener(UpkeepDetailsCommand::register);
            NeoForge.EVENT_BUS.addListener(UpkeepPriorityCommand::register);
            NeoForge.EVENT_BUS.addListener(SeedTestTeamsCommand::register);
        } else {
            LOGGER.info("FTB Chunks/Teams not detected - FTB Chunks integration disabled.");
        }

        if (dev.voidpulsar.lc_claim_economy.compat.ModCompat.isOpcAvailable()) {
            NeoForge.EVENT_BUS.register(new dev.voidpulsar.lc_claim_economy.opc.OpcIntegration());
            LOGGER.info("Open Parties and Claims detected - OP&C claim economy integration enabled.");
        }

        NeoForge.EVENT_BUS.addListener(ClearWarsCommand::register);
        NeoForge.EVENT_BUS.register(new dev.voidpulsar.lc_claim_economy.handler.CoinMintDisableHandler());

        if (FMLEnvironment.dist == Dist.CLIENT && dev.voidpulsar.lc_claim_economy.compat.ModCompat.isFtbAvailable()) {
            new ClientPendingRefreshHandler();
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar.playToClient(
                SyncClaimPricesPayload.TYPE,
                SyncClaimPricesPayload.STREAM_CODEC,
                SyncClaimPricesPayload::handleClient
        );
        registrar.playToClient(
                SyncPendingStatePayload.TYPE,
                SyncPendingStatePayload.STREAM_CODEC,
                SyncPendingStatePayload::handleClient
        );
        registrar.playToClient(
                SyncLandChunksPayload.TYPE,
                SyncLandChunksPayload.STREAM_CODEC,
                SyncLandChunksPayload::handleClient
        );
        registrar.playToClient(
                SyncWarStatePayload.TYPE,
                SyncWarStatePayload.STREAM_CODEC,
                SyncWarStatePayload::handleClient
        );
        registrar.playToClient(
                SyncChunkUserPermsPayload.TYPE,
                SyncChunkUserPermsPayload.STREAM_CODEC,
                SyncChunkUserPermsPayload::handleClient
        );
        registrar.playToServer(
                RequestClaimPricesPayload.TYPE,
                RequestClaimPricesPayload.STREAM_CODEC,
                RequestClaimPricesPayload::handleServer
        );
        registrar.playToServer(
                RequestPendingStatePayload.TYPE,
                RequestPendingStatePayload.STREAM_CODEC,
                RequestPendingStatePayload::handleServer
        );
        registrar.playToServer(
                RequestLandChunksPayload.TYPE,
                RequestLandChunksPayload.STREAM_CODEC,
                RequestLandChunksPayload::handleServer
        );
        registrar.playToServer(
                RequestChunkUserPermsPayload.TYPE,
                RequestChunkUserPermsPayload.STREAM_CODEC,
                RequestChunkUserPermsPayload::handleServer
        );
        registrar.playToServer(
                SetChunkUserPermsPayload.TYPE,
                SetChunkUserPermsPayload.STREAM_CODEC,
                SetChunkUserPermsPayload::handleServer
        );
        registrar.playToServer(
                ToggleChunkTypePayload.TYPE,
                ToggleChunkTypePayload.STREAM_CODEC,
                ToggleChunkTypePayload::handleServer
        );
        registrar.playToServer(
                ToggleChunkTypeBatchPayload.TYPE,
                ToggleChunkTypeBatchPayload.STREAM_CODEC,
                ToggleChunkTypeBatchPayload::handleServer
        );
        registrar.playToServer(
                RequestWarStatePayload.TYPE,
                RequestWarStatePayload.STREAM_CODEC,
                RequestWarStatePayload::handleServer
        );
        registrar.playToServer(
                ToggleWarPayload.TYPE,
                ToggleWarPayload.STREAM_CODEC,
                ToggleWarPayload::handleServer
        );
    }
}
