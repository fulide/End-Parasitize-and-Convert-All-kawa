# End-Parasitize and Convert All — NeoForge 1.21.1 移植报告

> 源工程：`tddddasd/End-Parasitize-and-Convert-All`（分支 `1.20.1-forge`，mod id `epca`，License MIT）
> 目标：**Minecraft 1.21.1 / NeoForge 21.1.176 / Java 21 / ModDevGradle 2.0.91**
> 工程目录：`D:\dev\projects\epca-1.21.1`
> 产物：`build\libs\epca-0.148-1.21.1.jar`（28.6 MB，2500+ 条目）

---

## 1. 结论（先看这里）

| 验证项 | 结果 |
|---|---|
| `gradlew compileJava` | ✅ 0 error |
| `gradlew build`（clean） | ✅ BUILD SUCCESSFUL |
| 服务端启动 `gradlew runServer` | ✅ `Done (2.835s)!`，**0 数据包解析错误**（1296 配方 / 1402 进度正常加载） |
| 客户端启动 `gradlew runClient` | ✅ 标题界面正常，`Sound engine started`，`[epca-araya] slash shader loaded`，**0 mixin 失败、0 崩溃报告** |
| 无 Player Animator 时启动 | ✅ 已实测（把该依赖从运行期类路径移除后启动，mod list 只剩 epca）：无 `NoClassDefFoundError`，可正常进世界 |
| 兼容性 | GeckoLib **4.8.4 与 4.9.3 均可编译**；`compat` 层消除渲染类 mod 的 mixin 冲突（见 §7.9/§7.10） |
| 代码规模 | 732 个 .java / 约 10.6 万行；1330 个资源文件 |

**这是源码级移植**，不是套壳或重写：全部 698 个原始类逐一迁移，业务逻辑与数值保持原样（差异见 §6）。

---

## 2. 工程结构

```
D:\dev\projects\epca-1.21.1
├─ build.gradle              ModDevGradle 2.0.91 + NeoForge 21.1.176
├─ gradle.properties         minecraft_version=1.21.1 / neo_version=21.1.176
├─ src/main/templates/META-INF/neoforge.mods.toml   （1.21 新格式，MDG 展开）
├─ src/main/resources/META-INF/accesstransformer.cfg（已改为官方名，5 条）
├─ src/main/resources/epca.mixins.json              （服务端/通用，defaultRequire 1）
├─ src/main/resources/epca.client.mixins.json       （客户端，defaultRequire 0，失败降级不崩）
├─ src/main/java/org/tdddd/epca/impl/compat/**      可选依赖隔离层（PlayerAnimSupport / PlayerAnimCompat）
├─ src/main/java/org/tdddd/epca/**                  698 → 707 个类
├─ src/main/java/org/tdddd/yawning_neko_api/**      25 个类（前置库源码内嵌移植）
├─ src/main/java/org/tdddd/epca_physics/**          3 个类（前置库兼容层，见 §5）
├─ libs/                    原始 1.20.1 前置 jar（仅归档，不参与编译）
└─ run/                     runServer/runClient 的工作目录（可删）
```

---

## 3. 依赖处理（本次移植最大的结构性决策）

1.20.1 工程依赖 6 个外部模组，其中 3 个是作者自研、只发布了 Forge 1.20.1 jar。1.21.1 上没有对应产物，因此逐一处理：

