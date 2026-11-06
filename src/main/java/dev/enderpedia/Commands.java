package dev.enderpedia;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Op-only test commands:
 * /enderpedia list
 * /enderpedia open <player>
 * /enderpedia sweep <player>
 * /enderpedia window <player> get|start|start <seconds>|clear
 * /enderpedia joined <player> get|reset
 * /enderpedia shelf <player> list|clear|give <slot>
 */
public final class Commands {
    private Commands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(literal("enderpedia").requires(s -> s.hasPermission(2))
                .then(literal("list").executes(Commands::list))
                .then(literal("open").then(argument("player", EntityArgument.player()).executes(ctx -> {
                    ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                    Network.openIndex(p);
                    return 1;
                })))
                .then(literal("sweep").then(argument("player", EntityArgument.player()).executes(ctx -> {
                    ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                    int n = JoinBooks.sweep(p);
                    ctx.getSource().sendSuccess(() -> Component.literal("Took " + n + " guide book stack(s) from " + p.getGameProfile().getName()), true);
                    return n;
                })))
                .then(literal("window").then(argument("player", EntityArgument.player())
                        .then(literal("get").executes(ctx -> {
                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                            int t = JoinBooks.ticksLeft(p);
                            ctx.getSource().sendSuccess(() -> Component.literal(t > 0
                                    ? "Join window open: " + (t / 20) + "s left" : "No join window open"), false);
                            return t;
                        }))
                        .then(literal("start").executes(ctx -> start(ctx, Config.JOIN_WINDOW_SECONDS.get()))
                                .then(argument("seconds", IntegerArgumentType.integer(1, 3600))
                                        .executes(ctx -> start(ctx, IntegerArgumentType.getInteger(ctx, "seconds")))))
                        .then(literal("clear").executes(ctx -> {
                            JoinBooks.clearWindow(EntityArgument.getPlayer(ctx, "player"));
                            ctx.getSource().sendSuccess(() -> Component.literal("Join window closed"), true);
                            return 1;
                        }))))
                .then(literal("joined").then(argument("player", EntityArgument.player())
                        .then(literal("get").executes(ctx -> {
                            boolean j = JoinBooks.hasJoined(EntityArgument.getPlayer(ctx, "player"));
                            ctx.getSource().sendSuccess(() -> Component.literal(j ? "Has joined before" : "Next login counts as a first join"), false);
                            return j ? 1 : 0;
                        }))
                        .then(literal("reset").executes(ctx -> {
                            JoinBooks.resetJoined(EntityArgument.getPlayer(ctx, "player"));
                            ctx.getSource().sendSuccess(() -> Component.literal("Next login counts as a first join"), true);
                            return 1;
                        }))))
                .then(literal("shelf").then(argument("player", EntityArgument.player())
                        .then(literal("list").executes(Commands::shelf))
                        .then(literal("clear").executes(ctx -> {
                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                            p.setData(PlayerBooks.TYPE, p.getData(PlayerBooks.TYPE).cleared());
                            Network.sendShelf(p);
                            ctx.getSource().sendSuccess(() -> Component.literal("Shelf cleared"), true);
                            return 1;
                        }))
                        .then(literal("give").then(argument("slot", IntegerArgumentType.integer(0)).executes(ctx -> {
                            ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
                            int slot = IntegerArgumentType.getInteger(ctx, "slot");
                            PlayerBooks data = p.getData(PlayerBooks.TYPE);
                            if (slot >= data.shelf().size()) {
                                ctx.getSource().sendFailure(Component.literal("No shelf slot " + slot));
                                return 0;
                            }
                            Shelves.give(p, data.shelf().get(slot).copy());
                            p.setData(PlayerBooks.TYPE, data.withRemoved(slot));
                            Network.sendShelf(p);
                            ctx.getSource().sendSuccess(() -> Component.literal("Gave shelf slot " + slot + " back"), true);
                            return 1;
                        }))))));
    }

    private static int shelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
        List<net.minecraft.world.item.ItemStack> shelf = p.getData(PlayerBooks.TYPE).shelf();
        ctx.getSource().sendSuccess(() -> Component.literal(shelf.size() + " item(s) on the shelf:"), false);
        for (int i = 0; i < shelf.size(); i++) {
            int slot = i;
            ctx.getSource().sendSuccess(() -> Component.literal(" " + slot + ": ").append(shelf.get(slot).getHoverName())
                    .append(Component.literal(" x" + shelf.get(slot).getCount())), false);
        }
        return shelf.size();
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        List<GuideBook> books = Books.all();
        Enderpedia.LOGGER.info("/enderpedia list: {} book(s): {}", books.size(), books.stream().map(b -> b.id() + " [" + b.system() + "]").toList());
        ctx.getSource().sendSuccess(() -> Component.literal(books.size() + " guide book(s):"), false);
        for (GuideBook b : books) {
            ctx.getSource().sendSuccess(() -> Component.literal(" " + b.id() + "  ").append(b.title())
                    .append(Component.literal("  [" + b.system() + ", " + b.modName() + "]")), false);
        }
        return books.size();
    }

    private static int start(CommandContext<CommandSourceStack> ctx, int seconds) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
        JoinBooks.startWindow(p, seconds);
        ctx.getSource().sendSuccess(() -> Component.literal("Join window open for " + seconds + "s"), true);
        return seconds;
    }
}
