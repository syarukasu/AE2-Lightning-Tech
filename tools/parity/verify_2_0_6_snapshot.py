#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SNAPSHOT = ROOT / "docs" / "parity" / "2.0.6" / "parity-summary.json"
EXPECTED_SHA256 = "bea3e8196a3f126e2d8fcedc86bdb44b536efd3bedd28d3f89bdb0848e5a687b"
EXPECTED_BASELINE = "9f9a84841bc428fb9059ceaaed5b32e85fdfbd4f"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


def main() -> None:
    data = json.loads(SNAPSHOT.read_text(encoding="utf-8"))
    target = data["target"]
    baseline = data["baseline"]
    java_diff = data["initial_java_diff"]
    resource_diff = data["initial_resource_raw_path_diff"]
    languages = data["languages"]
    thunderbolt = data["thunderbolt"]

    require(target["sha256"] == EXPECTED_SHA256, "2.0.6 target SHA-256 changed")
    require(baseline["commit"] == EXPECTED_BASELINE, "1.1.4 Forge baseline commit changed")
    require(target["decompiler"] == "CFR 0.152", "decompiler identity changed")
    require(target["jar_entries"] == 3136, "unexpected target JAR entry count")
    require(target["class_files_including_inner"] == 1507, "unexpected target class count")
    require(target["top_level_java"] == 948, "unexpected target Java count")
    require(target["resources"] == 1373, "unexpected target resource count")

    require(java_diff["same_path"] + java_diff["target_only"] == target["top_level_java"],
            "Java target inventory is internally inconsistent")
    require(java_diff["same_path"] + java_diff["baseline_only"] == baseline["java"],
            "Java baseline inventory is internally inconsistent")
    require(resource_diff["same_path"] + resource_diff["target_only"] == target["resources"],
            "resource target inventory is internally inconsistent")
    require(resource_diff["same_path"] + resource_diff["baseline_only"] == baseline["resources"],
            "resource baseline inventory is internally inconsistent")

    require(languages["en_us"]["target"] == 1301, "unexpected en_us target key count")
    require(languages["zh_cn"]["target"] == 1301, "unexpected zh_cn target key count")
    require(languages["ja_jp"]["baseline"] == 823, "Japanese compatibility baseline changed")
    require(thunderbolt["ae2lt_files_importing_thunderbolt"] == 54,
            "Thunderbolt import inventory changed")
    require(thunderbolt["referenced_thunderbolt_types"] == 63,
            "Thunderbolt type inventory changed")

    print("AE2LT 2.0.6 parity snapshot OK")
    print(f"target={target['artifact']} sha256={target['sha256']}")
    print(f"java={target['top_level_java']} resources={target['resources']}")


if __name__ == "__main__":
    main()
