# AE2 闪电科技 - 非官方 Forge 1.20.1 移植版

[English](README.md)

> [!IMPORTANT]
> 本仓库是 AE2 Lightning Tech 非官方 Minecraft 1.20.1 Forge 移植版的
> **唯一源码仓库、问题跟踪器与发行位置**。本项目不由原模组作者或
> Applied Energistics 2 团队维护、赞助或认可。
>
> 上游 CurseForge / Modrinth 页面发布的是原版 NeoForge 项目，并不是
> 本 Forge 1.20.1 移植版的下载页面。

这是 [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)
的附属模组，加入闪电能源、进阶加工机械、高吞吐过载 ME 网络、无线样板
路由以及装备系统。

## Forge 版唯一项目位置

```text
仓库       https://github.com/syarukasu/AE2-Lightning-Tech
分支       forge-1.20.1-port
问题       https://github.com/syarukasu/AE2-Lightning-Tech/issues
发行       https://github.com/syarukasu/AE2-Lightning-Tech/releases
```

原 `syarukasu/AE2-Lightning-Tech-Forge-1.20.1` 仓库是重复仓库，今后不再
用于新开发、问题反馈或发行。详情见
[`docs/REPOSITORY_CONSOLIDATION.md`](docs/REPOSITORY_CONSOLIDATION.md)。

## 当前版本与下一次更新

当前已发布的 Forge 基线：

```text
AE2LT       1.1.4-forge-1.20.1-r7
Minecraft   1.20.1
Forge       47.4.20
AE2         15.4.10 ～ 15.4.x
Java        17
```

下一次大型更新目标为上游 **AE2LT 2.0.6**。2.0.6 原版针对 Minecraft
1.21.1、NeoForge、AE2 19.x 与 Java 21，无法直接在 Forge 1.20.1 上运行。
Loader、Minecraft API、AE2 API、网络、Data Component、Mixin、世界数据与
Java 版本差异必须按功能重新移植。进度见
[Epic #21](https://github.com/syarukasu/AE2-Lightning-Tech/issues/21) 与
[`docs/UPSTREAM_2_0_6_DIFF.md`](docs/UPSTREAM_2_0_6_DIFF.md)。

## 主要特性

### 闪电能源系统

高压闪电与极高压闪电可以像 FE 一样被生成、存储、传输与消耗。

### 闪电收集与加工

- **闪电收集器**：捕获击中附近避雷针的雷电。
- **大气电离仪**：多方块天气加工设备。
- **特斯拉线圈**：使用材料与 FE 生产高压/极高压闪电。
- **闪电装配室**、**闪电模拟室**、**过载处理工厂**、**水晶催化器**。
- **苍穹转化核心**及相关配方链。

### 过载 ME 网络

- 过载 ME 控制器、接口、样板供应器与 16 色线缆。
- 带分页样板容量的扩展过载样板供应器。
- 无线过载控制器、无线接收器、频率与权限系统。
- 支持副产物与忽略 NBT 的过载样板编码器。

### 装备与设备

- Celestweave 装甲及可安装子模块。
- 电磁轨道炮、弹药、能量、网络绑定与特效。
- Device Hub 与过载设备工作台。

## Addon 开发者 API

`com.moakiee.ae2lt.api.*` 是第三方模组唯一可依赖的稳定接口。其他 package
都属于内部实现，可能在移植版更新之间改变。

公开范围包括：

- `AE2LTCapabilities.LIGHTNING_ENERGY_BLOCK`
- `ILightningEnergyHandler`
- `LightningTier`
- `LightningCollectedEvent`
- `AE2LTBlockEntityIds` / `AE2LTRecipeIds`
- `FrequencyApi` 与频率绑定辅助类
- `PatternProviderUiProfile`

2.0.6 移植应尽量保持该 Forge API；若必须改变，应提供明确的兼容门面与迁移表。

## 依赖

| 模组 | 要求 |
|---|---|
| Applied Energistics 2 | 必需 |
| Jade、JEI、AE2 Wireless Terminals、Curios | 可选联动 |
| Advanced AE、ExtendedAE、ExtendedAE Plus、Applied Flux | 可选联动 |
| Mekanism、Neo ECO AE Extension、Flywheel、Ponder | 可选联动 |

精确版本范围见 `gradle.properties` 与
`src/main/resources/META-INF/mods.toml`。

## 构建

使用 Java 17：

```powershell
.\gradlew.bat clean build --no-daemon
```

Linux/macOS：

```bash
./gradlew clean build --no-daemon
```

构建会把开发用 slim JAR 与最终 Jar-in-Jar 发行物分离。只发布无 classifier 的
最终文件：

```text
build/libs/ae2lt-1.1.4-forge-1.20.1-r7.jar
```

不要发布 `-slim.jar`。

## 问题反馈

Forge 1.20.1 移植版的问题统一提交到：

- https://github.com/syarukasu/AE2-Lightning-Tech/issues

请附上 Minecraft、Forge、AE2、AE2LT 与可选联动模组版本，以及完整日志或
崩溃报告。仅在上游 NeoForge 版也能复现时才应向上游项目报告。

## 许可证

[![源码许可证](https://img.shields.io/badge/Source-LGPL--3.0--only-blue)](LICENSE)
[![材质许可证](https://img.shields.io/badge/Assets-CC%20BY--NC--SA%203.0-lightgrey)](LICENSE_ASSETS.md)

- 源码：**LGPL-3.0-only**
- 纹理与其他视觉资源：**CC BY-NC-SA 3.0**

每个可分发 JAR 都在 `META-INF` 中包含源码许可证、资源许可证、永久署名与
移植来源说明。

## 鸣谢

- **原版 AE2 Lightning Tech：**由 **MOAKIEE** 创建，并由
  **CystrySU**、**gjmhmm8**、**_leng**、**TedXenon**、**MHanHanBing**
  共同开发。
- **Applied Energistics 2：**由 **TeamAppliedEnergistics** 创建并维护。
- **Forge 1.20.1 移植版：**由 **syarukasu** 独立维护，非上游官方发行。

详见 [CREDITS.md](CREDITS.md) 与 [PORT_NOTES.md](PORT_NOTES.md)。
