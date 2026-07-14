package dev.voidpulsar.lc_claim_economy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.voidpulsar.lc_claim_economy.LcClaimEconomy;
import dev.voidpulsar.lc_claim_economy.bank.BankAccountHelper;
import dev.voidpulsar.lc_claim_economy.service.ProtectionPriceDisplay;
import dev.voidpulsar.lc_claim_economy.service.UpkeepPriorityService;
import dev.voidpulsar.lc_claim_economy.util.MoneyMessageUtil;
import dev.voidpulsar.lc_claim_economy.util.MoneyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class UpkeepPriorityCommand {
    private UpkeepPriorityCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(LcClaimEconomy.MOD_ID)
                .then(Commands.literal("upkeep_priority")
                        .executes(UpkeepPriorityCommand::showPriority)));
    }

    private static int showPriority(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null || !FTBTeamsAPI.api().isManagerLoaded()) {
            return 0;
        }

        Team team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElse(null);
        if (team == null) {
            return 0;
        }

        if (team.isPartyTeam() && !BankAccountHelper.canPurchaseForTeam(team, player.getUUID())) {
            player.displayClientMessage(
                    Component.translatable("message.lc_claim_economy.upkeep_priority.denied"),
                    false
            );
            return 0;
        }

        var entries = UpkeepPriorityService.buildOrder(source.getServer(), team);
        if (entries.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.lc_claim_economy.upkeep_priority.empty"),
                    false
            );
            return 1;
        }

        Component period = ProtectionPriceDisplay.upkeepPeriodLabel();
        MutableComponent message = Component.translatable("message.lc_claim_economy.upkeep_priority.header")
                .withStyle(ChatFormatting.YELLOW);
        message.append("\n");
        message.append(Component.translatable("message.lc_claim_economy.upkeep_priority.legend")
                .withStyle(ChatFormatting.GRAY));
        message.append("\n");

        for (UpkeepPriorityService.PriorityEntry entry : entries) {
            Component cost = MoneyMessageUtil.formatValue(MoneyUtil.fromCopper(entry.costCopper()));
            String kindKey = entry.kind() == UpkeepPriorityService.EntryKind.PROTECTION
                    ? "message.lc_claim_economy.upkeep_priority.kind.protection"
                    : "message.lc_claim_economy.upkeep_priority.kind.war";
            message.append(Component.translatable(
                    "message.lc_claim_economy.upkeep_priority.line",
                    entry.priority(),
                    entry.label(),
                    Component.translatable(kindKey),
                    cost,
                    period
            ));
            message.append("\n");
        }

        player.displayClientMessage(message, false);
        return 1;
    }
}
