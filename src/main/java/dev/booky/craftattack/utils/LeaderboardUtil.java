package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (02:58 26.10.2025)

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.NumberConversions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@NullMarked
public final class LeaderboardUtil {

    private LeaderboardUtil() {
    }

    private static LeaderboardEntry calcPlacedEntry(UnplacedLeaderboardEntry entry, @Nullable LeaderboardEntry lastEntry) {
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

    public static LeaderboardResult buildLeaderboard(Objective objective, int size, @Nullable String self) {
        Scoreboard scoreboard = objective.getScoreboard();
        if (scoreboard == null) {
            throw new IllegalArgumentException("Objective " + objective + " has no assigned scoreboard, can't build leaderboard");
        }
        // request all entries from scoreboard objective
        Set<String> entryNames = scoreboard.getEntries();
        Object2IntMap<String> entries = new Object2IntArrayMap<>(entryNames.size());
        for (String entryName : entryNames) {
            try {
                UUID.fromString(entryName);
                continue; // ignore entity entries
            } catch (IllegalArgumentException ignored) {
            }

            Score score = objective.getScore(entryName);
            if (score.getScore() > 0) {
                entries.put(entryName, score.getScore());
            }
        }

        // extract self entry
        UnplacedLeaderboardEntry unplacedSelfEntry = self == null ? null :
                new UnplacedLeaderboardEntry(self, entries.getInt(self));

        // build sorted entries from raw entries
        List<UnplacedLeaderboardEntry> unplacedEntries = entries.object2IntEntrySet().stream()
                .sorted(Comparator.<Object2IntMap.Entry<String>>comparingInt(Object2IntMap.Entry::getIntValue).reversed())
                .map(entry -> new UnplacedLeaderboardEntry(entry.getKey(), entry.getIntValue()))
                .toList();
        int avgScore = unplacedEntries.stream()
                .mapToInt(UnplacedLeaderboardEntry::score).average() // calc average
                .stream().mapToInt(NumberConversions::round).findAny() // round to int
                .orElse(0);

        // add placement data to entries
        List<LeaderboardEntry> leaderboard = new ArrayList<>(size);
        LeaderboardEntry lastEntry = null;
        LeaderboardEntry selfEntry = null;
        for (UnplacedLeaderboardEntry entry : unplacedEntries) {
            LeaderboardEntry placedEntry = calcPlacedEntry(entry, lastEntry);
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
            int placement = lastEntry == null ? 1 : lastEntry.place() + 1;
            selfEntry = unplacedSelfEntry.withPlacement(placement);
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
