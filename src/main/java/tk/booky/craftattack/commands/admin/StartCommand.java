package tk.booky.craftattack.commands.admin;
// Created by booky10 in CraftAttack (14:18 06.08.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tk.booky.craftattack.CraftAttackMain;

public class StartCommand extends CommandAPICommand implements CommandExecutor {

    public StartCommand() {
        super("start");

        withPermission("craftattack.command.admin.start");

        executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        World world = Bukkit.getWorlds().get(0);
        int timeId = Bukkit.getScheduler().runTaskTimer(CraftAttackMain.main,
            () -> world.setTime(world.getTime() + 20), 20, 1).getTaskId();

        new BukkitRunnable() {
            private int countdown = 30;

            @Override
            public void run() {
                switch (countdown--) {
                    case 10:
                    case 5:
                    case 4:
                    case 3:
                    case 2:
                    case 1:
                        Title.Times countdownTimes = Title.Times.of(Ticks.duration(5), Ticks.duration(10), Ticks.duration(5));
                        Title countdownTitle = Title.title(Component.text(countdown + 1, NamedTextColor.GOLD, TextDecoration.BOLD), Component.empty(), countdownTimes);

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.AMBIENT, 100000f, 1f);
                            player.showTitle(countdownTitle);
                        }

                        if (countdown == 0) {
                            Bukkit.broadcast(Component.text("CraftAttack 6 startet in 1 Sekunde!", NamedTextColor.GREEN));
                            break;
                        }
                    case 30:
                    case 15:
                        Bukkit.broadcast(Component.text("CraftAttack 6 startet in " + (countdown + 1) + " Sekunden!", NamedTextColor.GREEN));
                        break;
                    case 0:
                        Title.Times startTimes = Title.Times.of(Ticks.duration(10), Ticks.duration(100), Ticks.duration(20));
                        Title startTitle = Title.title(Component.text("CloudCraft"), Component.text("CraftAttack 6", NamedTextColor.GREEN), startTimes);

                        Component message = Component.text("CraftAttack 6 hat begonnen!", NamedTextColor.GREEN);
                        Bukkit.getConsoleSender().sendMessage(message);

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.AMBIENT, 100000f, 1f);
                            player.sendMessage(message);
                            player.showTitle(startTitle);
                        }

                        Bukkit.getScheduler().runTaskLater(CraftAttackMain.main,
                            () -> world.getWorldBorder().setSize(59999968), 20 * 100);
                        world.getWorldBorder().setSize(world.getWorldBorder().getSize() + 2000, 100);

                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);

                        Bukkit.getScheduler().cancelTask(timeId);
                        Bukkit.getScheduler().cancelTask(getTaskId());
                    default:
                        break;
                }
            }
        }.runTaskTimer(CraftAttackMain.main, 20, 20);
    }
}
