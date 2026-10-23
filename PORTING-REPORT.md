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

15. **🔴 仓库里缺了 2 个物品标签（已从官方 1.20.1 发布包补齐）**
    做法：从 Modrinth 下载官方 `0.148-1.20.1` jar，逐条比对 `data/`+`assets/` 的 1219 个 JSON 与本地资源。
    结论：**配方集合完全一致（139 = 139）**——`living_chestplate` 之类本来就没有配方（活体护甲是右键 **Living Armor Box** 整套穿戴，只有盒子可合成），不是移植丢失。
    但发现仓库确实少提交了 2 个 tag 文件（发布 jar 里有）：
    - `forge:enchanting_fuels`（= `epca:infested_lapis_lazuli`）→ 已补为 `data/c/tags/item/enchanting_fuels.json`
    - `forge:infested_flesh`（= `infested_flesh` / `weird_minced_flesh` / `diseased_heart`）→ 已补为 `data/c/tags/item/infested_flesh.json`
    第 2 个尤其重要：`erosion_clock` 配方用的就是 `c:infested_flesh`，**标签不存在时配方永远匹配不上（静默失效）**。

16. **🔴 `Holder<MobEffect>` 身份比较导致功能静默失效（已修复）**
    1.21 里效果实例持有的是注册表里的 `Holder`，而我们自己的 `ModEffects.X` 是 `DeferredHolder`（另一个对象），所以
    `instance.getEffect() != ModEffects.COTH` 恒为 `true`、`== this` 恒为 `false`。
    共 5 处：`CothEffectEvents`、`LivingEntityMixin`（×2）、`DeepSneakEffect`、`End……ErosionEffect`（×3）、`EffectApplicationInterceptor`。
    已改为 `.value() != X.get()` 形式 —— 修复前 **COTH（虫巢召唤）的自定义处理、深沉潜行的移除逻辑、末影侵蚀的计时器全部是死代码**。

17. **🔴 附魔台青金石槽不接受 `epca:infested_lapis_lazuli`（已修复）**
    1.21.1 原版 `EnchantmentMenu` 把青金石槽硬编码成匿名 `Slot` 里的 `stack.is(Items.LAPIS_LAZULI)`（源码里就留着
    `// Neo: TODO - replace with the tag` 的注释），所以任何"青金石类"物品都放不进去。
    修复方式：
    - 新增 `EnchantingFuelSlot`（继承 `Slot`），接受**原版青金石**、**`c:enchanting_fuels` 标签内的物品**、以及 `epca:infested_lapis_lazuli`；
    - `EnchantmentMenuMixin` 在菜单构造完成后把 1 号槽替换成它；
    - 同 mixin 用 `@Redirect(require = 0)` 修正 `quickMoveStack` 里的同一个判断，让 **Shift+左键**也能把虫染青金石送进燃料槽。
    坑：`@Inject(method = "<init>")` 只会匹配到**第一个**构造器（`(int, Inventory)` 那个委托构造器），
    服务端实际用的是 `(int, Inventory, ContainerLevelAccess)`，必须写完整描述符才会生效。
    验证（专用服务端 + RCON 自检命令）：`slotClass=EnchantingFuelSlot`、`acceptsInfested=true`、`acceptsLapis=true`、
    `rejectsDirt=true`、`shiftClickToFuel=true`（6 个全部进入 1 号槽）。菜单其余逻辑（费用计算、`clickMenuButton` 的消耗、
    `getGoldCount()`）只清点数量、不校验物品 id，因此不需要额外 hook。

