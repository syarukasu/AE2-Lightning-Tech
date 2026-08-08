# AE2 Lightning Tech Forge 1.20.1 port codebase map

> **Navigation only.** このMapはCodex・LLM・reviewerの探索量を減らすためのindexです。仕様判断はREADME、PORT_NOTES、`UPSTREAM_2_0_6_DIFF.md`、public API docs、現行Issueを使用します。

## 使い方

1. [`../AGENTS.md`](../AGENTS.md)を読む。
2. 下のTask routeを1つ選ぶ。
3. `Read first`と`Source scope`だけを開き、symbol検索から始める。
4. compile/test結果が別package依存を示した場合だけscopeを広げる。

初期読込の対象外:

```text
build/**
.gradle/**
run/**
生成JAR / logs / crash reports
全assets / 全recipes / 全lang
全BlockEntity / 全client code
対象外optional integration
historical duplicate repository
```

## 固定座標

```text
Canonical repository  syarukasu/AE2-Lightning-Tech
Branch                forge-1.20.1-port
Minecraft             1.20.1
Forge                 47.4.20
AE2                   15.4.10 / 15.4.x
Java                  17
Current release       1.1.4-forge-1.20.1-r7
Next target           upstream AE2LT 2.0.6
Stable API            com.moakiee.ae2lt.api.*
Source license        LGPL-3.0-only
Assets license        CC BY-NC-SA 3.0
```

## Current Forge baseline routes

| Route | Task | Read first | Source scope | Verification scope |
|---|---|---|---|---|
| `C0` | Port方針、repository、attribution、license、version | `../README.md`, `../PORT_NOTES.md`, `REPOSITORY_CONSOLIDATION.md`, credit/license files | docs/build metadata中心 | notices、JAR contents、build |
| `A1` | Public addon API、Capability、Event、frozen ID | README Public API、`api/package-info.java` | `api`と直接bridgeだけ | API compile/contract、addon smoke |
| `L1` | Lightning energy、tier、collector、grid storage、weather | README Lightning sections | 対象`grid`/`blockentity`/`event`/`config`だけ | unit、実lightning capture |
| `M1` | Assembly、Simulation、Factory、Catalyzer、Tesla、Firmament | README Machinery | 対象Block/BE/Menu/Recipe package | machine test、multiblock実動 |
| `W1` | Wireless controller/receiver、Frequency、Security、Binding | README Wireless/Public API | `api/frequency`とinternal wireless/networkだけ | bind/reconnect/save/restart |
| `N1` | Overloaded Controller/Cable/Interface/Provider/Encoder | README Overloaded Network | 対象`device`/`grid`/BE/Menu/Network | AE2 network integration |
| `E1` | Celestweave、Railgun、Device Hub/Workbench | README Equipment | `celestweave`, `item/railgun`, `device`,対象client/network | Client/Server、save/restart |
| `C1` | Screen、Renderer、Toolbar、Client-only integration | README Optional integrations | `client`と対象integrationだけ | Client load、visual smoke |
| `R1` | Registry、Recipes、Loot、Models、Lang、Data | README feature/ID contract | registration classと対象resource namespaceだけ | datagen/resource validation |
| `V1` | Build、CI、Packaging、Release | `../build.gradle`, `../gradle.properties`, release docs | build files、tests、workflow | `clean build`、JAR audit |

## AE2LT 2.0.6 backport routes

| Route | Issue | Task | Read first | Initial source scope |
|---|---:|---|---|---|
| `P2` | #12 | NeoForge 1.21.1 / AE2 19 / Java 21からForge基盤変換 | `UPSTREAM_2_0_6_DIFF.md`, Issue #12 | build、metadata、registry/network/save adapter、Mixin configs |
| `MX2` | #13 | Matter Warping Matrix | Issue #13 | Matrix Block/BE、cluster/scanner/repository、Menu/Screen、該当data |
| `TS2` | #14 | Tianshu Supercomputer、Closed Loop、Terminal | Issue #14 | Tianshu multiblock、pattern/seed、maintenance/reserve、terminal UI/network |
| `PG2` | #15 | Pigmee Technology、Alien Starship | Issue #15 | Pigmee machine/cell/entity、conversion/ritual、worldgen/loot |
| `ST2` | #16 | LightningKey、Bulk/Infinite/Fixed Cell | Issue #16 | key type、cell handler/inventory/item、NBT、tooltip/resources |
| `WN2` | #17 | Interface/Provider/Wireless/Frequency parity | Issue #17 | existing provider/interface/wireless/frequency logicと2.0.6対応class |
| `EQ2` | #18 | Celestweave/Phase Lock/Railgun/Device parity | Issue #18 | armor state/service/module、phase、railgun、device workbench/network |
| `IX2` | #19 | JEI/EMI/AE2WTLib/Mekanism/Veil/Data/Asset | Issue #19 | 対象integration、recipe viewer、resources/langだけ |
| `RV2` | #20 | World migration、runtime matrix、performance、release | Issue #20 | migration、acceptance tests、build/release docs、profiling |

`P2`が成立する前に各feature routeで独自Network/Data Component/Registry adapterを複製しない。2.0.6全体の進行はEpic #21を使用する。

## Package clusters

