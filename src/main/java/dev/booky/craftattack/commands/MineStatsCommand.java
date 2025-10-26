package dev.booky.craftattack.commands;
// Created by booky10 in CraftAttack (15:36 29.10.23)

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.booky.craftattack.utils.LeaderboardUtil;
import dev.booky.craftattack.utils.LeaderboardUtil.LeaderboardEntry;
import dev.booky.craftattack.utils.LeaderboardUtil.LeaderboardResult;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerProfileArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class MineStatsCommand extends CommandAPICommand implements PlayerCommandExecutor {

    private static final int LEADERBOARD_COUNT = 10;

    private final String objectiveName;

    public MineStatsCommand(CaConfig.MineStatEntry entry) {
        this(entry.getObjectiveName());
    }

    public MineStatsCommand(String objectiveName) {
        super(objectiveName);
        this.objectiveName = objectiveName;

        this.withOptionalArguments(new PlayerProfileArgument("target"));

        this.withPermission("craftattack.command.minestats." + objectiveName);
        this.executesPlayer(this);
    }

    @Override
    public void run(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(this.objectiveName);
        if (objective == null) {
            sender.sendMessage(CaManager.getPrefix().append(translatable(
                    "ca.command.mine-stats.internal-error", NamedTextColor.RED)));
            return;
        }

        // if there was an explicit target specified, look up only itself
        Optional<List<PlayerProfile>> targets = args.<List<PlayerProfile>>getOptionalUnchecked("target")
                .filter(list -> !list.isEmpty());
        if (targets.isPresent()) {
            for (PlayerProfile target : targets.get()) {
                String name = Objects.requireNonNull(target.getName());
                int score = objective.getScore(name).getScore();
                sender.sendMessage(CaManager.getPrefix().append(translatable(
                        "ca.command.mine-stats.value", NamedTextColor.GREEN,
                        text(this.objectiveName), text(score))));
            }
            return;
        }

        LeaderboardResult leaderboard = LeaderboardUtil.buildLeaderboard(
                objective, LEADERBOARD_COUNT, sender.getName());

        sender.sendMessage(CaManager.getPrefix().append(translatable(
                "ca.command.mine-stats.leaderboard.header", NamedTextColor.GREEN,
                text(this.objectiveName),
                text(leaderboard.entries().size()),
                text(leaderboard.average()))));
        for (LeaderboardEntry entry : leaderboard.entries()) {
            sender.sendMessage(CaManager.getPrefix().append(entry.buildLine()));
        }
    }
}
