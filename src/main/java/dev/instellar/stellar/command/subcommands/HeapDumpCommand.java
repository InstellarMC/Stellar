package dev.instellar.stellar.command.subcommands;

import dev.instellar.stellar.util.JvmUtil;
import io.papermc.paper.command.PaperSubcommand;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

import static net.kyori.adventure.text.Component.text;

@NullMarked
public final class HeapDumpCommand implements PaperSubcommand {
    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        this.doDump(sender);
        return true;
    }

    private void doDump(final CommandSender sender) {
        Command.broadcastCommandMessage(sender, text("Writing JVM heap data"));

        if (JvmUtil.dumpHeap()) {
            Command.broadcastCommandMessage(sender, text("Heap dump complete", NamedTextColor.GREEN));
        } else {
            Command.broadcastCommandMessage(sender, text("Failed to write heap dump. Check the console for more information.", NamedTextColor.RED));
        }
    }
}
