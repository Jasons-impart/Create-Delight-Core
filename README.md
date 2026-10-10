# Create Delight Core

Create Delight Core（CDC，模组 ID：`createdelightcore`）是 **Create Delight Remake** 的自定义模组，提供整合包使用的机械设备、农业与食物内容、材料与流体，并补充不同模组之间的联动和兼容修复。

[项目仓库](https://github.com/Jasons-impart/Create-Delight-Core) · [Jasons-impart 项目列表](https://github.com/orgs/Jasons-impart/repositories)

## 运行环境

| 项目 | 当前配置 |
| --- | --- |
| Minecraft | 1.20.1 |
| 模组加载器 | Forge，当前构建目标为 47.4.16 |
| Java | 17 |
| 必需模组 | Create、Create Dragons Plus |
| CDC 版本 | 以 [gradle.properties](gradle.properties) 中的 `mod_version` 为准 |

必需依赖由 [mods.toml](src/main/resources/META-INF/mods.toml) 声明。下文列出的模组是 CDC 的联动或修复对象；安装时还需遵循所用整合包及对应模组的依赖要求。

## 主要内容

### 机械与农业

- **智能湿度调节器**：可设置目标绝对湿度，在以设备为中心的 **9×9×9** 范围内调节湿度，并适配 Ecliptic Seasons 的湿度读数和作物判定。侧面连接齿轮传递动力、顶部供水，绝对转速至少为 **16 RPM** 时工作；右键底面中心可打开设置界面。多个设备覆盖同一位置时，取最高的目标湿度。
- **农业设备与材料**：提供加湿器、除湿器、幻影堆肥等内容，服务于整合包的种植与环境调节玩法。
- **品质与生命质设备**：提供品控收割控制器、生命质注入器等方块，为收获与加工流程补充内容。

### 食物与加工

- 添加披萨、冰淇淋、炸物食材、巧克力模具和果冻等内容，扩展整合包的食物制作与加工链。
- 为不同食物模组补充采收、加工和食物品质联动，让相关产物能够接入整合包的生产流程。

### 材料与流体

- 提供金币、金属材料与多种熔融材料流体，以及黏液、奶昔、葡萄汁、糖浆和果冻流体。
- 提供携带遗传数据的**遗传培养液**，并在 JEI 中按遗传类型展示，方便查询相关配方。
- 添加浆液混合、配置模块补充等配方类型，支持对应的加工与配置流程。

### 订单与供货

提供**订单公告板、订单请求器、供货委托台**等方块，用于整合包的订单与供货玩法。

## 模组联动与修复

下表概括 CDC 当前实现的主要联动和补丁，具体配方与玩法安排由整合包提供。

| 涉及模组 | 具体改动 |
| --- | --- |
| Ecliptic Seasons | 将 CDC 湿度设备接入湿度读数与作物生长判定。 |
| Create Diesel Generators | 为大型发酵罐补充 Create 过滤槽，支持按物品或流体输出筛选配方。 |
| Create: Metallurgy | 为工业坩埚适配合金配方处理，修复结构仪表缓存，并支持按配置屏蔽指定的砂带研磨配方。 |
| Create Integrated Farming | 将区域收割流程接入 CDC 的品质收割机制，并为真空收割机增加品质输入槽。 |
| Farmer’s Respite、Youkai’s Homecoming | 将 Farmer’s Respite 茶树采收的不同茶叶统一为 Youkai’s Homecoming 的茶叶，保留原有采收流程。 |
| Farmer’s Delight、Quality Food | 保留番茄作物在生长阶段和形态转换前后的品质，避免转换时丢失品质。 |
| Fruits Delight、Quality Food | 支持灌木果实继承植株品质，并提供 CDC 自定义果冻及对应流体。 |
| Tetra | 为全息物品界面增加分页、翻页按钮和页码，方便浏览较多物品。 |
| Tetra、Create: Enchantment Industry | 支持将带有数据的 Tetra 卷轴作为模板，通过打印流程复制卷轴。 |
| JEI-Tetra | 修复提升名称在配方组件和提示中的显示。 |
| Sophisticated Backpacks、Sol Apple Pie | 调整高级喂食升级的食物选择，在满足喂食条件和过滤规则的食物中，优先选择最有利于饮食多样性的食物。 |
| Apothic Attributes | 支持按配置启用采用递减暴击倍率的多重暴击计算。 |
| Iron’s Spells ’n Spellbooks | 支持按配置让回声打击使用原始伤害，避免后续伤害修正再次计入回声。 |
| Travel Optics | 将 Shadowed Miasma 的伤害倍率成长调整为递减收益。 |
| Alex’s Mobs | 修复绯红蚊被扭曲蟾蜍捕食时使用异常伤害值的问题。 |
| AE2 | 修复线缆在 Create 思索（Ponder）场景中的连接显示。 |
| ExtendedAE | 调整零件包列表初始化时机，避免注册顺序引起的问题。 |
| JEI | 为遗传培养液提供按遗传数据区分的展示，避免不同类型混为同一种流体。 |

## 开发与构建

使用 **Java 17**。项目通过 ForgeGradle 构建，使用 Parchment mappings；依赖同时来自远程 Maven 和本地 `libs/`，具体配置见 [build.gradle](build.gradle)。

在仓库根目录的 PowerShell 中执行：

```powershell
# 构建模组
.\gradlew.bat build --no-daemon

# 生成数据与资源
.\gradlew.bat runData --no-daemon

# 启动开发客户端
.\gradlew.bat runClient --no-daemon
```

构建产物位于 `build/libs/`，文件名由 Gradle 项目名、Minecraft 版本与 `mod_version` 组成。默认项目名取自检出目录；在 `Create-Delight-Core` 目录中构建时为：

```text
build/libs/Create-Delight-Core-1.20.1-<mod_version>.jar
```

向整合包交付时使用上述**非 `-all` 的 reobf jar**，并确保 jar 版本与 `gradle.properties` 中的源码版本一致。

`src/generated/resources/` 是主资源集的一部分，数据生成后需要保留并检查其中的变更。执行 `runData` 如需临时调整 Central Kitchen 依赖，应按 `build.gradle` 中的注释操作，并在完成后恢复配置。

开发约束见 [AGENTS.md](AGENTS.md)，实现说明见 [docs/dev-knowledge](docs/dev-knowledge)。
