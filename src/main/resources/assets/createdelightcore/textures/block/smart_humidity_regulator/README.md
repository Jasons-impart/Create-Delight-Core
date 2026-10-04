# 智能湿度调节器贴图与模型

将 PNG 放在此目录，在模型中引用
`createdelightcore:block/smart_humidity_regulator/<贴图名>`（不带 `.png`）。

保留以下三个模型入口，后续直接替换 JSON/PNG：

- `models/block/smart_humidity_regulator/block.json`：静态主体，当前使用高炉占位。
- `models/block/smart_humidity_regulator/item.json`：完整静态物品模型，最终包含主体与齿轮。
- `models/block/smart_humidity_regulator/cog.json`：独立旋转齿轮。

齿轮布局参照 Vintage Improvements 0.3.7.8 的压缩机
`models/block/vacuum_chamber/cog.json`：在方块中段横放，Y 轴旋转，
齿轮占据 Y=5..11，齿尖从 X/Z=-1..17 的四个侧面露出。
当前占位齿轮使用 Create 磨石贴图，模型本身不依赖 Vintage Improvements。
旋转轴线经过 `(8, 8, 8)`，普通渲染和 Flywheel 均按 Y 轴处理。
动力由水平侧面的相邻齿轮啮合输入；所有面均不开放传动轴口。
顶面、底面不接受齿轮连接或从该面放置齿轮。
顶部进水、底部洒水，保持畅通。

四个竖直侧面各有一个设置热点：水平居中、距底部 3/16 格，
位置参考动力臂下方的设置区。主体默认正面朝北，
放置朝向只旋转主体纹理，不改变齿圈、四个设置热点或上下流体方向。
物品模型应组合相同的中段横向齿圈。