| 依赖 | 1.20.1 | 1.21.1 方案 | 状态 |
|---|---|---|---|
| **GeckoLib** | 4.8.4 forge | `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.8.4` | ✅ 官方 1.21.1 版本，API 仅去掉 `.core` 包段 |
| **Player Animator** | 1.2.x forge | `dev.kosmx.player-anim:player-animation-lib-forge:2.0.4+1.21.1` | ✅ 官方 NeoForge 版；**可选依赖**：全部 API 用法收敛到 `compat/PlayerAnimCompat`，缺失时自动跳过（已实测无该 mod 启动成功） |
| **yawning_neko_api** | 1.0.5 jar | **反编译 → 源码内嵌移植**（同包名 25 类） | ✅ 数据驱动系统（伤害适应/最低伤害/实体强化/免疫）完整工作，7 个 datapack 目录全部可加载 |
| **epca_physics** | 1.0.0 jar | **兼容层**（`SubLevel`/`SubLevelRegistry`/`SubLevelBehaviour` 3 类） | ⚠️ 物理化方块特性降级，见 §5 |
| **eej（End-Element-Journey）** | 1.0.0 jar | **可选集成关闭**（`EejCompat.loaded()` 恒 false） | ⚠️ 祭坛联动关闭，见 §5 |
| **Registrate / Jade / Photon / LDLib** | 编译期依赖 | **全部移除** | ℹ️ 源码中实际零引用（Jade 兼容类删除） |

> `yawning_neko_api` 与 `epca` 现在同在**一个 jar** 里（原版就是 jarJar 内嵌），没有新增外部前置。

---

## 4. 已完成的迁移要点

### 4.1 加载器 / API 层（Forge → NeoForge）
- `net.minecraftforge.*` → `net.neoforged.neoforge.*`（eventbus→`net.neoforged.bus.api`，fml→`net.neoforged.fml`，distmarker→`net.neoforged.api.distmarker`）
- `@Mod` 构造注入 `(IEventBus, ModContainer)`；`FMLJavaModLoadingContext` 删除；mixin 配置改为 `neoforge.mods.toml` 的 `[[mixins]]`
- `ForgeRegistries` → `BuiltInRegistries`；`RegistryObject` → `DeferredHolder`；`DeferredRegister.Blocks/Items/DataComponents`
- 能力(Capability) → **数据附件(AttachmentType)**：`epca:lifetime`（存活倒计时）、`epca:shield`（护盾值）、`yawning_neko_api:adaptation_data`
  同时 NeoForge 的**新** capability API 仍用于物品/方块/实体容器（`Capabilities.ItemHandler.*`）
- 网络层整体重写：`SimpleChannel` → **30 个 `CustomPacketPayload` + StreamCodec + `RegisterPayloadHandlersEvent`**；逐包保持原字节布局；客户端专用逻辑隔离在 `ClientPacketHandlers`
- 配置：`ForgeConfigSpec` → `ModConfigSpec`，经 `modContainer.registerConfig` 注册
- 事件全面换名：`LivingHurtEvent`→`LivingIncomingDamageEvent`、`TickEvent.X`→`X.Post`、`LivingEvent.LivingTickEvent`→`EntityTickEvent.Pre`、`SpawnPlacementRegisterEvent`→`RegisterSpawnPlacementsEvent`、`MobSpawnEvent`→`FinalizeSpawnEvent`、GUI overlay→`LayeredDraw.Layer` 等
- `@EventBusSubscriber` 的 `bus = MOD` 全部显式补齐（MOD 总线事件不能挂在 game bus 上）

### 4.2 原版 1.20.1 → 1.21.1
- `ResourceLocation` 构造器私有化 → `fromNamespaceAndPath` / `parse`
- 实体同步数据 → `defineSynchedData(SynchedEntityData.Builder)`
- `setMaxUpStep` → `Attributes.STEP_HEIGHT`；`setSecondsOnFire` → `igniteForSeconds`
- `MobEffect`/`Enchantment`/`Attribute` **Holder 化**（200+ 处）
- `AttributeModifier` 改为 `(ResourceLocation, amount, Operation)`；`MULTIPLY_TOTAL`→`ADD_MULTIPLIED_TOTAL`
- ItemStack NBT 删除 → `DataComponents`（自定义组件 `epca:adaptation_count` 等 4 个）+ `CustomData`
- `ArmorMaterial` 由接口变 record → 注册为 `Holder<ArmorMaterial>`
- `AbstractArrow` 新构造器 + `getDefaultPickupItem()`；`AbstractArrow.setPierceLevel` 私有 → 加 AT
- `canBreatheUnderwater` / `getDimensions` / `getGravity` 变 final → 改用 `getDefaultDimensions` / `getDefaultGravity` / **实体类型标签**
- 方块：`use`→`useItemOn`/`useWithoutItem`、`playerWillDestroy` 返回 `BlockState`、12 个方块补 `codec()`、`StairBlock` 构造器变化、`IPlantable` 删除改 `canSustainPlant`
- 方块实体：`loadAdditional/saveAdditional(CompoundTag, HolderLookup.Provider)`、`ItemStackHandler` 带 provider
- 药水效果 `applyEffectTick` 返回 **boolean**、`isDurationEffectTick`→`shouldApplyEffectTickThisTick`
- 客户端：`ShaderInstance(ResourceProvider,...)`、`VertexConsumer` 全 API（`addVertex/setColor/setUv/setLight`，无 `endVertex`）、`RegisterMenuScreensEvent`、`TextureAtlasStitchedEvent`、GeckoLib 4.8 打包 ARGB 颜色

