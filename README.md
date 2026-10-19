# End-Parasitize and Convert All — 1.21.1 NeoForge 版

Minecraft **1.21.1** / **NeoForge 21.1.176** 的源码级移植工程（原工程为 Forge 1.20.1）。

- mod id：`epca`
- 版本：`0.148-1.21.1`
- 许可证：MIT（沿用上游，见 [LICENSE](LICENSE)）
- 原作者：-Yawning喵喵子-（[tddddasd](https://github.com/tddddasd)）
- **非官方移植**，与原作者无隶属关系 —— 详见 [NOTICE.md](NOTICE.md)

## 直接下载

- 成品：`dist/epca-0.148-1.21.1.jar`（也可在 [Releases](../../releases) 下载）
- 校验：`dist/SHA256SUMS.txt`

把 jar 放进 `mods/`，并安装 **GeckoLib 4.8.4+（1.21.1 NeoForge）** 即可。

## 需要的前置 mod

| mod | 版本 | 必需 |
|---|---|---|
| GeckoLib | 4.8.4+（1.21.1 NeoForge；4.8.4 / 4.9.3 均已验证编译） | ✅ 必需 |
| Player Animator | 2.0.4（1.21.1 NeoForge） | 可选（缺失时只有长矛/击杀法杖的玩家动画不播放，其余正常；已实测无它也启动） |

`yawning_neko_api`（伤害适应系统）已作为源码内嵌进本 jar，**不需要**单独安装。

## 构建

需要 JDK 21（`JAVA_HOME` 指向它）。

```bash
./gradlew build
```

产物：`build/libs/epca-0.148-1.21.1.jar`

> 上游仓库把 datagen 产物提交在 `src/generated/resources`，本仓库保持同样结构；
> 如果你只拷贝 `src/main` 会丢语言文件与大量模型/粒子定义（血泪教训，见移植报告 §7.11）。

## 运行 / 开发

```bash
./gradlew runClient     # 客户端
./gradlew runServer     # 服务端（需 run/eula.txt）
./gradlew runData       # 数据生成
```

## 仓库结构

```
src/main/java/org/tdddd/epca/**              移植后的模组源码（698 → 707 类）
src/main/java/org/tdddd/yawning_neko_api/**  上游前置库的源码内嵌移植（25 类）
src/main/java/org/tdddd/epca_physics/**      epca_physics 兼容层（3 类）
src/main/resources/**                        手写资源 + mixin/AT 配置
src/generated/resources/**                   datagen 产物（语言、模型、blockstate、粒子、配方…）
src/main/templates/META-INF/neoforge.mods.toml  1.21 模组元数据模板
dist/                                        成品 jar + SHA-256
PORTING-REPORT.md                            完整移植报告（依赖决策/已知限制/排错记录）
```

## 文档

- 完整移植说明、依赖决策、已知限制、逐条排错记录：[PORTING-REPORT.md](PORTING-REPORT.md)
- 非官方声明与第三方内容：[NOTICE.md](NOTICE.md)

## 已知限制（摘要）

1. 物理化方块（infected carved pumpkin 物理实体）降级为普通方块 —— `epca_physics` 无 1.21.1 产物，本工程提供 API 兼容层。
2. eej 祭坛联动关闭 —— `eej` 为可选依赖，无 1.21.1 产物。
3. 旧 1.20.1 存档中的物品 NBT / 属性修饰符不迁移（1.21 改为 DataComponents / ResourceLocation id）。
4. 网络协议与 1.20.1 客户端不兼容（NeoForge CustomPacketPayload）。
5. 4 个模块物品使用占位图标（上游无贴图）。
