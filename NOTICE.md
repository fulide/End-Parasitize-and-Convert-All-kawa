# NOTICE

## 非官方移植声明 / Unofficial port notice

本仓库是社区对 **End-Parasitize and Convert All**（mod id `epca`，作者 **tddddasd / -Yawning喵喵子-**）
的 **非官方** Minecraft **1.21.1 / NeoForge** 源码移植，**与原作者无隶属关系，也未获其背书**。

This repository is an **unofficial** community source port of *End-Parasitize and Convert All*
to Minecraft 1.21.1 / NeoForge. It is **not affiliated with or endorsed by** the original author.

- 上游仓库 / Upstream: <https://github.com/tddddasd/End-Parasitize-and-Convert-All>（分支 `1.20.1-forge`）
- 上游发布 / Upstream releases: <https://modrinth.com/mod/epca>
- 移植基线 / Port baseline: 上游 `0.148-1.20.1`（Modrinth 官方发布包）

> 原作者**没有**发布 1.21.1 版本；本仓库的移植工作、1.21.1 适配改动与新增文件由本仓库维护者完成。
> The upstream author has **not** published a 1.21.1 build; the porting work and 1.21.1-specific
> changes in this repository were made by this repository's maintainer.

## 许可 / License

- 上游代码与资源：**MIT License**, Copyright (c) 2026 tddddasd（见 `LICENSE`）。
- 本移植同样以 **MIT License** 发布，并保留上述版权与许可声明。
- 若你是权利人并认为本仓库有不妥之处，请开 issue，我们会及时处理（包括下架）。

## 第三方内容 / Third-party content

本仓库**不包含**任何原版 Minecraft 资源、官方客户端/服务端 jar 或其他模组的成品 jar。

构建期依赖（不随仓库分发，仅由 Gradle 在构建时下载）：

| 依赖 | 用途 | 许可 |
|---|---|---|
| NeoForge 21.1.176 | 模组加载器 | LGPL-2.1 |
| GeckoLib 4.8.4 (NeoForge 1.21.1) | 实体/物品动画 | MIT |
| Player Animator 2.0.4 (NeoForge 1.21.1) | 玩家动画（**可选依赖**） | MIT |

`org.tdddd.yawning_neko_api`（上游的 jar-in-jar 前置库）已按上游同包名**以源码形式**内置进本工程，
其版权与许可随上游（MIT）。

## 美术资源说明 / Art assets

`assets/epca/textures/item/{flesh_armor_module_i,feeding_module_i,flight_module_i,netherite_module_i}.png`
为移植过程中生成的**占位图标**（上游仓库未提供这四个物品的贴图），有正式美术后可直接替换同名文件。

## 分发注意 / Distribution

编译产物（`dist/*.jar`）仍受 Minecraft EULA 约束：仅可用于拥有正版账号的玩家，且不得单独分发
Minecraft 本体或其资源。Made with ❤️ for the Minecraft modding community.
