# End-Parasitize and Convert All — 1.21.1 NeoForge 版

Minecraft **1.21.1** / **NeoForge 21.1.176** 的源码级移植工程（原工程为 Forge 1.20.1）。

- mod id：`epca`
- 版本：`0.148-1.21.1`
- 许可证：MIT（沿用原工程）
- 原作者：-Yawning喵喵子-（tddddasd）

## 构建

```powershell
$env:JAVA_HOME='D:\dev\jdk\temurin-21'
$env:GRADLE_USER_HOME='D:\gradle-home'
.\gradlew build
```

产物：`build\libs\epca-0.148-1.21.1.jar`

## 运行

```powershell
.\gradlew runClient     # 客户端
.\gradlew runServer     # 服务端（run\eula.txt 已置 true）
.\gradlew runData       # 数据生成
```

## 需要的运行前置 mod

| mod | 版本 | 必需 |
|---|---|---|
| GeckoLib | 4.8.4（1.21.1 NeoForge） | ✅ 必需 |
| Player Animator | 2.0.4（1.21.1 NeoForge） | 可选（缺失时玩家动画不播放） |

`yawning_neko_api`（伤害适应系统）已作为源码内嵌进本 jar，**不需要**单独安装。

## 文档

- 完整移植说明、依赖决策、已知限制、排错记录：[PORTING-REPORT.md](PORTING-REPORT.md)

## 已知限制（摘要）

1. 物理化方块（infected carved pumpkin 物理实体）降级为普通方块 —— `epca_physics` 无 1.21.1 产物，本工程提供 API 兼容层。
2. eej 祭坛联动关闭 —— `eej` 为可选依赖，无 1.21.1 产物。
3. 旧 1.20.1 存档中的物品 NBT / 属性修饰符不迁移（1.21 改为 DataComponents / ResourceLocation id）。
4. 网络协议与 1.20.1 客户端不兼容（NeoForge CustomPacketPayload）。