### 4.3 数据包 / 资源
- 目录单数化：`recipe`、`loot_table`、`advancement`、`structure`、`tags/block|item|entity_type`、`neoforge/biome_modifier`
- `data/forge/tags/items/nuggets/copper` → `data/c/tags/item/nuggets/copper`
- 配方 `result.item` → `result.id`（1.21 ItemStack codec）
- 维度类型 `monster_spawn_light_level` 改为内联 `IntProvider`
- 战利品 `minecraft:set_nbt`（1.20.5 已删）→ `minecraft:set_components` + `custom_data`
- 模型 `"loader": "forge:separate_transforms"` → `neoforge:separate_transforms`（16 个长矛模型）
- `pack.mcmeta` pack_format 15 → **34**
- 新增标签：`minecraft:can_breathe_under_water`（8 个实体）、`minecraft:enchantable/*`（长矛/活体护甲盒的附魔可应用性——1.21 删除 `canApplyAtEnchantingTable` 后由标签驱动）
- 着色器 `fog.glsl` 签名变化：`corruption.vsh` 改 `fog_distance(vec3,int)`

### 4.4 访问转换器（AT）
原文件是 SRG 名（1.21 下**完全无效**），已改为官方名：
```
CreativeModeTab displayItems / displayItemsSearchTab
CreativeModeInventoryScreen$ItemPickerMenu getRowIndexForScroll(F)I
CreativeModeInventoryScreen selectedTab
AbstractArrow setPierceLevel(B)V
```

---

## 5. 已知限制（与原版 1.20.1 的行为差异）

| # | 项目 | 影响 | 原因 |
|---|---|---|---|
| 1 | **物理化方块失效** | 被感染的雕刻南瓜不再"变成会走的实体方块"，退化为普通方块 | `epca_physics` 无 1.21.1 产物，本次只做了 API 兼容层（`SubLevel` 值对象 + 行为注册表），未重写物理引擎/mixin/同步 |
| 2 | **eej 祭坛联动关闭** | 无祭坛献祭、无祭坛 2× 转化加成、无击杀法杖祭坛诊断 | `eej` 为可选依赖，无 1.21.1 产物；`EejCompat.loaded()` 恒 false |
| 3 | **旧存档不迁移** | 物品 NBT（活体护甲盒状态/生物质/模块）、属性修饰符 UUID、能力数据不随存档迁移 | 1.21 全面改为 DataComponents / ResourceLocation id；新存档无影响 |
| 4 | **网络不兼容 1.20.1 客户端** | 协议从"单通道 + int 判别"改为"每包一个 ResourceLocation" | NeoForge 1.21.1 强制 `CustomPacketPayload` |
| 5 | **Jade 提示移除** | 不再显示生物适应度/击杀数悬浮提示 | 可选联动；Jade 1.21.1 API 差异大，为控制风险删除 |
| 6 | GeckoLib 4.8 渲染细节 | 若干旧 5 参 `applyRotations` 覆盖在 4.8 中本就不会被调用（本次改为 6 参，属修复） | 上游 API 变化 |
| 7 | **不装 Player Animator 时无玩家动画** | 长矛突刺、击杀法杖的第三人称/第一人称动画不播放；其余玩法正常 | 该依赖改为 **optional**，缺失时 `PlayerAnimSupport.available() == false` 直接跳过（见 §7.10） |
| 8 | 渲染类 mod 下部分客户端特效可能降级 | 例如别的 mod 占用了同一渲染调用时，本 mod 的着色层/闪电染色会**静默不生效**（不再崩溃） | 客户端 mixin 配置 `defaultRequire: 0`，失败即降级 |
| 9 | 4 个模块物品使用**占位图标** | `flesh_armor_module_i` / `feeding_module_i` / `flight_module_i` / `netherite_module_i` 在源仓库里没有任何模型/贴图，本次生成了 16×16 占位图 | 上游资源缺失；有正式美术后直接替换 `assets/epca/textures/item/<id>.png` 即可 |
| 10 | `liquid_biomass` / `magic_reflector` 等为**孤儿资源** | 代码里已无对应方块，永远不会被加载（无报错） | 历史遗留，可安全删除 |

