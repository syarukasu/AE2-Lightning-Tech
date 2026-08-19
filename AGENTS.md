# AE2 Lightning Tech Forge 1.20.1 port agent entrypoint

このファイルはCodex・LLM・自動レビューが、巨大なForge port全体を毎回読み込まずに作業範囲を決めるための入口です。

## 最小読込手順

1. 最初に本書と[`docs/CODEBASE_MAP.md`](docs/CODEBASE_MAP.md)だけを読む。
2. 2.0.6移植作業では[`docs/parity/2.0.6/README.md`](docs/parity/2.0.6/README.md)を固定target snapshotとして追加で読む。
3. MapのTask routeを1つ選び、そのrouteに書かれた文書、package、直近testだけを開く。
4. Java/Gradleファイルは対象symbolを検索し、必要なclass、method、task周辺だけを読む。
5. compile error、test failure、実依存関係が示した場合だけ隣接scopeへ範囲を広げる。
6. 全assets、全recipes、全BlockEntity、全client code、全optional integrationの再帰読込を開始条件にしない。

## Canonical repository

```text
Repository          syarukasu/AE2-Lightning-Tech
Development branch  forge-1.20.1-port
Issues              syarukasu/AE2-Lightning-Tech/issues
Releases            syarukasu/AE2-Lightning-Tech/releases
```

`syarukasu/AE2-Lightning-Tech-Forge-1.20.1`はhistorical redirectであり、通常の探索、修正、Issue、Release対象にしません。明示的な履歴比較が必要な場合だけ参照します。

## 固定契約

```text
Project status             unofficial port
Minecraft                  1.20.1
Loader                     Forge 47.4.20
Required mod               Applied Energistics 2 15.4.10 / 15.4.x
Runtime / bytecode         Java 17
Source license             LGPL-3.0-only
Visual assets license      CC BY-NC-SA 3.0
Stable addon API           com.moakiee.ae2lt.api.* only
Current baseline           2.0.7-forge-1.20.1-r5
Exact major target         ae2lt-2.0.6.jar
Target SHA-256             bea3e8196a3f126e2d8fcedc86bdb44b536efd3bedd28d3f89bdb0848e5a687b
```

このportを原作者・AE2 teamの公式releaseと表現しません。upstream attribution、source license、asset license、permanent noticesを削除・曖昧化しません。

`com.moakiee.ae2lt.api.*`だけがthird-party addon向けstable surfaceです。その他package、internal BlockEntity、Menu、Network、Registry実装をpublic compatibility contractとして扱いません。

Lightning storage、Wireless Frequency/Security、AE2 Grid connection、Machine inventory、Recipe、Network packet、World/NBTはServer authorityを維持します。性能目的でClient側推測、非同期World mutation、保存形式の無断変更を入れません。

## 2.0.6 port規則

2.0.6差分作業では最初に[`docs/parity/2.0.6/README.md`](docs/parity/2.0.6/README.md)、[`docs/UPSTREAM_2_0_6_DIFF.md`](docs/UPSTREAM_2_0_6_DIFF.md)、対象Issueだけを読みます。

- GitHub上の1.1.4 sourceは開始点であって完成目標ではない。完成基準は固定SHA-256の2.0.6 JAR。
- 2.0.6-only class/resourceを「主要機能ではない」という理由で除外しない。
- 同名classでもmethod-level parity確認前に移植済み扱いしない。
- NeoForge 1.21.1 / AE2 19.x / Java 21 codeを直接コピーして完了扱いしない。
- Data Component、StreamCodec、payload、registry、Mixin、world dataを1.20.1用の明示adapterへ変換する。
- 既存r7 ID/NBT/APIを壊すrenameを行わない。
- 変換不能なstateを黙ってskipせず、World open前に理由付きで停止する。
- #12の共通compatibility層を各featureで重複実装しない。
- #23のParity Gateで未分類entryが残る状態を2.0.6完了としない。
- Build成功をruntime/feature/release verifiedと表現しない。

## 安全規則

- Forge 1.20.1 / AE2 15.4.x runtime nameとlifecycleを基準にする。
- Frequency API/binding lifecycleはServer threadとAE2 node lifecycleを維持する。
- Lightning capture eventはinsert前のcancellation/amount rewrite契約を崩さない。
- Public ID、serialized tier名、Recipe/BlockEntity IDを無断変更しない。
- optional MODがない環境でclass loading errorを起こさない。
- simulation/execute差分で材料消失・重複出力を起こさない。
- 無制限Storage scan、Pattern scan、queue、Map copy、packet burstを追加しない。

## 編集規則

- source変更では同じpackageのtest、READMEの該当contract、`PORT_NOTES.md`を先に確認する。
- public API変更はAPI docs、README、互換性/移行説明を同じ変更で更新する。
- entrypoint、主要package、public API、重要test、2.0.6 routeの位置が変わる場合は`docs/CODEBASE_MAP.md`を更新する。
- `AE2LightningTech.java`等の大型fileは対象registration/symbol周辺だけを読む。
- `build/`、`.gradle/`、run directory、logs、生成JARをsourceとして編集しない。

## 検証順

```text
対象testまたはcompile task
-> ./gradlew clean build --no-daemon
-> 必要な場合だけForge実環境でClient / Server / World / Machine / Network / Save / Restart確認
```

unit testやCIだけの結果をruntime verifiedとして扱いません。
