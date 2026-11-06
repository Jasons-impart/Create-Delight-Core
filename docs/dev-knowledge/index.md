# CDC Development Knowledge Index

This directory stores CDC development knowledge that is useful for future work but too detailed for root `AGENTS.md`.

## Storage Rules

| 知识类型 | 目标位置 |
|---|---|
| 已实现功能、可见行为、实现位置 | [content-map.md](content-map.md) |
| 轻量技术做法（某类修改该怎么做） | [how-to-index.md](how-to-index.md) |
| 项目技术专题 | 本目录下的专题文档；多文件专题以 `README.md` 为入口。当前：[Quality Food 品质机制](quality-food.md)、[雪傀儡冷却室](snowman-cooler.md) |
| 常驻约束和入口路由 | 根 `AGENTS.md` 或目录级 `AGENTS.md` |
| 历史故障和绕路经验 | [../lessons-learned.md](../lessons-learned.md) |
| 外部模组缺陷或兼容差异的定向补丁、验证及升级复核条件 | [compatibility-patches.md](compatibility-patches.md) |

## Entry Rules

- Content facts should say what exists, how it behaves, where it lives, and the current status.
- How-to facts should say the goal, edit locations, checklist, and validation command.
- Link longer notes instead of copying long paragraphs.
- Keep one fact in one owning file; other files should point to it.
- Prefer concrete paths, class names, providers, recipe ids, and commands because future agents search for those.
- `docs/dev-knowledge/` 正文以中文为主；模组名、路径、类名和命令可保留英文。
