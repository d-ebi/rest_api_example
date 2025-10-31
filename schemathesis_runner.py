#!/usr/bin/env python3
"""
Schemathesis を用いた OpenAPI テスト実行スクリプト。

仮想環境 (`~/.venvs/schemathesis`) を有効化した状態で実行してください。
"""

from __future__ import annotations

import argparse
import os
import subprocess
import sys
from pathlib import Path

DEFAULT_CHECKS = ",".join(
    [
        "not_a_server_error",
        "status_code_conformance",
        "content_type_conformance",
        "response_headers_conformance",
        "response_schema_conformance",
    ]
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run Schemathesis against the local REST API.")
    parser.add_argument(
        "--schema-path",
        default=os.environ.get("SCHEMATHESIS_SCHEMA_PATH", "target/api-docs.yml"),
        help="Path to an OpenAPI schema file (default: target/api-docs.yml).",
    )
    parser.add_argument(
        "--base-url",
        default=os.environ.get("SCHEMATHESIS_BASE_URL", "http://localhost:8080"),
        help="Target base URL (default: http://localhost:8080).",
    )
    parser.add_argument(
        "--workers",
        default=os.environ.get("SCHEMATHESIS_WORKERS", "1"),
        help="Number of concurrent workers to use (default: 1).",
    )
    parser.add_argument(
        "--checks",
        default=os.environ.get("SCHEMATHESIS_CHECKS", DEFAULT_CHECKS),
        help="Comma separated list of Schemathesis checks to run "
             f"(default: {DEFAULT_CHECKS}).",
    )
    parser.add_argument(
        "--phases",
        default=os.environ.get("SCHEMATHESIS_PHASES", "examples,fuzzing"),
        help="Comma separated Schemathesis test phases to enable (default: examples,fuzzing).",
    )
    parser.add_argument(
        "--suppress-health-check",
        default=os.environ.get("SCHEMATHESIS_SUPPRESS_HEALTH_CHECK", "data_too_large,filter_too_much"),
        help="Comma separated Schemathesis health checks to suppress (default: data_too_large,filter_too_much).",
    )
    parser.add_argument(
        "--config-file",
        default=os.environ.get("SCHEMATHESIS_CONFIG_FILE", "schemathesis.toml"),
        help="Schemathesis TOML config file (default: schemathesis.toml).",
    )
    parser.add_argument(
        "extra_args",
        nargs=argparse.REMAINDER,
        help="Additional arguments passed verbatim to the Schemathesis CLI.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    schema_path = Path(args.schema_path)
    if not schema_path.exists():
        print(f"[ERROR] Schema file not found: {schema_path}", file=sys.stderr)
        return 1

    os.environ.setdefault("SCHEMATHESIS_BASE_URL", args.base_url)

    config_path = Path(args.config_file)
    command = ["schemathesis"]
    if config_path.exists():
        command.extend(["--config-file", str(config_path)])
    command.extend(
        [
            "run",
            str(schema_path),
            f"--url={args.base_url}",
            f"--checks={args.checks}",
            f"--workers={args.workers}",
        ]
    )

    if args.phases:
        command.append(f"--phases={args.phases}")

    if args.suppress_health_check:
        command.append(f"--suppress-health-check={args.suppress_health_check}")

    if args.extra_args:
        command.extend(args.extra_args)

    print("[INFO] Executing:", " ".join(command))
    completed = subprocess.run(command, check=False)
    return completed.returncode


if __name__ == "__main__":
    sys.exit(main())