18. **🔴 寄巢笔记（`epca:epca_note`）GUI 被整层模糊（已修复）**
    1.21 把 `Screen#renderBackground` 从"1.20.1 的纯半透明渐变"换成了**高斯模糊 + 菜单背景贴图**
    （`renderBlurredBackground` + `renderMenuBackground`），而 `Screen#render` 会先画背景再画控件。
    这两个 GUI 的 `render` 是**先画书页/面板、最后才调用 `super.render`**（1.20.1 时代也这么写，但那时背景只是一层
    半透明渐变，盖上去不明显），于是 1.21 里模糊和那张棕色菜单贴图被**糊在书页上面** → 用户看到"打开笔记有一层模糊"。
    修复：
    - `EPCANoteScreen` 覆写 `renderBackground` 为 1.20.1 的等价实现（`fillGradient(0,0,w,h,0xC0101010,0xD0101010)`，
      非游戏内退回菜单背景），在 `render` 开头脑后画一次，然后**不再调用 `super.render`**，改为自己遍历
      `renderables` 渲染按钮（`Screen#render` 的实现就是"背景 + 遍历控件"，只跳过背景那一半）；
      同时补发 `ScreenEvent.BackgroundRendered`，避免依赖该事件的 mod 失效。
    - `NestLeaderOrganScreen` 同样在开头画背景、结尾 `super.render` 会二次糊面板；改为只遍历 `renderables`。

19. **🔴 部分感染生物"血量归 0 却杀不死、/kill 无效"（已修复）**
    复现方式：在专用服务端连杀 12 只 `epca:infested_zombie`，其中 **7 只触发了"假死"**（`startFakeDeath`：
    `setInvulnerable(true)` + `setNoAi(true)` + 血量压到 `0.02` + `Pose.DYING`，30 tick 后 `discard()`）——
    概率性正是 `fakeDeathChance = 40`。假死期间 `AbstractEpcaEntity#hurt` 直接 `return false`，所以 `/kill` 无效；
    一旦因为任何原因（存档/区块卸载时正处于假死、死亡动画被取消、`LivingDeathEvent` 被 adaption 取消…）
    停在"0 血但没被移除"的状态，原版 `LivingEntity#hurt` 会在 `isDeadOrDying()` 处提前返回，
    **任何伤害（含 /kill）都再也打不进去**，于是永久僵死。
    修复（三处）：
    - `AbstractEpcaEntity#hurt`/`AbstractInfestedEntity#hurt`：任何 `BYPASSES_INVULNERABILITY` 伤害（`/kill`、虚空等）
      一律放行；假死中收到这类伤害会**立即结束假死并真正死亡**；0 血僵死态直接 `remove(KILLED)`。
    - `AbstractEpcaEntity#die`/`AbstractInfestedEntity#die`：BYPASS 伤害**跳过**假死随机与适应度复活，保证 `/kill` 必杀。
    - `tick()` 增加看门狗：0 血且未移除、非假死状态下超过 60 tick 直接 `remove(KILLED)`；
      并把假死状态写进 NBT（`EpcaFakeDeath` / `EpcaFakeDeathTicks`），读档后**继续跑完假死**而不是留下 NoAI 的残骸；
      对旧存档里"NoAI + 0.02 血且无假死标记"的残骸，读档时自动解除 NoAI 并把血量补到 1。
    验证（专用服务端 + RCON）：假死中 `/kill` → `removed=true`；0 血僵死态 `/kill` → `removed=true`；
    12 连杀全部正常死亡（不再残留假死）。

20. **🔴 抛射物在区块卸载/存档时报 `Cannot encode empty ItemStack`（已修复）**
    日志特征：`entity.epca.biomass_egg has thrown an exception trying to write state ... Cannot encode empty ItemStack`
    → `AbstractArrow.addAdditionalSaveData`（`AbstractArrow.java:529`）。
    根因：`BiomassEgg` 与 `BoneArrow` 都把"拾取物"写成 `ItemStack.EMPTY`（`getDefaultPickupItem()` 返回空 + 构造器传空）。
    1.20.1 里 `ItemStack#save` 允许空堆栈（写成 `{}`），**1.21 改成抛异常**，于是这两个实体只要被存档就会失败并丢失，
    每次区块卸载都会刷一次 ERROR。
    修复（两个类同样处理）：
    - 构造器与 `getDefaultPickupItem()` / `getPickupItem()` 改为返回非空占位堆栈（蛋 → `minecraft:egg`，骨箭 → `minecraft:arrow`）；
      这两个实体本身 `pickup = CREATIVE_ONLY`，占位物不会被玩家拿到；
    - 覆写 `addAdditionalSaveData`，若拾取堆栈为空则先补回占位物，**保证任何路径都不会把空堆栈交给序列化器**。
    验证（RCON）：`biomass_egg: OK item={count:1,id:"minecraft:egg"}` / `bone_arrow: OK item={count:1,id:"minecraft:arrow"}`
    / 无射手构造路径同样 OK；服务端日志不再出现 `Cannot encode` / `EntityStorage` 异常。