| Cluster | Typical paths | Responsibility |
|---|---|---|
| Entrypoint/registration | `AE2LightningTech.java`, registry/config | Forge lifecycleとfeature registration |
| Stable API | `api`, `api/client`, `api/event`, `api/frequency`, `api/ids`, `api/lightning` | third-party addon contract |
| Lightning runtime | `grid`, `event`, lightning関連BlockEntity | energy storage/capture/tier/grid ownership |
| Machines | machine関連Block/BE/Menu/Recipe | multiblock、processing、inventory/power |
| Matter Warping Matrix | `block/Matrix*`, `blockentity/Matrix*`, `logic/craft/Matrix*` | 7x11x7 formation scanning、formed state、member binding |
| Overloaded AE network | `device`, `grid`, provider/interface/Menu/Network | high-throughput network components |
| Wireless/Frequency | frequency/security/binding/wirelesslink/packet | membership、routing、recovery、permissions |
| Equipment | `celestweave`, `item/railgun`, `device/module`, `device/network` | armor、railgun、module、binding |
| Client | `client`、renderer、screen、FX | visual/UI only |
| Integrations | `integration/*`, optional Mixins | JEI/Jade/AE2WTLib/Mekanism等 |
| Resources | `src/main/resources` | metadata、assets、recipes、tags、loot、worldgen |
| Tests | `src/test` | math、serialization、contract、regression |

route選択後に対象package直下だけを列挙する。repository全体のrecursive treeを初手にしない。

## 主要entrypointとhot areas

| Purpose | Path |
|---|---|
| Mod entrypoint/registration | `src/main/java/com/moakiee/ae2lt/AE2LightningTech.java` |
| Stable API root | `src/main/java/com/moakiee/ae2lt/api` |
| Lightning capability | `src/main/java/com/moakiee/ae2lt/api/AE2LTCapabilities.java` |
| Collection event | `src/main/java/com/moakiee/ae2lt/api/event/LightningCollectedEvent.java` |
| Frequency facade | `src/main/java/com/moakiee/ae2lt/api/frequency/FrequencyApi.java` |
| Frequency binding | `src/main/java/com/moakiee/ae2lt/api/frequency/FrequencyBindingAccess.java` |
| Forge metadata | `src/main/resources/META-INF/mods.toml` |
| Access Transformer | `src/main/resources/META-INF/accesstransformer.cfg` |
| Main Mixin config | `src/main/resources/ae2lt.mixins.json` |
| Build/version | `build.gradle`, `gradle.properties` |
| Repository policy | `docs/REPOSITORY_CONSOLIDATION.md` |
| 2.0.6 diff | `docs/UPSTREAM_2_0_6_DIFF.md` |
| Release gate | GitHub Issue #20 |

`AE2LightningTech.java`は大型registration fileなので、対象DeferredRegister、Event、Listener symbolを検索して必要範囲だけ読む。

## 2.0.6 source-artifact search hints

2.0.6 class名を検索する場合は、最初に機能prefixを1つだけ選ぶ。

```text
Matrix*
Tianshu*
ClosedLoop*
Pigmee*
LightningKey*
BulkLightning*
OverloadedInterfaceLogic / OverloadedPatternProviderLogic
ProviderWireless* / WirelessOverflow* / Frequency*
Celestweave* / PhaseLock* / Railgun* / Device*
JEI* / Emi* / Veil* / Mekanism*
```

JAR全class一覧を毎回promptへ貼らない。対象prefixのtop-level class、resource、registration、testだけを扱う。

## 文書の読み分け

| Need | Document |
|---|---|
| current feature、dependencies、public API | `../README.md` |
| Forge 1.20.1 port provenance | `../PORT_NOTES.md` |
| repository統合 | `REPOSITORY_CONSOLIDATION.md` |
| 2.0.6 feature差分とIssue | `UPSTREAM_2_0_6_DIFF.md` |
| attribution | `../CREDITS.md` |
| license boundary | `../LICENSE`, `../LICENSE_ASSETS.md` |
| CurseForge本文/checklist | `../CURSEFORGE_DESCRIPTION.md`, `../CURSEFORGE_SUBMISSION_CHECKLIST.md` |
| Chinese user docs | `../README_zh_CN.md`（翻訳変更時のみ） |

## 最小検証コマンド

```text
./gradlew clean build --no-daemon
```

Windows:

```text
.\gradlew.bat clean build --no-daemon
```

API変更ではconsumer compile smoke、Wireless/Machine変更では実Forge World、Save/Restart、Client/Server両側確認を追加する。実行していない受入を完了扱いしない。

## Map更新条件

次の変更では本書を同じPRで更新する。

```text
Main entrypointまたは主要package移動
Stable API追加・削除・rename
新しい大規模multiblock/terminal/storage subsystem追加
2.0.6 Issueのsource/test境界変更
Build/metadata/release layout変更
```

## 省トークン用prompt

```text
AGENTS.mdとdocs/CODEBASE_MAP.mdの<Route ID>だけを基準に作業する。
Task: <作業内容>
最初はroute記載の文書、package、直近test以外を読まない。
別scopeへ広げる場合はcompile dependencyまたはtest failureを根拠として示す。
Unofficial port表記、stable API、ID/NBT、license境界を維持する。
実Minecraft未実行の結果をruntime verifiedと書かない。
```
