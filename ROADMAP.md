# Markor Live Preview Roadmap

This roadmap tracks the work to evolve Markor from a source-only Markdown editor into a modern editor with an Obsidian-style Live Preview mode while keeping the existing WebView reading preview.

## Current Status

[X] Live Preview highlighter class created as a separate implementation:
`app/src/main/java/net/gsantner/markor/format/markdown/MarkdownLivePreviewHighlighter.java`

[X] Live Preview integrated into the Markdown format selection path in `FormatRegistry`

[X] Live Preview setting added under Markdown settings:
- Markdown editing mode
- Live Preview syntax markers

[X] Live Preview enabled by default for Markdown documents

[X] Source mode remains available

[X] Existing WebView preview remains available as Reading Preview

[X] `HighlightingEditor` updated to support cursor-aware selection refresh for Live Preview highlighters

[X] Initial visible Live Preview styling implemented for:
- Headings
- Bold
- Italic
- Strikethrough
- Inline code
- Links
- Task list lines

[X] Markdown syntax markers are currently dimmed only

[X] Temporary menu-level toggle added so the user can switch between Source mode and Live Preview mode

[X] Menu label now indicates whether Markdown editor is in Source mode or Live Preview mode

[X] Diagnostic logging added to report whether Markdown uses:
- `MarkdownLivePreviewHighlighter`
- `MarkdownSyntaxHighlighter`

[X] Large-file fallback path added so Markdown can fall back to classic syntax highlighting

[X] Debug build verified successfully with the current integration

## Next Steps

[] Cursor-near behavior refinement:
syntax should reappear more precisely when the cursor is inside or adjacent to a formatted region

[] Visible-region optimization:
reduce recomputation scope for large Markdown files

[] Per-file or global UI polish:
make Source/Live Preview/Reading Preview distinctions clearer in menus and labels

[] Experimental marker hiding mode validation:
only if cursor movement, selection, copy/paste, undo/redo, and deletion remain stable

[] Obsidian-style extensions:
- `[[Note]]`
- `[[Note|Alias]]`
- `![[image.png]]`
- `==highlight==`
- `%% comments %%`
- callouts
- improved table styling
- Mermaid/math block detection styling

[] Markdown formatting parity gaps from Obsidian docs:
- support `__bold__` in addition to `**bold**`
- support `_italic_` in addition to `*italic*`
- support `***bold italic***` and `___bold italic___`
- support nested emphasis such as `**bold and _nested italic_**`
- support escaped Markdown sequences more robustly so `\*`, `\_`, `\#`, ``\````, `\|`, `\~`, and `1\.` remain visibly literal in Live Preview

[] Paragraph and line break behavior:
- expose and document a strict line break mode comparable to Obsidian's editor behavior
- make `Shift+Enter` clearly insert a hard line break inside paragraphs and lists
- improve visual distinction between soft line continuation and paragraph breaks in Live Preview

[] List editing improvements:
- make nested ordered, unordered, and task lists more visually obvious in Live Preview
- improve `Tab` / `Shift+Tab` handling for indent and unindent of selected list items
- preserve numbering behavior cleanly when inserting line breaks inside ordered lists
- support non-`x` task markers such as `[-]` and `[?]` with sensible visual treatment

[] Inline code and fenced code improvements:
- support double-backtick inline code spans for content containing literal backticks
- style fenced code blocks created with backticks or tildes
- detect and style fenced language identifiers more clearly
- handle nested fenced code blocks safely in Source mode and Live Preview
- distinguish indented code blocks from normal indented text

[] Link and embed improvements:
- improve Markdown links to local files with spaces, including `<...>` URL wrapping
- support visual handling for external image syntax and image size hints such as `![alt|100x145](url)`
- support block-reference style links and embeds such as `![[Link#^id]]`
- support internal block definition markers such as `^id`

[] Footnotes:
- detect footnote references like `[^1]` and named footnotes like `[^note]`
- style footnote definition blocks
- support inline footnotes such as `^[text]` in Reading Preview and provide safe editor styling

[] Comments:
- distinguish inline and multiline `%% comment %%` blocks more clearly in Live Preview
- ensure comments remain editable and discoverable without affecting saved Markdown

[] Tables:
- support canonical pipe-table syntax with header separators for Live Preview and Reading Preview parity
- support tables even when outer edge pipes are omitted
- accept header rows with at least two hyphens per column, without requiring perfect visual alignment
- detect tables more reliably even when outer pipes are omitted
- visually distinguish header rows, separator rows, and body rows in Live Preview
- visually support header separators and column alignment markers `:--`, `:--:`, `--:`
- support left, center, and right column alignment markers
- support escaped pipes `\|` inside table cells and link aliases
- preserve escaped pipes in wikilinks, aliases, and image size syntax inside tables
- support formatting inside table cells:
  links, internal links, emphasis, code, embeds, and images where safe
- support image/embed sizing hints inside table cells such as `![[image.png\|200]]`
- add insert-table command parity in the editor UI if missing from discoverable menus
- add a simple Insert Table action that creates a basic editable Markdown table scaffold
- add row/column add, delete, move, and sort operations for Markdown tables
- add context-menu or table-action affordances for row and column editing in Live Preview
- format content inside tables without breaking links, embeds, or inline styles

[] Diagrams and math:
- style Mermaid fenced blocks as first-class diagram blocks in Live Preview
- detect Mermaid internal-link classes without trying to fully render diagrams in the editor
- improve inline math `$...$` styling in Live Preview
- improve block math `$$...$$` styling in Live Preview

[] Tag-aware editing:
- highlight inline tags such as `#meeting`
- support nested tags such as `#inbox/to-read`
- avoid false positives on numeric fragments that should not become tags
- consider tag navigation and search affordances that fit Markor without relying on Vault features

[] Horizontal rules:
- improve Live Preview styling for `***`, `---`, `___`, and spaced variants like `* * *`

[] Quote and callout polish:
- visually distinguish plain blockquotes from callouts
- support quote attribution lines cleanly without breaking Markdown editing

[] Reading Preview parity:
- ensure syntax that already exists in WebView preview also receives coherent Source mode and Live Preview treatment where practical
- review unsupported or partially supported Obsidian-flavored Markdown extensions and map them explicitly to:
  Source-only
  Live Preview styled
  Reading Preview rendered

[] Toolbar and command improvements for Live Preview workflows

[] Keyboard shortcut review for mode switching

[] Manual testing pass on device and emulator:
- undo/redo
- copy/paste
- search and replace
- rotation
- large files
- many headings and links
- save/reopen
- switching between Source, Live Preview, and Reading Preview
- dark theme and light theme
- backspace/delete near Markdown markers

[] Follow-up cleanup:
- reduce false-positive matches
- improve span layering
- add tests where practical
