package dev.instellar.stellar.command;

import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;

import java.util.HashMap;
import java.util.Map;

public final class StellarCommands {

    private StellarCommands() {
        throw new AssertionError();
    }

    private static final Map<String, Command> COMMANDS = new HashMap<>();
    static {
        COMMANDS.put("stellar", new StellarCommand("stellar"));
    }

    public static void registerCommands(final MinecraftServer server) {
        COMMANDS.forEach((s, command) -> {
            server.server.getCommandMap().register(s, "Stellar", command);
        });
    }
}