> 以上均已在源码注释中标注；不影响主玩法（感染、转化、进化、巢穴首领、器官系统）。

---

## 6. 构建与运行

```powershell
$env:JAVA_HOME='D:\dev\jdk\temurin-21'
$env:GRADLE_USER_HOME='D:\gradle-home'
cd D:\dev\projects\epca-1.21.1

.\gradlew build        # 产物 build\libs\epca-0.148-1.21.1.jar
.\gradlew runServer    # 服务端（run/eula.txt 已置 true）
.\gradlew runClient    # 客户端
.\gradlew runData      # 数据生成（输出到 src/generated/resources）
```

> 首次构建依赖 `D:\gradle-home\init.gradle` 把 `maven.neoforged.net` 重定向到 `neoforged.forgecdn.net`（见《环境搭建清单》）。

**安装**：把 jar 丢进 `mods/`，另需安装 **GeckoLib 4.8.4（1.21.1 NeoForge）**；Player Animator 2.0.4 为可选（缺失时动画不播放，不影响启动）。

---

## 7. 移植过程中的关键排错记录（供后续参考）

1. **`GeckoLib 4.8.4` 的 1.21.1 版本删除了 `.core` 包段** —— 545 处 import 需整体平移（`core.animation.*` → `animation.*`，`core.object.PlayState` → `animation.PlayState`，`core.animatable.model.CoreGeoBone` → `cache.object.GeoBone`）。
2. **`player-animation-lib` 有两个 artifact**：`player-animation-lib`（Architectury common，intermediary 名，**编不过**）和 `player-animation-lib-forge`（Mojang 名，正确）。必须用后者。
3. **`@EventBusSubscriber` 的 `bus` 参数不能省** —— 早期机械替换把所有 `bus = MOD` 删掉，导致 `GatherDataEvent` 等 MOD 总线事件被挂到 game bus，启动直接崩。
4. **自动订阅类要求处理方法是 `static`** —— 3 个类因此改造；另有 3 个类其实没有 `@SubscribeEvent`，注解本身要删。
5. **`PoseStack.popPose()` 在 1.21.1 的 `ItemRenderer#render` 里只剩一处**（原 ordinal=1 失效）→ 去掉 ordinal 才注入成功；这是客户端唯一一个启动期 mixin 崩溃。
6. **`ItemStack.STRICT_CODEC` 用 `id` 而不是 `item`** —— 配方 `result` 必须跟着改，否则整条配方静默不加载。
7. **维度类型 `monster_spawn_light_level` 的 `IntProvider` 不再有 `value` 包裹层**，否则 `dimension_type` 注册失败、服务端起不来。
8. **`minecraft:set_nbt` 战利品函数在 1.20.5 已删除**，必须换 `set_components` + `custom_data`（本次只有 `swallow_cyst` 一处）。
9. **🔴 混装渲染类 mod 时的 `@Redirect` 冲突（真机崩溃已修复）**
   现象：在装有 `acceleratedrendering` / `sodium` / `lightspeed` 等渲染优化 mod 的整合包里启动即崩：
   ```
   @Redirect conflict. Skipping epca.mixins.json:client.LivingEntityRendererTintMixin ... 
   InjectionError: Critical injection failure: Redirector epca$renderTintedModel ... (0/1) succeeded
   ```
   根因：`@Redirect` 对同一处调用是**独占**的，别的 mod 先占位后 Mixin 直接跳过我们的注入，而 `defaultRequire: 1` 把"跳过"升级成致命错误。
   修复：
   - `LivingEntityRendererTintMixin` / `LightningBoltRendererMixin` / `MerchantOfferMixin` 的 `@Redirect` → **`@WrapOperation`（MixinExtras，随 NeoForge 内置）**，可与其他 redirect / wrap 叠加，不再互斥；
   - 客户端 mixin 拆到独立的 `epca.client.mixins.json`，`defaultRequire: 0`（渲染路径被优化 mod 改写时**降级而不是崩游戏**）；服务端 mixin 仍保持 `defaultRequire: 1`（玩法核心必须生效）；
   - 顺带给 `ItemRendererShaderLayerMixin` 去掉失效的 `ordinal = 1`（见第 5 条）。
