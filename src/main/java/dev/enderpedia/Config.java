package dev.enderpedia;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class Config {
    // Common: the server decides what happens to books handed out on join.
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.BooleanValue BLOCK_JOIN_BOOKS;
    public static final ModConfigSpec.IntValue JOIN_WINDOW_SECONDS;
    public static final ModConfigSpec.BooleanValue TELL_PLAYER;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> KEEP_BOOKS;
    public static final ModConfigSpec.BooleanValue TAKE_WRITTEN_BOOKS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXTRA_JOIN_ITEMS;
    public static final ModConfigSpec.BooleanValue ALLOW_COPIES;

    // Client: how the button and index look.
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_BUTTON;
    public static final ModConfigSpec.BooleanValue SHOW_IN_CREATIVE;
    public static final ModConfigSpec.IntValue BUTTON_X;
    public static final ModConfigSpec.IntValue BUTTON_Y;
    public static final ModConfigSpec.BooleanValue INCLUDE_PATCHOULI;
    public static final ModConfigSpec.BooleanValue INCLUDE_MODONOMICON;
    public static final ModConfigSpec.BooleanValue INCLUDE_GUIDEME;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_BOOKS;
    public static final ModConfigSpec.BooleanValue SHOW_SHELF;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FAVORITE_BOOKS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("joinbooks");
        BLOCK_JOIN_BOOKS = b.comment("Takes away guide books that mods put in your inventory when you first join. You can still read them from the inventory button, and crafted books are never touched.")
                .define("blockJoinBooks", true);
        JOIN_WINDOW_SECONDS = b.comment("How long after a player's first join books are taken away. Mods hand out books in the first few seconds. Default 15.")
                .defineInRange("joinWindowSeconds", 15, 1, 300);
        TELL_PLAYER = b.comment("Shows a chat line saying how many books were moved to the index.")
                .define("tellPlayer", true);
        KEEP_BOOKS = b.comment("Books and items players keep in their inventory, by book id or item id (for example patchouli:guide or modid:manual). Use /enderpedia list to see book ids.")
                .defineListAllowEmpty("keepBooks", List.of(), () -> "", o -> o instanceof String);
        TAKE_WRITTEN_BOOKS = b.comment("Also takes written books handed out on first join (welcome letters, lore, rules). They go on the player's shelf in the index, where they can be read or taken back.")
                .define("takeWrittenBooks", true);
        EXTRA_JOIN_ITEMS = b.comment("Other items to take on first join, from any mod: item ids (modid:item) or item tags (#modid:tag). They go on the player's shelf, where they can be taken back. Datapacks can also add items to the tag #enderpedia:join_items.")
                .defineListAllowEmpty("extraJoinItems", List.of(), () -> "", o -> o instanceof String);
        b.pop();
        b.push("copies");
        ALLOW_COPIES = b.comment("Lets players get a guide book as an item from the index (shift-click a book). Turn off on servers where book items should only be crafted.")
                .define("allowCopies", true);
        b.pop();
        COMMON_SPEC = b.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        c.push("button");
        SHOW_BUTTON = c.comment("Shows the book button in the survival inventory.")
                .define("showButton", true);
        SHOW_IN_CREATIVE = c.comment("Also shows the button in the creative inventory.")
                .define("showInCreative", true);
        BUTTON_X = c.comment("Moves the button left or right, in pixels. Use it if another mod's button sits in the same spot.")
                .defineInRange("buttonX", 0, -200, 200);
        BUTTON_Y = c.comment("Moves the button up or down, in pixels.")
                .defineInRange("buttonY", 0, -200, 200);
        c.pop();
        c.push("index");
        INCLUDE_PATCHOULI = c.comment("Lists Patchouli books in the index.")
                .define("includePatchouli", true);
        INCLUDE_MODONOMICON = c.comment("Lists Modonomicon books in the index.")
                .define("includeModonomicon", true);
        INCLUDE_GUIDEME = c.comment("Lists GuideME guides in the index (Applied Energistics 2 and other tech mods use them).")
                .define("includeGuideme", true);
        HIDDEN_BOOKS = c.comment("Books left out of the index, by book id.")
                .defineListAllowEmpty("hiddenBooks", List.of(), () -> "", o -> o instanceof String);
        SHOW_SHELF = c.comment("Lists your shelf (written books and items taken on your first join) in the index.")
                .define("showShelf", true);
        FAVORITE_BOOKS = c.comment("Books pinned to the top of the index, by book id. Right-click a book in the index to pin or unpin it.")
                .defineListAllowEmpty("favoriteBooks", List.of(), () -> "", o -> o instanceof String);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    private Config() {}
}
