package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (02:58 26.10.2025)

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboard;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.NumberConversions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@NullMarked
public final class LeaderboardUtil {

    private LeaderboardUtil() {
    }

    private static LeaderboardEntry calcPlacedEntry(UnplacedLeaderboardEntry entry, int index, @Nullable LeaderboardEntry lastEntry) {
        // calculate placement position, players with the same score share a placement
        // and thereby occupy all following slots (e.g. 1, 1, 3 instead of 1, 1, 2)
        int placement;
        if (lastEntry != null && entry.score() == lastEntry.score()) {
            // don't move one place further if still the same score
            placement = lastEntry.place();
        } else {
            // skip slots already occupied by players with an equal score
            placement = index + 1;
        }

        return entry.withPlacement(placement);
    }

    public static void applyLeaderboard(TextDisplay display, CaConfig.LeaderboardConfig config) {
        LeaderboardResult leaderboard = buildLeaderboard(config);
        if (leaderboard == null) {
            return; // objective can't be found
        }
        ComponentBuilder<?, ?> builder = config.getPrefix();
        for (LeaderboardEntry line : leaderboard.entries()) {
            Component wrappedComp = config.getWrapper().append(line.buildLine());
            builder.append(wrappedComp).appendNewline();
        }
        display.text(builder.append(config.getSuffix()).asComponent());
    }

    public static @Nullable LeaderboardResult buildLeaderboard(CaConfig.LeaderboardConfig config) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(config.getObjective());
        if (objective != null) {
            return buildLeaderboard(objective, config.getEntries(), null);
        }
        return null;
    }

    public static LeaderboardResult buildLeaderboard(Objective objective, int size, @Nullable String self) {
        Scoreboard scoreboard = objective.getScoreboard();
        if (scoreboard == null) {
            throw new IllegalArgumentException("Objective " + objective + " has no assigned scoreboard, can't build leaderboard");
        }
        // use nms to extract player scores for scoreboard to prevent
        // bukkit from wrapping our player scores 10x or more
        net.minecraft.world.scores.Scoreboard mcScoreboard = ((CraftScoreboard) scoreboard).getHandle();
        net.minecraft.world.scores.Objective mcObjective = mcScoreboard.getObjective(objective.getName());
        Collection<PlayerScoreEntry> playerScores = mcScoreboard.listPlayerScores(Objects.requireNonNull(mcObjective));

        // build a map out of the player score list
        Map<String, Integer> entries = new HashMap<>(playerScores.size());
        for (PlayerScoreEntry score : playerScores) {
            if (score.value() > 0) {
                entries.put(score.owner(), score.value());
            }
        }

        // extract self entry
        UnplacedLeaderboardEntry unplacedSelfEntry = self == null ? null :
                new UnplacedLeaderboardEntry(self, entries.getOrDefault(self, 0));

        // build sorted entries from raw entries
        List<UnplacedLeaderboardEntry> unplacedEntries = entries.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .map(entry -> new UnplacedLeaderboardEntry(entry.getKey(), entry.getValue()))
                .toList();
        int avgScore = unplacedEntries.stream()
                .mapToInt(UnplacedLeaderboardEntry::score).average() // calc average
                .stream().mapToInt(NumberConversions::round).findAny() // round to int
                .orElse(0);

        // add placement data to entries
        List<LeaderboardEntry> leaderboard = new ArrayList<>(size);
        LeaderboardEntry lastEntry = null;
        LeaderboardEntry selfEntry = null;
        for (int i = 0; i < unplacedEntries.size(); i++) {
            UnplacedLeaderboardEntry entry = unplacedEntries.get(i);
            LeaderboardEntry placedEntry = calcPlacedEntry(entry, i, lastEntry);
            // add to leaderboard if not already full
            if (leaderboard.size() < size) {
                leaderboard.add(placedEntry);
            }

            lastEntry = placedEntry;
            if (entry.equals(unplacedSelfEntry)) {
                selfEntry = placedEntry;
            }

            if (leaderboard.size() >= size && (selfEntry != null || unplacedSelfEntry == null)) {
                break; // early cancel, all necessary data has already been calculated
            }
        }

        // fallback self entry if not on leaderboard
        if (selfEntry == null && unplacedSelfEntry != null) {
            // all remaining entries occupy the slots before the self entry
            selfEntry = unplacedSelfEntry.withPlacement(unplacedEntries.size() + 1);
        }

        // add self entry to bottom of leaderboard
        if (selfEntry != null && !leaderboard.contains(selfEntry)) {
            leaderboard.add(selfEntry);
        }

        return new LeaderboardResult(leaderboard, avgScore);
    }

    private record UnplacedLeaderboardEntry(String entry, int score) {

        public LeaderboardEntry withPlacement(int place) {
            return new LeaderboardEntry(this.entry, place, this.score);
        }
    }

    public record LeaderboardEntry(String entry, int place, int score) {

        public Component buildLine() {
            return translatable("ca.command.mine-stats.leaderboard.entry", NamedTextColor.GREEN,
                    text(this.place), text(this.entry), text(this.score));
        }
    }

    public record LeaderboardResult(List<LeaderboardEntry> entries, int average) {
    }
}