10. **🔴 未安装 Player Animator 时的 `NoClassDefFoundError`（真机崩溃已修复）**
    现象：`Failed to register automatic subscribers. ModID: epca → NoClassDefFoundError: dev/kosmx/playerAnim/api/layered/IAnimation`。
    根因：`events/PlayerAnimator` 是 `@EventBusSubscriber`（FML 启动时会 `Class.forName` 它），而它的**方法签名**里就有 PAL 类型 ⇒ 没装该 mod 时类加载失败 ⇒ 整个 mod 构造失败。
    修复：把所有 PAL 用法收敛到 `impl/compat/PlayerAnimCompat`（唯一引用 `dev.kosmx.*` 的类），并新增不含任何 PAL 引用的探针 `PlayerAnimSupport.available()`；
    `@EventBusSubscriber` 的 `events/PlayerAnimator` 删除，改为 `ClientSetup` 里 `if (PlayerAnimSupport.available()) PlayerAnimCompat.registerFactories();`。
    现在**不装 Player Animator 也能正常启动**（只是长矛/击杀法杖的玩家动画不播放），`neoforge.mods.toml` 里该依赖为 `optional`。
11. **🔴 材质与语言"全部丢失"（真机现象已修复）—— 根因是 `src/generated/resources` 没被复制**
    现象：创造模式标签页标题显示 `itemGroup.epca.main_tab`、物品提示 `item.epca.parasite_viscera`，大量物品是紫黑格。
    根因：原工程把 **datagen 产物提交在 `src/generated/resources`**（671 个文件：185 个物品模型 + 150 个方块模型 + 87 个 blockstate + 2 个语言文件 + 12 个粒子定义 + 配方/进度/标签），
    首次移植只复制了 `src/main/{java,resources}`，于是：
    - 语言文件整个缺失（`assets/epca/lang/*.json` 根本不存在）⇒ 所有键显示原文；
    - 手写模型只有 82 个（长矛等），其余 185 个物品模型缺失 ⇒ 紫黑格；
    - 87 个 blockstate、150 个方块模型、12 个粒子定义缺失 ⇒ 方块无模型 + 粒子创建 NPE（`SplashiParticle` 的 `this.sprites is null`）。
    修复：完整复制 `src/generated/resources`（排除 `.cache`），并做 1.21 适配：
    - 目录单数化：`advancements→advancement`、`recipes→recipe`、`tags/blocks|items|entity_types→tags/block|item|entity_type`、`forge/tags/items→c/tags/item`；
    - 结果字段：`"result": {"item": ...}` → `{"id": ...}`，**并且 12 个熔炼/高炉配方是裸字符串 `"result": "epca:copper_nugget"`，1.21 非法** → 统一改成 `{"id": ...}`；
    - 条件：`"conditions": [{"type":"forge:mod_loaded"...}]` → `"neoforge:conditions": [{"type":"neoforge:mod_loaded"...}]`（仅 113 个 eej 灵魂火配方）；
    - `build.gradle` 给 `processResources` 加 `duplicatesStrategy = EXCLUDE`（main 与 generated 有意重叠）。
