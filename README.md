# Enderpedia

Every guide book in your pack, one click away. NeoForge 1.21.1.

> **Alpha:** usable, but expect rough edges. **Feedback, bug reports and feature ideas are very welcome**: please post them in **[Issues](https://github.com/SteelArchadian/Enderpedia/issues)**. Every one gets read, and feature suggestions are genuinely encouraged.

- **Book button** in the survival and creative inventory (and an optional keybind, unbound by default) opens an index of every Patchouli, Modonomicon and GuideME guide installed. Search by book or mod; click to read. No item needed. Shift-click hands you the book as an item if you want one; right-click pins a book to the top.
- **No more join books:** guide books that mods put in your inventory on your first join are taken away (the first 15 seconds after the first join only; crafted books are never touched). Keep specific books with the `keepBooks` setting.
- **Your shelf:** written books (welcome letters, rules, lore) and any other items listed in `extraJoinItems` or the `#enderpedia:join_items` tag are taken on first join too, but kept on your shelf in the index. Read written books there, or shift-click to take an item back. Works with any mod's items, no built-in mod list.
- Works with any mix of Patchouli, Modonomicon and GuideME, or none (then the index is just empty). Settings show in Config Panel / the NeoForge config screen.

## Debug commands (op, level 2)
- `/enderpedia list` - every book id, title, system and mod.
- `/enderpedia open <player>` - opens the index on that player's screen.
- `/enderpedia sweep <player>` - takes guide books from their inventory now.
- `/enderpedia window <player> get|start [seconds]|clear` - the join window.
- `/enderpedia joined <player> get|reset` - reset makes the next login count as a first join.
- `/enderpedia shelf <player> list|clear|give <slot>` - the player's shelf.

## Build
`gradlew build` -> `build/libs/enderpedia-<ver>.jar`. Compiles against `libs/` (Patchouli, Modonomicon; both optional at runtime) and Config Panel from mavenLocal. Icons: `python -I tools/make_icons.py <project dir>`.

## License
GNU LGPL 3.0 only: see [LICENSE](LICENSE), which builds on the GPL 3.0 in [COPYING](COPYING).

Made with AI assistance.
