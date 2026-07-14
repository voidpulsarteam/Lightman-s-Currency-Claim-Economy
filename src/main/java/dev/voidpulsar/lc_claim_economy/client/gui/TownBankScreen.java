package dev.voidpulsar.lc_claim_economy.client.gui;

import dev.voidpulsar.lc_claim_economy.client.TownMenuState;
import dev.voidpulsar.lc_claim_economy.network.TownMenuActionPayload;
import dev.voidpulsar.lc_claim_economy.network.TownMenuEntry;
import dev.voidpulsar.lc_claim_economy.town.TownBankTransaction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TownBankScreen extends Screen {
    private static final int PAD = 16;
    private static final int PANEL = 10;
    private static final ItemStack OVERVIEW_ICON = new ItemStack(Items.FILLED_MAP);
    private static final ItemStack TOWNS_ICON = new ItemStack(Items.BELL);
    private static final ItemStack RESIDENTS_ICON = new ItemStack(Items.PLAYER_HEAD);
    private static final ItemStack BANK_ICON = new ItemStack(Items.CHEST);

    private EditBox amountBox;

    public TownBankScreen() {
        super(Component.translatable("gui.lc_claim_economy.town_bank.title"));
    }

    @Override
    protected void init() {
        TownMenuEntry currentTown = TownMenuState.currentPlayerTown();
        if (currentTown == null) {
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_bank.no_town"), button -> {})
                    .bounds(PAD, 40, 200, 20)
                    .build());
            return;
        }

        amountBox = new EditBox(font, PAD, 48, Math.max(180, width - PAD * 2 - 196), 20, Component.empty());
        amountBox.setMaxLength(12);
        amountBox.setHint(Component.translatable("gui.lc_claim_economy.town_bank.amount_hint"));
        addRenderableWidget(amountBox);

        if (currentTown.canDepositTreasury()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_bank.deposit"), button -> send(TownMenuActionPayload.Action.DEPOSIT_TREASURY))
                    .bounds(width - PAD - 96, 48, 96, 20)
                    .build());
        }
        if (currentTown.canWithdrawTreasury()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_bank.withdraw"), button -> send(TownMenuActionPayload.Action.WITHDRAW_TREASURY))
                    .bounds(width - PAD - 96, 72, 96, 20)
                    .build());
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_bank.back"), button -> minecraft.setScreen(new TownMenuScreen()))
                .bounds(PAD, height - 32, 80, 20)
                .build());
    }

    private void send(TownMenuActionPayload.Action action) {
        if (amountBox == null || TownMenuState.playerTownId() == null) {
            return;
        }
        String amount = amountBox.getValue().trim();
        if (amount.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(new TownMenuActionPayload(action, TownMenuState.playerTownId(), amount, "", ""));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        drawPanel(graphics, PAD, 18, width - PAD * 2, 54);
        drawPanel(graphics, PAD, 82, width - PAD * 2, height - 98);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        drawSidebarIcons(graphics);

        TownMenuEntry currentTown = TownMenuState.currentPlayerTown();
        if (currentTown == null) {
            graphics.drawString(font, Component.translatable("gui.lc_claim_economy.town_bank.no_town"), PAD + 8, 34, 0xA0A0A0);
            super.render(graphics, mouseX, mouseY, delta);
            return;
        }

        graphics.drawString(font, Component.literal("Town: " + currentTown.name()), PAD + 8, 30, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("gui.lc_claim_economy.town_bank.balance", currentTown.treasuryCopper()), PAD + 8, 42, 0xD0D0D0);
        graphics.drawString(font, Component.translatable("gui.lc_claim_economy.town_bank.instructions"), PAD + 8, 60, 0xA0A0A0);

        int ledgerY = 104;
        graphics.drawString(font, Component.translatable("gui.lc_claim_economy.town_bank.ledger"), PAD + 8, ledgerY, 0xFFFFFF);
        ledgerY += 14;
        if (currentTown.bankLedger().isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.lc_claim_economy.town_bank.ledger_empty"), PAD + 8, ledgerY, 0xA0A0A0);
        } else {
            for (TownBankTransaction transaction : currentTown.bankLedger()) {
                String sign = transaction.amountCopper() >= 0 ? "+" : "";
                int rowColor = transaction.amountCopper() >= 0 ? 0xC7EFC9 : 0xF7C3B7;
                graphics.fill(PAD + 6, ledgerY - 2, width - PAD - 6, ledgerY + 10, transaction.amountCopper() >= 0 ? 0x22104A1E : 0x223B1515);
                graphics.drawString(font, Component.literal(transaction.action() + " · " + transaction.actorName()), PAD + 12, ledgerY, 0xFFFFFF);
                graphics.drawString(font, Component.literal(sign + transaction.amountCopper() + " copper"), width - PAD - 12 - font.width(sign + transaction.amountCopper() + " copper"), ledgerY, rowColor);
                ledgerY += 12;
            }
        }
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawSidebarIcons(GuiGraphics graphics) {
        graphics.renderItem(OVERVIEW_ICON, PAD + 4, 38);
        graphics.renderItem(TOWNS_ICON, PAD + 4, 62);
        graphics.renderItem(RESIDENTS_ICON, PAD + 4, 86);
        graphics.renderItem(BANK_ICON, PAD + 4, 110);
    }

    private void drawPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xB0181A20);
        graphics.fill(x, y, x + w, y + 1, 0xFF3E4B6B);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF111827);
        graphics.fill(x, y, x + 1, y + h, 0xFF111827);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFF111827);
    }
}