12. **🔴 缺失/损坏的资源（已补）**
    - `epca:acid_solution` 没有 blockstate 文件 → 每个 `level=0..15` 都报 "missing model for variant"，补齐 16 变体 blockstate；
    - 该方块的模型只有 `{"parent":"minecraft:block/cube_all"}`（无贴图，非法）→ 改为流体式 `{"textures":{"particle":"epca:block/acid"}}`；
    - 4 个模块物品（`flesh_armor_module_i` / `feeding_module_i` / `flight_module_i` / `netherite_module_i`）在源仓库里**既无模型也无贴图** → 本次生成占位图标 + 模型（**占位美术**，见 §5.9）；
    - 91 个音效缺 `subtitles.epca.*` 翻译键（每个都刷一条 ERROR）→ 自动补英文/中文键；
    - 2 个生物质刷怪蛋缺 `item.epca.*` 语言键 → 补上。

14. **🔴 方块掉落"全部变成掉自己"（真机问题已修复）—— 附魔判定结构在 1.21 改名**
    现象：`epca:infested_heavy_copper_ore` 等矿石被挖后掉自身，而不是 `epca:infested_raw_copper`；用**任何**工具（含空手）都一样。
    根因：1.20.1 的 `match_tool` 谓词写法是
    ```json
    "predicate": { "enchantments": [ { "enchantment": "minecraft:silk_touch", "levels": { "min": 1 } } ] }
    ```
    而 1.21.1 的 `ItemPredicate` 已经没有 `enchantments` 字段，附魔判定搬进了 `predicates` 子谓词，内层字段名也从 `enchantment` 改成 `enchantments`：
    ```json
    "predicate": { "predicates": { "minecraft:enchantments": [ { "enchantments": "minecraft:silk_touch", "levels": { "min": 1 } } ] } }
    ```
    Mojang 的 codec 会**静默忽略未知字段** ⇒ 整个谓词变成"空谓词" ⇒ `match_tool` **匹配任意工具** ⇒ `alternatives` 里的"精准采集"分支永远命中 ⇒ 所有方块都掉自己。
    影响面：**95 个战利品表**（几乎所有方块的精准采集分支，共 96 处 silk_touch）。
    修复：写脚本按 1.21 结构重写（Python，保留数组结构），并用原版 `data/minecraft/loot_table/blocks/coal_ore.json` 的谓词做结构比对 —— **逐字段完全一致**。
    注意：`apply_bonus` 里的 `"enchantment": "minecraft:fortune"` 在 1.21 **没有变**，脚本特意跳过了它（否则时运失效）。
13. **🔴 日志刷屏（真机上把日志刷到 2.9 MB）** —— 实体移植时留下了 `[climb-diag][spider]` 临时诊断，每个蜘蛛每 10 tick 打一条 INFO。已全部删除（`InfestedSpider` / `WalkingSpiderHead`）。

---

## 8. 后续可做（按优先级）

1. **移植 `epca_physics`**（46 类：SubLevel 物理、碰撞 mixin、同步、渲染）→ 恢复物理化方块。
2. **移植 `eej`**（80 类）→ 恢复祭坛联动。
3. **为 GUI 全面补 `@Override`** 并按 1.20.1 参考树 diff，排查"签名不再是覆盖"的死方法（本次已过编译期，但参数含义类问题（如 `mouseScrolled` 横向/纵向）仍需人工核对）。
4. **跑一次 `runData`** 校验 datagen 产出的资源与手写资源是否一致（尤其语言文件）。
5. **网络包对称性自测**（编码残留 == 0）与两端注册表一致性检查。
6. 有条件时在真实客户端 + GeckoLib/PlayerAnimator 环境下做一次玩法回归。

---

*报告生成时间：2026-10-19；构建环境：Temurin 21.0.12.1、Gradle 8.14.2、NeoForge 21.1.176。*