21. **🔴 三个结构里用了可选依赖 `eej` 的方块（已替换为原版方块）**
    `abandoned_cake_workshop` / `_sand` / `_redsand` 三个 `.nbt` 结构模板的调色板里含有
    `eej:packed_mud_altar_stone` 与 `eej:packed_mud_pedestal` —— `eej`（祭坛 mod）没有 1.21.1 产物，
    这两个方块不存在，生成时那部分会变成空洞；被替换掉的 `eej:packed_mud_pedestal` 还带着
    方块实体 NBT（`id` + 空 inventory + `filterData`），放着不管只会在放置时刷 "mismatched block entity"。
    处理（用脚本直接改写 NBT，见 `nbt.py` / `fix_structure_blocks.py`）：
    - `eej:packed_mud_altar_stone` → `minecraft:packed_mud`（结构里本来就有这个方块，风格一致）
    - `eej:packed_mud_pedestal`    → `minecraft:mud_bricks`（同样本来就在调色板里）
    - 并把指向这两个状态的方块条目上的 `nbt` 键删除（原版方块没有方块实体）
    处理结果：三个结构调色板 **只剩 `minecraft:` 命名空间**，方块总数不变（5082），
    state 索引全部合法，文件从 16.4/16.3/16.1 KB 降到 14.7/14.5/14.7 KB。
    验证（专用服务端 RCON，`/place template` 直接放模板而不是 jigsaw 结构）：
    - `abandoned_cake_workshop` → `mud_bricks` 175 块、`packed_mud` 39 块，与 NBT 统计一致；
    - `_sand` → `mud_bricks` 175 块；`_redsand` → `mud_bricks` 174 块；
    - 服务端日志 **0 条 ERROR**（此前 `eej` 方块缺失时会报未知方块/解析失败）。

22. **🔴 创造模式投出的长矛无法拾回（已修复）**
    8 个 `Thrown*Spear`（木/石/铁/金/钻/下界合金/铜/燧石）的 `playerTouch` 里有这么一句：
    ```java
    if (this.wasCreativeThrower) return;
    ```
    创造模式投掷不消耗物品，于是落地后的长矛既不进背包、也不会被移除 —— **永远躺在原地**，
    玩家表现为"捡不起来"。
    修复：改为按原版 `Pickup.CREATIVE_ONLY` 的语义处理——
    - 创造模式投出的长矛：创造模式玩家触碰 → 播拾取音效并 `discard()`（物品本来就没消耗，不复制）；
      非创造玩家触碰 → 不能拿（避免刷长矛）。
    - 生存模式投出的长矛：保持原逻辑，触碰入手（背包满则留在地上）。
    验证（专用服务端 RCON 自检）：
    `creativeThrow/creativePlayer removed=true`、`creativeThrow/survivalPlayer removed=false`、
    `survivalThrow/survivalPlayer removed=true`、`survivalThrow/creativePlayer removed=true`。

