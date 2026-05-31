package dev.instellar.stellar.command.brigadier;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.channel.embedded.EmbeddedChannel;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public final class CreateFakePlayersCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("createfakeplayers")
            .requires(source -> source.hasPermission(2))
            // createfakeplayers <amount>
            .then(
                Commands.argument("amount", IntegerArgumentType.integer(1, 500))
                .executes(
                    ctx -> createFakePlayers(
                        ctx,
                        IntegerArgumentType.getInteger(ctx, "amount"),
                        ctx.getSource().getLevel()
                    )
                )
            )
            // createfakeplayers <dimension> <amount>
            .then(
                Commands.argument("dimension", DimensionArgument.dimension())
                .then(
                    Commands.argument("amount", IntegerArgumentType.integer(1, 500))
                    .executes(
                        ctx -> createFakePlayers(
                            ctx,
                            IntegerArgumentType.getInteger(ctx, "amount"),
                            DimensionArgument.getDimension(ctx, "dimension")
                        )
                    )
                )
            )
        );
    }

    private static int lotNo = 0;
    private static int createFakePlayers(
        final CommandContext<CommandSourceStack> ctx,
        final int amount,
        final ServerLevel dimension
    ) throws CommandSyntaxException {
        FakePlayerFactory.unloadLevel(dimension);

        final ReferenceArraySet<ServerPlayer> fakePlayers = new ReferenceArraySet<>();
        final int max = amount + lotNo;
        boolean success = true;
        try {
            for (; lotNo < max; lotNo++) {
                final var gameProfile = new GameProfile(UUID.randomUUID(), "FakePlayer" + String.format("%04d", lotNo));
                final var cookie = CommonListenerCookie.createInitial(gameProfile, false);
                final var connection = new Connection(PacketFlow.SERVERBOUND);
                connection.channel = new EmbeddedChannel(connection);
                ServerPlayer fakePlayer = FakePlayerFactory.get(dimension, gameProfile);
                fakePlayers.add(fakePlayer);

                ctx.getSource().getServer().getPlayerList().placeNewPlayer(connection, fakePlayer, cookie, Optional.empty());
            }
        } catch (final Exception ignored) {
            success = false;
        }

        if (lotNo != max && !success) {
            final var message = text("Created").appendSpace().append(text(String.valueOf(lotNo - (max - amount))))
                // Created X
                .appendSpace().append(text("/")).appendSpace().append(text(String.valueOf(amount)))
                // Created X / Y
                .appendSpace().append(text("fake players before an error occurred."))
                // Created X / Y fake players before an error occurred.
                .color(NamedTextColor.RED);

            ctx.getSource().getSender().sendMessage(message);
            return lotNo - (max - amount);
        }

        final var message = text("Successfully created").appendSpace().append(text(String.valueOf(amount)))
            // Successfully created X
            .appendSpace().append(text("fake players."))
            // Successfully created X fake players.
            .color(net.kyori.adventure.text.format.NamedTextColor.GREEN);

        ctx.getSource().getSender().sendMessage(message);
        return amount;
    }
}