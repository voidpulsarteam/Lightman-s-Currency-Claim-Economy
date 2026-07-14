package dev.voidpulsar.lc_claim_economy.handler;

import dev.voidpulsar.lc_claim_economy.config.LcClaimEconomyConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Optionally disables Lightman's Currency's Coin Mint block entirely
 * (server-wide), so a claim economy can't be bypassed by players just
 * minting their own money from raw materials. Controlled by
 * {@link LcClaimEconomyConfig.Server#disableCoinMint}.
 * <p>
 * Detects the Coin Mint by its registry ID ({@code lightmanscurrency:coin_mint})
 * rather than importing Lightman's Currency's block class directly. This is
 * deliberate: it keeps this handler correct even if the exact class name in
 * a given Lightman's Currency version differs from what's assumed here, and
 * it's independent of which claim backend (FTB or OP&C) is active.
 */
public class CoinMintDisableHandler {
    private static final ResourceLocation COIN_MINT_ID =
            ResourceLocation.fromNamespaceAndPath("lightmanscurrency", "coin_mint");

    @SubscribeEvent
    public void onRightClickCoinMint(PlayerInteractEvent.RightClickBlock event) {
        if (!LcClaimEconomyConfig.SERVER.disableCoinMint.get()) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!isCoinMint(event.getLevel().getBlockState(event.getPos()))) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        event.getEntity().displayClientMessage(
                Component.translatable("message.lc_claim_economy.coin_mint_disabled"),
                true
        );
    }

    @SubscribeEvent
    public void onPlaceCoinMint(BlockEvent.EntityPlaceEvent event) {
        if (!LcClaimEconomyConfig.SERVER.disableCoinMint.get()) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!isCoinMint(event.getPlacedBlock())) {
            return;
        }

        event.setCanceled(true);
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            player.displayClientMessage(Component.translatable("message.lc_claim_economy.coin_mint_disabled"), true);
        }
    }

    private static boolean isCoinMint(BlockState state) {
        return COIN_MINT_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }
}