23. **🟢 效果替身：装了 LSO / sa_core 时改用它们的效果（新增兼容层）**
    需求：整合包里同时装了 `legendarysurvivaloverhaul`（内伤）和 `sa_core`（流血）时，
    不应该再叠加本模组同语义的 `epca:viral` / `epca:bleeding`。
    实现：新增 `org.tdddd.epca.impl.compat.EffectCompat`——
    - `legendarysurvivaloverhaul` 已加载 ⇒ `epca:viral` 换成 `legendarysurvivaloverhaul:vulnerability`；
    - `sa_core` 已加载 ⇒ `epca:bleeding` 换成 `sa_core:bleeding`；
    - 只按 `ResourceLocation` 去 `BuiltInRegistries.MOB_EFFECT.getHolder(...)` 查，**不编译依赖**；
      查不到就静默回落并打一条 WARN。
    覆盖点：
    - 所有 `new MobEffectInstance(ModEffects.BLEEDING/VIRAL, …)` 改为 `EffectCompat.*Instance(...)`（9 个文件）；
    - 叠层判定（`Fins`/`Ripper` 的 `getEffect(...)`）改用 `activeBleeding/activeViral`，
      旧存档里残留的本体效果仍然认；
    - `entity_attack_effects/*.json` 里的 `epca:bleeding` 由 `mapDataEffectId()` 在**运行时**重映射，
      数据文件不用改两份；
    - `entity_immunity_effects/*.json` 里虫族对 `epca:viral` 的免疫由 `immunityAlias()` 继承给替身，
      否则装了 LSO 后虫族会突然吃"内伤"；
    - 首领免疫名单（`NestLeaderHandler`）用 `EffectCompat.isViral()` 同时认本体与替身。
    **刻意不改的地方**：`LivingHurtEventHandler` / `ViralEffectEventHandler` / `WaterInteractionHandler`
    只认 `epca:viral`——替身有自己的易伤实现，如果这里也认就会双倍加伤。
    验证（专用服务端 + 强制替身 id）：`viralSub/bleedSub=true`、`bleedApplied=替身`、`bleedStacked amp=1`、
    `mapBleeding/mapViral` 正确重映射、`aliasBleeding/aliasViral` 正确回指、非免疫生物（牛）两个效果都挂上、
    免疫的虫族替身被正确拦下。

24. **🔴 感染生物"倒地→爆炸→倒地"多播一次死亡动画（已修复）**
    现象：`epca:infested_zombie` 等假死生物，观感是"倒地→爆炸→倒地"，比预期多一次倒地。
    定位（服务端逐 tick 日志）：假死流程本身只有一次（`die()` → 30 tick → `onFakeDeathEnd` → 爆开 → `discard()`），
    所以问题在**动画时长与假死时长完全相等**：
    - `assets/epca/animations/infested_zombie.animation.json` 里 `dead` 动画长度 = **1.5 秒**；
    - `fakeDeathDuration` = **30 tick = 1.5 秒**；
    - 而动画用的是 `RawAnimation.begin().thenLoop("dead")`（循环）。
    ⇒ 第 30 tick 动画正好播放完并**从头重播**（重新倒地），同一 tick 触发爆开、随后实体被移除，
    于是看到"倒地 → 爆炸 → 又倒一次"。
    修复：把**假死分支**里的 `thenLoop("dead…")` 全部改成 `thenPlay("dead…")`（一次性、停在最后一帧）——
    共 16 个实体（Bat/Chicken/Cow/Drowned×2/Fox/Husk/Pig/Pillager/Sheep/Skeleton/Villager/Vindicator/Wolf/
    Zombie/ZombieVillager/ReshapeLongarms）。`InfestedEnderman`、`InfestedSlime*`、`InfestedSpider`、
    `WalkingSpiderHead` 本来就是 `thenPlay`/triggerable，不用改。
    ⚠️ 没有改动 `isExploding()` 分支（`LargeIncompleteForm`/`ReshapeYelloweye`）和方块实体
    `SwallowCystBlockEntity` 的 `dead_idle`——那不是假死动画。

