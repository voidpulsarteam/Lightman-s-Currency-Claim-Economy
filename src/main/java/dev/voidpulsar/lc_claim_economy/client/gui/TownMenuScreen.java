package dev.voidpulsar.lc_claim_economy.client.gui;

import dev.voidpulsar.lc_claim_economy.client.TownMenuState;
import dev.voidpulsar.lc_claim_economy.network.TownMenuActionPayload;
import dev.voidpulsar.lc_claim_economy.network.TownMenuEntry;
import dev.voidpulsar.lc_claim_economy.network.TownResidentEntry;
import dev.voidpulsar.lc_claim_economy.network.RequestTownMenuPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TownMenuScreen extends Screen {
    private static final int PAD = 16;
    private static final int SIDEBAR_WIDTH = 108;
    private static final int CONTENT_LEFT = PAD + SIDEBAR_WIDTH + 12;
    private static final int ROW_HEIGHT = 22;

    public enum Section {
        OVERVIEW,
        TOWNS,
        RESIDENTS,
        BANK
    }

    private Section section;
    private EditBox townNameBox;
    private EditBox residentNameBox;
    private EditBox amountBox;
    private static final ItemStack OVERVIEW_ICON = new ItemStack(Items.FILLED_MAP);
    private static final ItemStack TOWNS_ICON = new ItemStack(Items.BELL);
    private static final ItemStack RESIDENTS_ICON = new ItemStack(Items.PLAYER_HEAD);
    private static final ItemStack BANK_ICON = new ItemStack(Items.CHEST);

    public TownMenuScreen() {
        this(Section.OVERVIEW);
    }

    public TownMenuScreen(Section section) {
        super(Component.translatable("gui.lc_claim_economy.town_menu.title"));
        this.section = section;
    }

    public Section section() {
        return section;
    }

    @Override
    protected void init() {
        addSidebarButton(Section.OVERVIEW, 36, "gui.lc_claim_economy.town_menu.section_overview");
        addSidebarButton(Section.TOWNS, 60, "gui.lc_claim_economy.town_menu.section_towns");
        addSidebarButton(Section.RESIDENTS, 84, "gui.lc_claim_economy.town_menu.section_residents");
        addSidebarButton(Section.BANK, 108, "gui.lc_claim_economy.town_menu.section_bank");
        addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.refresh"), button -> PacketDistributor.sendToServer(new RequestTownMenuPayload()))
                .bounds(PAD, height - 38, SIDEBAR_WIDTH, 20)
                .build());

        switch (section) {
            case OVERVIEW -> buildOverviewPage();
            case TOWNS -> buildTownsPage();
            case RESIDENTS -> buildResidentsPage();
            case BANK -> buildBankPage();
        }
    }

    private void addSidebarButton(Section targetSection, int y, String key) {
        addRenderableWidget(Button.builder(
                Component.translatable(key),
                button -> minecraft.setScreen(new TownMenuScreen(targetSection))
        ).bounds(PAD, y, SIDEBAR_WIDTH, 20).build());
    }

    private void buildOverviewPage() {
        int headerWidth = width - CONTENT_LEFT - PAD;
        TownMenuEntry currentTown = TownMenuState.currentPlayerTown();
        if (TownMenuState.playerTownId() == null) {
            if (TownMenuState.canCreateTown()) {
                townNameBox = new EditBox(font, CONTENT_LEFT + 10, 74, Math.max(200, headerWidth - 108), 20, Component.empty());
                townNameBox.setMaxLength(32);
                townNameBox.setHint(Component.translatable("gui.lc_claim_economy.town_menu.create_hint"));
                addRenderableWidget(townNameBox);
                addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.create"), button -> createTown())
                        .bounds(width - PAD - 96, 74, 96, 20).build());
            }
            return;
        }

        if (currentTown != null) {
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.treasury", currentTown.treasuryCopper()), button -> {})
                    .bounds(CONTENT_LEFT, 74, headerWidth, 20)
                    .build());
        }

        townNameBox = new EditBox(font, CONTENT_LEFT, 102, Math.max(180, headerWidth - 92), 20, Component.empty());
        townNameBox.setMaxLength(32);
        townNameBox.setHint(Component.translatable("gui.lc_claim_economy.town_menu.create_hint"));
        addRenderableWidget(townNameBox);
        addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.create"), button -> createTown())
                .bounds(width - PAD - 96, 102, 96, 20).build());

        if (currentTown != null) {
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.open_bank"), button -> minecraft.setScreen(new TownMenuScreen(Section.BANK)))
                    .bounds(CONTENT_LEFT, 130, 96, 20)
                    .build());
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.section_residents"), button -> minecraft.setScreen(new TownMenuScreen(Section.RESIDENTS)))
                    .bounds(CONTENT_LEFT + 100, 130, 116, 20)
                    .build());
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.section_towns"), button -> minecraft.setScreen(new TownMenuScreen(Section.TOWNS)))
                    .bounds(CONTENT_LEFT + 220, 130, 96, 20)
                    .build());
        }
    }

    private void buildTownsPage() {
        int y = 72;
        for (TownMenuEntry town : TownMenuState.towns()) {
            addTownRow(town, y);
            y += 28;
        }
    }

    private void buildResidentsPage() {
        TownMenuEntry currentTown = TownMenuState.currentPlayerTown();
        if (currentTown == null) {
            return;
        }

        residentNameBox = new EditBox(font, CONTENT_LEFT, 74, Math.max(180, width - CONTENT_LEFT - PAD - 160), 20, Component.empty());
        residentNameBox.setMaxLength(48);
        residentNameBox.setHint(Component.translatable("gui.lc_claim_economy.town_menu.invite_hint"));
        addRenderableWidget(residentNameBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.invite"), button -> {
            String value = residentNameBox.getValue().trim();
            if (!value.isEmpty()) {
                PacketDistributor.sendToServer(new TownMenuActionPayload(
                        TownMenuActionPayload.Action.INVITE_PLAYER,
                        TownMenuState.playerTownId(),
                        "",
                        value,
                        ""
                ));
            }
        }).bounds(width - PAD - 140, 74, 140, 20).build());

        int rowY = 108;
        for (TownResidentEntry resident : TownMenuState.residents()) {
            addResidentRow(resident, rowY);
            rowY += 26;
        }
    }

    private void buildBankPage() {
        TownMenuEntry currentTown = TownMenuState.currentPlayerTown();
        if (currentTown == null) {
            return;
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.treasury", currentTown.treasuryCopper()), button -> {})
                .bounds(CONTENT_LEFT, 74, width - CONTENT_LEFT - PAD, 20)
                .build());

        amountBox = new EditBox(font, CONTENT_LEFT, 106, Math.max(180, width - CONTENT_LEFT - PAD - 210), 20, Component.empty());
        amountBox.setMaxLength(12);
        amountBox.setHint(Component.translatable("gui.lc_claim_economy.town_bank.amount_hint"));
        addRenderableWidget(amountBox);

        if (currentTown.canDepositTreasury()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_bank.deposit"), button -> sendBank(TownMenuActionPayload.Action.DEPOSIT_TREASURY))
                    .bounds(width - PAD - 102, 106, 102, 20)
                    .build());
        }
        if (currentTown.canWithdrawTreasury()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_bank.withdraw"), button -> sendBank(TownMenuActionPayload.Action.WITHDRAW_TREASURY))
                    .bounds(width - PAD - 102, 130, 102, 20)
                    .build());
        }

        int ledgerY = 162;
        addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_bank.ledger"), button -> {})
                .bounds(CONTENT_LEFT, ledgerY - 16, 160, 20)
                .build());

        for (TownMenuEntry entry : TownMenuState.towns()) {
            if (!entry.playerTown()) {
                continue;
            }
            ledgerY = renderLedger(entry, ledgerY);
            break;
        }
    }

    private int renderLedger(TownMenuEntry currentTown, int ledgerY) {
        if (currentTown.bankLedger().isEmpty()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_bank.ledger_empty"), button -> {})
                    .bounds(CONTENT_LEFT, ledgerY, width - CONTENT_LEFT - PAD, 20)
                    .build());
            return ledgerY + 24;
        }

        for (var transaction : currentTown.bankLedger()) {
            String amount = (transaction.amountCopper() >= 0 ? "+" : "") + transaction.amountCopper() + " copper";
            addRenderableWidget(Button.builder(Component.literal(transaction.action() + " · " + transaction.actorName() + " · " + amount), button -> {})
                    .bounds(CONTENT_LEFT, ledgerY, width - CONTENT_LEFT - PAD, 20)
                    .build());
            ledgerY += 22;
        }
        return ledgerY;
    }

    private void addTownRow(TownMenuEntry town, int y) {
        addRenderableWidget(Button.builder(
                Component.literal(town.name() + " · " + town.mayorName()),
                button -> {}
        ).bounds(CONTENT_LEFT, y, Math.max(240, width - CONTENT_LEFT - PAD), 20).build());

        boolean currentTownOwnsPlot = town.townId().equals(TownMenuState.currentPlotTownId());
        boolean wilderness = TownMenuState.currentPlotTownId() == null;

        addRenderableWidget(Button.builder(
                Component.translatable(town.publicAccess() ? "gui.lc_claim_economy.town_menu.public" : "gui.lc_claim_economy.town_menu.private"),
                button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                        TownMenuActionPayload.Action.TOGGLE_PUBLIC_ACCESS,
                        town.townId(),
                        "",
                        "",
                        ""
                ))
        ).bounds(width - PAD - 236, y, 72, 20).build());

        String plotActionLabel = currentTownOwnsPlot
                ? "gui.lc_claim_economy.town_menu.unclaim"
                : (wilderness ? "gui.lc_claim_economy.town_menu.claim" : null);
        addRenderableWidget(Button.builder(
                currentTownOwnsPlot || wilderness
                        ? Component.translatable(plotActionLabel)
                        : Component.literal(TownMenuState.currentPlotTownName()),
                button -> {
                    if (currentTownOwnsPlot) {
                        PacketDistributor.sendToServer(new TownMenuActionPayload(
                                TownMenuActionPayload.Action.UNCLAIM_CURRENT_PLOT,
                                town.townId(),
                                "",
                                "",
                                ""
                        ));
                    } else if (wilderness) {
                        PacketDistributor.sendToServer(new TownMenuActionPayload(
                                TownMenuActionPayload.Action.CLAIM_CURRENT_PLOT,
                                town.townId(),
                                "",
                                "",
                                ""
                        ));
                    }
                }
        ).bounds(width - PAD - 152, y, 112, 20).build());

        if (town.invited() && !town.playerTown()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.join"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.ACCEPT_INVITE,
                    town.townId(),
                    "",
                    "",
                    ""
            ))).bounds(width - PAD - 62, y, 28, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.lc_claim_economy.town_menu.decline"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.DECLINE_INVITE,
                    town.townId(),
                    "",
                    "",
                    ""
            ))).bounds(width - PAD - 30, y, 20, 20).build());
        }
    }

    private void addResidentRow(TownResidentEntry resident, int y) {
        addRenderableWidget(Button.builder(
                Component.literal(resident.playerName() + " • " + resident.rank()),
                button -> {}
        ).bounds(CONTENT_LEFT, y, Math.max(220, width - CONTENT_LEFT - PAD), 20).build());

        if (!"MAYOR".equals(resident.rank())) {
            addRenderableWidget(Button.builder(
                    Component.literal(resident.trustedOnCurrentPlot() ? "Untrust" : "Trust"),
                    button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                            resident.trustedOnCurrentPlot()
                                    ? TownMenuActionPayload.Action.UNTRUST_CURRENT_PLOT
                                    : TownMenuActionPayload.Action.TRUST_CURRENT_PLOT,
                            TownMenuState.playerTownId(),
                            "",
                            resident.playerId().toString(),
                            ""
                    ))
            ).bounds(width - PAD - 198, y, 44, 20).build());

            addRenderableWidget(Button.builder(
                Component.literal(resident.deniedOnCurrentPlot() ? "Undeny" : "Deny"),
                button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    resident.deniedOnCurrentPlot()
                        ? TownMenuActionPayload.Action.UNDENY_CURRENT_PLOT
                        : TownMenuActionPayload.Action.DENY_CURRENT_PLOT,
                    TownMenuState.playerTownId(),
                    "",
                    resident.playerId().toString(),
                    ""
                ))
            ).bounds(width - PAD - 150, y, 44, 20).build());

            addRenderableWidget(Button.builder(Component.literal("+"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.SET_RESIDENT_RANK,
                    TownMenuState.playerTownId(),
                    "",
                resident.playerId().toString(),
                "ASSISTANT"
            ))).bounds(width - PAD - 102, y, 20, 20).build());

            addRenderableWidget(Button.builder(Component.literal("-"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.SET_RESIDENT_RANK,
                    TownMenuState.playerTownId(),
                    "",
                resident.playerId().toString(),
                "RESIDENT"
            ))).bounds(width - PAD - 78, y, 20, 20).build());

            addRenderableWidget(Button.builder(Component.literal("X"), button -> PacketDistributor.sendToServer(new TownMenuActionPayload(
                    TownMenuActionPayload.Action.REMOVE_RESIDENT,
                    TownMenuState.playerTownId(),
                    "",
                    resident.playerId().toString(),
                    ""
            ))).bounds(width - PAD - 54, y, 20, 20).build());
        }
    }

    private void createTown() {
        if (townNameBox == null) {
            return;
        }
        String name = townNameBox.getValue().trim();
        if (!name.isEmpty()) {
            PacketDistributor.sendToServer(new TownMenuActionPayload(TownMenuActionPayload.Action.CREATE_TOWN, null, name, "", ""));
        }
    }

    private void sendBank(TownMenuActionPayload.Action action) {
        if (amountBox == null || TownMenuState.playerTownId() == null) {
            return;
        }
        String amount = amountBox.getValue().trim();
        if (amount.isEmpty()) {
            return;
        }
        PacketDistributor.sendToServer(new TownMenuActionPayload(action, TownMenuState.playerTownId(), amount, "", ""));
    }

    private void drawHeaderBar(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xC0121622);
        graphics.fill(x, y, x + w, y + 1, 0xFF4C5D85);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF0A0D12);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);

        graphics.fill(PAD - 2, 18, PAD + SIDEBAR_WIDTH + 2, height - 18, 0xC0181A20);
        graphics.fill(CONTENT_LEFT - 2, 18, width - PAD + 2, height - 18, 0xC0181A20);
        graphics.drawString(font, Component.translatable("gui.lc_claim_economy.town_menu.sidebar_title"), PAD, 24, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("gui.lc_claim_economy.town_menu.current_chunk", TownMenuState.currentChunkKey()), CONTENT_LEFT, 24, 0xD0D0D0);
        graphics.drawString(font, Component.translatable("gui.lc_claim_economy.town_menu.current_plot", TownMenuState.currentPlotTownName()), CONTENT_LEFT, 36, 0xD0D0D0);

        drawSidebarIcon(graphics, OVERVIEW_ICON, 20, 38, section == Section.OVERVIEW);
        drawSidebarIcon(graphics, TOWNS_ICON, 20, 62, section == Section.TOWNS);
        drawSidebarIcon(graphics, RESIDENTS_ICON, 20, 86, section == Section.RESIDENTS);
        drawSidebarIcon(graphics, BANK_ICON, 20, 110, section == Section.BANK);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawSidebarIcon(GuiGraphics graphics, ItemStack icon, int x, int y, boolean active) {
        if (active) {
            graphics.fill(PAD + 1, y - 2, PAD + SIDEBAR_WIDTH - 1, y + 18, 0x551F2A44);
        }
        graphics.renderItem(icon, PAD + 4, y - 1);
    }
}
