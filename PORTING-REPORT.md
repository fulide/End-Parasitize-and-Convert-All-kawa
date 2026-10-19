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
├─ src/main/resources/epca.mixins.json              （JAVA_21，无 refmap）
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
| **Player Animator** | 1.2.x forge | `dev.kosmx.player-anim:player-animation-lib-forge:2.0.4+1.21.1` | ✅ 官方 NeoForge 版；API 2.x 差异已适配（`playAnimation()` 等） |
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