25. **🔴 让虫族"像原版敌对生物一样守规矩"（和平难度 / /kill / 破坏方块）**
    用户反馈：和平难度下照样冒虫、还带仇恨（只是打不出伤害）；`/kill` 把血打到 0 却清不掉；
    蜂巢方块刷出大量 ripper 拖低 TPS。排查后确认这是**三处"越界"**叠加：
    | 越界点 | 原因 |
    |---|---|
    | 和平难度不消失 | `AbstractEpcaEntity` 没覆写 `shouldDespawnInPeaceful()`（原版 `Monster` 里是 `true`），只有 4 个类单独覆写过；且 `MobMixin.checkDespawn` 对"受保护"生物**无条件 cancel**，把原版的和平清除也一起吃掉了 |
    | 和平难度照常生成 | 61 条 `SpawnPlacements` 用的是自定义 `checkXxxSpawnRules`，绕过了 `Monster.checkMonsterSpawnRules` 里的 `difficulty != PEACEFUL`；方块刷怪器（蜂巢节点、信标、感染石、玩家死亡转化）也完全不看难度与 `doMobSpawning` |
    | `/kill` 清不掉 | 50 多个实体的 `hurt()` 会先走 `IParasite#onHurt` → `handleParasiteDamage`，它在"伤害适应无敌"时**直接返回 0**，`/kill` 的 `genericKill` 也被抹成 0；`AbstractReshapeEntity#hurt` 还额外用 `isInvulnerable()` 挡掉了穿透伤害 |
    | 拆玩家火把 | `EntityBreakUtils.tryBreakLightSources` 直接 `destroyBlock`，**不看 `mobGriefing`** |
    修复：
    - 新增 `utils/DifficultyGuards`：`isPeaceful` / `canSpawnHostiles`（和平或关 `doMobSpawning` 都不刷）/ `peacefulGuard(SpawnPredicate)`；
    - `AbstractEpcaEntity#shouldDespawnInPeaceful()` → `true`；`MobMixin#checkDespawn` 在和平难度直接放行给原版处理；
    - **61 条**刷怪规则统一套上 `peacefulGuard(...)`；蜂巢节点（curbug + ripper 成熟）、`StageI/II Beckon` 生物质、6 个感染石方块、玩家死亡转化全部加 `canSpawnHostiles` 判定；
    - `IParasite#handleParasiteDamage` 遇到 `BYPASSES_INVULNERABILITY` 直接返回原始伤害；`AbstractEpcaEntity#hurt` 对穿透伤害**跳过整条 parasite 伤害管线**（含 `shouldIgnoreDamageFrom` 与 `onHurt`），并在 0 血时直接 `remove(KILLED)`；`AbstractReshapeEntity#hurt` 同样放行穿透伤害；
    - `EntityBreakUtils` 受 `mobGriefing` 约束。
    验证（专用服务端 RCON）：
    - 普通难度召唤感染僵尸 → `/difficulty peaceful` → 3 秒后 `execute if entity` **Test failed**（已清除）；
    - 召唤 9 只 ripper → `/kill @e[type=epca:ripper]` → `Killed 9 entities`，3 秒后 **Test failed**（无残留）；
    - `mobGriefing false` 时火把 4 秒后仍在（Test passed）；`mobGriefing true` 时火把被拆（Test failed，证明功能未被写死关掉）。

## 8. 后续可做（按优先级）

1. **移植 `epca_physics`**（46 类：SubLevel 物理、碰撞 mixin、同步、渲染）→ 恢复物理化方块。
2. **移植 `eej`**（80 类）→ 恢复祭坛联动。
3. **为 GUI 全面补 `@Override`** 并按 1.20.1 参考树 diff，排查"签名不再是覆盖"的死方法（本次已过编译期，但参数含义类问题（如 `mouseScrolled` 横向/纵向）仍需人工核对）。
4. **跑一次 `runData`** 校验 datagen 产出的资源与手写资源是否一致（尤其语言文件）。
5. **网络包对称性自测**（编码残留 == 0）与两端注册表一致性检查。
6. 有条件时在真实客户端 + GeckoLib/PlayerAnimator 环境下做一次玩法回归。

---

*报告生成时间：2026-10-19；构建环境：Temurin 21.0.12.1、Gradle 8.14.2、NeoForge 21.1.176。*
