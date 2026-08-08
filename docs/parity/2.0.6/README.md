# AE2LT 2.0.6 exact parity target

This directory pins the exact artifact and inventory used as the completion target for the Forge 1.20.1 backport.

## Source of truth

```text
Target artifact          ae2lt-2.0.6.jar
Target SHA-256           bea3e8196a3f126e2d8fcedc86bdb44b536efd3bedd28d3f89bdb0848e5a687b
Decompiler               CFR 0.152
Source Minecraft         1.21.1
Source loader            NeoForge 21+
Source AE2               [19.2.17,19.3)
Source Java              21 / class major 65

Port Minecraft           1.20.1
Port loader              Forge 47.4.20
Port AE2                 15.4.10 / 15.4.x
Port Java                17 / class major 61
Baseline commit          9f9a84841bc428fb9059ceaaed5b32e85fdfbd4f
Baseline feature state   AE2LT 2.0.6 Forge 1.20.1 port
```

The upstream 2.0.6 JAR is the feature target. The Forge 1.20.1 implementation is published as `2.0.6-forge-1.20.1-r1`.

## Decompile inventory

The uploaded target JAR was fully decompiled with CFR 0.152 without fatal errors.

```text
JAR entries                          3,136
AE2LT class files including inner   1,507
Top-level/non-inner Java files        948
JAR-owned non-class resources       1,373
CFR fatal errors                        0
```

Initial comparison against the pinned Forge baseline:

```text
Java
  current                             671
  target                              948
  same class path                     576
  target-only                         372
  current-only                         95

Resources (raw paths)
  current                             965
  target                            1,373
  same raw path                       687
  target-only raw path                686
  current-only raw path               278

Language keys
  en_us current 823 / target 1301 / missing 488
  zh_cn current 823 / target 1301 / missing 486
  ja_jp current 823 / target JAR 0
```

Raw resource paths are not final parity identities because Minecraft 1.21.1 and 1.20.1 use different data directory/schema conventions. Recipes, loot tables, advancements and tags must be normalized to logical registry IDs before marking them missing or obsolete.

## Completion rule

Every target-owned Java class and resource must eventually have one explicit status:

```text
PORTED_EXACT_BEHAVIOR
PORTED_1_20_1_ADAPTER_SAME_BEHAVIOR
EXTERNAL_DEPENDENCY_EXACT_EQUIVALENT
NOT_APPLICABLE_WITH_TECHNICAL_PROOF
BLOCKED_MISSING_REQUIRED_ARTIFACT
VERIFIED_CLIENT_SERVER_SAVE_RESTART
```

An unexplained omission is not a valid status. Matching class paths are not considered ported until the implementation is reviewed. A successful build is not runtime parity.

## Japanese localization

The target JAR does not contain `ja_jp.json`. The existing Forge port's 823 Japanese keys are a port-owned compatibility feature and must not be removed. New 2.0.6 keys should be translated as their features are ported.

## Thunderbolt boundary

AE2LT 2.0.6 requires `thunderbolt [1.0.1,2.0.0)`. The decompiled AE2LT source contains 54 files importing 63 Thunderbolt-owned types. Those external algorithms cannot be reconstructed from the AE2LT JAR alone. Exact parity therefore requires a fixed compatible Thunderbolt artifact/source or an explicitly tracked Thunderbolt backport. Placeholder/no-op behavior is not acceptable.

## Tracking

- Full parity gate: #23
- Common platform/API adapter: #12
- Overall 2.0.6 backport Epic: #21
- Release gate: #20
