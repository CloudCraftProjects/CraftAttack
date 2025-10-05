package dev.booky.craftattack.commands;
// Created by booky10 in CraftAttack (15:36 29.10.23)

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.booky.craftattack.CaManager;
import dev.booky.craftattack.utils.CaConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerProfileArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    private static LeaderboardEntry calcPlacedEntry(UnplacedLeaderboardEntry entry, LeaderboardEntry lastEntry) {
        // calculate placement position
        int placement;
        if (lastEntry == null) {
            placement = 1;
        } else if (entry.score() == lastEntry.score()) {
            // don't move one place further if still the same score
            placement = lastEntry.place();
        } else {
            placement = lastEntry.place() + 1;
        }

        return entry.withPlacement(placement);
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

        // request all entries from scoreboard objective
        Set<String> entryNames = scoreboard.getEntries();
        Object2IntMap<OfflinePlayer> entries = new Object2IntArrayMap<>(entryNames.size());
        for (String entryName : entryNames) {
            OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(entryName);
            if (target == null) {
                try {
                    UUID.fromString(entryName);
                    continue; // skip looking for player, is an entity
                } catch (IllegalArgumentException ignored) {
                    target = Bukkit.getOfflinePlayer(entryName);
                }
            }

            if (!target.hasPlayedBefore()) {
                continue; // unknown player
            }

            Score score = objective.getScore(target);
            if (score.getScore() > 0) {
                entries.put(target, score.getScore());
            }
        }

        // extract self entry
        UnplacedLeaderboardEntry unplacedSelfEntry = new UnplacedLeaderboardEntry(
                sender, entries.getInt(sender));

        // build sorted entries from raw entries
        List<UnplacedLeaderboardEntry> unplacedEntries = entries.object2IntEntrySet().stream()
                .sorted(Comparator.<Object2IntMap.Entry<OfflinePlayer>>comparingInt(Object2IntMap.Entry::getIntValue).reversed())
                .map(entry -> new UnplacedLeaderboardEntry(entry.getKey(), entry.getIntValue()))
                .toList();
        int avgScore = unplacedEntries.stream()
                .mapToInt(UnplacedLeaderboardEntry::score).average() // calc average
                .stream().mapToInt(NumberConversions::round).findAny() // round to int
                .orElse(0);

        // add placement data to entries
        List<LeaderboardEntry> leaderboard = new ArrayList<>(LEADERBOARD_COUNT);
        LeaderboardEntry lastEntry = null;
        LeaderboardEntry selfEntry = null;
        for (UnplacedLeaderboardEntry entry : unplacedEntries) {
            LeaderboardEntry placedEntry = calcPlacedEntry(entry, lastEntry);
            // add to leaderboard if not already full
            if (leaderboard.size() < LEADERBOARD_COUNT) {
                leaderboard.add(placedEntry);
            }

            lastEntry = placedEntry;
            if (entry.equals(unplacedSelfEntry)) {
                selfEntry = placedEntry;
            }

            if (leaderboard.size() >= LEADERBOARD_COUNT && selfEntry != null) {
                break; // early cancel, all necessary data has already been calculated
            }
        }

        // fallback self entry if not on leaderboard
        if (selfEntry == null) {
            int placement = lastEntry == null ? 1 : lastEntry.place() + 1;
            selfEntry = unplacedSelfEntry.withPlacement(placement);
        }

        // finally, print results to command executor
        sender.sendMessage(CaManager.getPrefix().append(translatable(
                "ca.command.mine-stats.leaderboard.header", NamedTextColor.GREEN,
                text(this.objectiveName), text(entries.size()), text(avgScore))));
        for (LeaderboardEntry entry : leaderboard) {
            sender.sendMessage(CaManager.getPrefix().append(entry.buildMessage()));
        }
        // show own place for players which aren't in the top 10
        if (!leaderboard.contains(selfEntry)) {
            sender.sendMessage(CaManager.getPrefix().append(selfEntry.buildMessage()));
        }
    }

    private record UnplacedLeaderboardEntry(OfflinePlayer player, int score) {

        public LeaderboardEntry withPlacement(int place) {
            return new LeaderboardEntry(this.player, place, this.score);
        }
    }

    private record LeaderboardEntry(OfflinePlayer player, int place, int score) {

        public Component buildMessage() {
            return translatable("ca.command.mine-stats.leaderboard.entry", NamedTextColor.GREEN,
                    text(this.place), text(String.valueOf(this.player.getName())), text(this.score));
        }
    }
}
