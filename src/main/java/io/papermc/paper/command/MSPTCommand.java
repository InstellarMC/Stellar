package io.papermc.paper.command;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import com.mohistmc.youer.util.I18n;

@DefaultQualifier(NonNull.class)
public final class MSPTCommand extends Command {
    private static final DecimalFormat DF = new DecimalFormat("########0.0");
    private static final Component SLASH = text("/");

    public MSPTCommand(final String name) {
        super(name);
        this.description = I18n.as("msptcmd.description");
        this.usageMessage = "/mspt";
        this.setPermission("bukkit.command.mspt");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        // Stellar start - Port SparklyPaper patches; Track World specific MSPT
        if (args.length == 1) {
            final MinecraftServer server = MinecraftServer.getServer();
            return CommandUtil.getListMatchingLast(sender, args, server.levelKeys().stream().map(ResourceKey::location).toList());
        }
        // Stellar end - Port SparklyPaper patches; Track World specific MSPT
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        MinecraftServer server = MinecraftServer.getServer();

        if (args.length == 0) { // Stellar
        List<Component> times = new ArrayList<>();
        times.addAll(eval(server.tickTimes1s.getTimes())); // Stellar - Add more granular tick times
        times.addAll(eval(server.tickTimes5s.getTimes()));
        times.addAll(eval(server.tickTimes10s.getTimes()));
        times.addAll(eval(server.tickTimes30s.getTimes())); // Stellar - Add more granular tick times
        times.addAll(eval(server.tickTimes60s.getTimes()));

        sender.sendMessage(text().content("Server tick times ").color(GOLD)
                .append(text().color(YELLOW)
                        .append(
                                text("("),
                                text("avg", GRAY),
                                text("/"),
                                text("min", GRAY),
                                text("/"),
                                text("max", GRAY),
                                text(")")
                        )
                ).append(
                        // Stellar start - Add more granular tick times
                        text(" from last 1s"),
                        text(",", GRAY),
                        text(" 5s"),
                        text(",", GRAY),
                        text(" 10s"),
                        text(",", GRAY),
                        text(" 30s"),
                        text(",", GRAY),
                        text(" 1m"),
                        text(":", YELLOW)
                        // Stellar end - Add more granular tick times
                )
        );
        sender.sendMessage(text().content("◴ ").color(GOLD)
                .append(text().color(GRAY)
                        .append(
                                times.get(0), SLASH, times.get(1), SLASH, times.get(2), text(", ", YELLOW),
                                times.get(3), SLASH, times.get(4), SLASH, times.get(5), text(", ", YELLOW),
                                // Stellar start - Add more granular tick times
                                times.get(6), SLASH, times.get(7), SLASH, times.get(8), text(", ", YELLOW),
                                times.get(9), SLASH, times.get(10), SLASH, times.get(11), text(", ", YELLOW),
                                times.get(12), SLASH, times.get(13), SLASH, times.get(14), text(", ", YELLOW)
                                // Stellar end - Add more granular tick times
                        )
                )
        );
        } else if (args.length == 1) { // Stellar
            // Stellar start - Port SparklyPaper patches; Track World specific MSPT
            sender.sendMessage(text());
            sender.sendMessage(text().content("World tick times ").color(GOLD)
                    .append(text().color(YELLOW)
                            .append(
                                    text("("),
                                    text("avg", GRAY),
                                    text("/"),
                                    text("min", GRAY),
                                    text("/"),
                                    text("max", GRAY),
                                    text(")")
                            )
                    ).append(
                            // Stellar start - Add more granular tick times
                            text(" from last 1s"),
                            text(",", GRAY),
                            text(" 5s"),
                            text(",", GRAY),
                            text(" 10s"),
                            text(",", GRAY),
                            text(" 30s"),
                            text(",", GRAY),
                            text(" 1m"),
                            text(":", YELLOW)
                            // Stellar end - Add more granular tick times
                    )
            );
            final var resourcelocation = ResourceLocation.parse(args[0]);
            final var resourcekey = ResourceKey.create(Registries.DIMENSION, resourcelocation);
            final var serverlevel = server.getLevel(resourcekey);
            List<Component> worldTimes = new ArrayList<>();
            worldTimes.addAll(eval(serverlevel.tickTimes1s.getTimes())); // Stellar - Add more granular tick times
            worldTimes.addAll(eval(serverlevel.tickTimes5s.getTimes()));
            worldTimes.addAll(eval(serverlevel.tickTimes10s.getTimes()));
            worldTimes.addAll(eval(serverlevel.tickTimes30s.getTimes())); // Stellar - Add more granular tick times
            worldTimes.addAll(eval(serverlevel.tickTimes60s.getTimes()));

            sender.sendMessage(text().content("◴ " + serverlevel.getWorld().getName() + ": ").color(GOLD)
                    .append(text().color(GRAY)
                            .append(
                                    worldTimes.get(0), SLASH, worldTimes.get(1), SLASH, worldTimes.get(2), text(", ", YELLOW),
                                    worldTimes.get(3), SLASH, worldTimes.get(4), SLASH, worldTimes.get(5), text(", ", YELLOW),
                                    // Stellar start - Add more granular tick times
                                    worldTimes.get(6), SLASH, worldTimes.get(7), SLASH, worldTimes.get(8), text(", ", YELLOW),
                                    worldTimes.get(9), SLASH, worldTimes.get(10), SLASH, worldTimes.get(11), text(", ", YELLOW),
                                    worldTimes.get(12), SLASH, worldTimes.get(13), SLASH, worldTimes.get(14), text(", ", YELLOW)
                                    // Stellar end - Add more granular tick times
                            )
                    )
            );
            // Stellar end - Port SparklyPaper patches; Track World specific MSPT
        }
        return true;
    }

    private static List<Component> eval(long[] times) {
        long min = Integer.MAX_VALUE;
        long max = 0L;
        long total = 0L;
        for (long value : times) {
            if (value > 0L && value < min) min = value;
            if (value > max) max = value;
            total += value;
        }
        double avgD = ((double) total / (double) times.length) * 1.0E-6D;
        double minD = ((double) min) * 1.0E-6D;
        double maxD = ((double) max) * 1.0E-6D;
        return Arrays.asList(getColor(avgD), getColor(minD), getColor(maxD));
    }

    private static Component getColor(double avg) {
        return text(DF.format(avg), avg >= 50 ? RED : avg >= 40 ? YELLOW : GREEN);
    }
}
