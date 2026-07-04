# CDC Development Knowledge Index

This directory stores CDC development knowledge that is useful for future work but too detailed for root `AGENTS.md`.

## Storage Rules

| Knowledge type | File |
|---|---|
| Implemented CDC features, visible behavior, implementation outline, and main code locations | `docs/dev-knowledge/content-map.md` |
| Lightweight technical "how do I change this kind of thing" notes | `docs/dev-knowledge/how-to-index.md` |
| Current repository-wide constraints and route pointers | `AGENTS.md` or a directory-level `AGENTS.md` |
| Historical bugs, root causes, and workarounds | `docs/lessons-learned.md` |

## Entry Rules

- Content facts should say what exists, how it behaves, where it lives, and the current status.
- How-to facts should say the goal, edit locations, checklist, and validation command.
- Link longer notes instead of copying long paragraphs.
- Keep one fact in one owning file; other files should point to it.
- Prefer concrete paths, class names, providers, recipe ids, and commands because future agents search for those.
