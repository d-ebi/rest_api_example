import copy
import json
import os
import uuid
from collections import defaultdict
from typing import Any, Dict, List, Optional, Tuple

import requests
import schemathesis

BASE_URL = os.environ.get("SCHEMATHESIS_BASE_URL", "http://localhost:8080")
SESSION = requests.Session()
USER_CACHE: List[Dict[str, Any]] = []
KNOWN_NAMES: set[str] = set()
RUN_STATS: Dict[str, Any] = {"total": 0, "statuses": defaultdict(int)}
CASE_IDS: Dict[int, int] = {}

FORCE_VALID_RUN = {
    ("POST", "/api/v1/users"): True,
    ("PUT", "/api/v1/users/{user_id}"): True,
    ("GET", "/api/v1/users/{user_id}"): True,
    ("DELETE", "/api/v1/users/{user_id}"): True,
}

DEFAULT_CAREER = {
    "title": "Software Engineer",
    "period": {"from": "2018/04/01", "to": "2021/03/31"},
}


def _refresh_cache() -> None:
    try:
        response = SESSION.get(f"{BASE_URL}/api/v1/users", params={"limit": 100, "offset": 0}, timeout=5)
        response.raise_for_status()
        users = response.json().get("users", [])
    except requests.RequestException:
        users = []
    USER_CACHE.clear()
    USER_CACHE.extend(users)
    KNOWN_NAMES.clear()
    KNOWN_NAMES.update(user["name"] for user in users if isinstance(user.get("name"), str))


def _ensure_cache() -> None:
    if not USER_CACHE:
        _refresh_cache()


def _generate_unique_name() -> str:
    while True:
        candidate = f"schemathesis-{uuid.uuid4().hex[:10]}"
        if candidate not in KNOWN_NAMES:
            return candidate


def _make_payload(include_career_histories: bool = True) -> Dict[str, Any]:
    payload = {
        "name": _generate_unique_name(),
        "age": 30,
        "birthday": "1994/04/01",
        "height": 170.5,
        "zipCode": "123-4567",
    }
    if include_career_histories:
        payload["careerHistories"] = [copy.deepcopy(DEFAULT_CAREER)]
    return payload


def _create_user() -> Optional[int]:
    payload = _make_payload()
    try:
        response = SESSION.post(
            f"{BASE_URL}/api/v1/users",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=5,
        )
        response.raise_for_status()
    except requests.RequestException:
        return None
    location = response.headers.get("Location")
    user_id = _extract_id(location)
    if user_id is not None:
        USER_CACHE.append({"id": user_id, "name": payload["name"]})
        KNOWN_NAMES.add(payload["name"])
    return user_id


def _extract_id(location: Optional[str]) -> Optional[int]:
    if not location:
        return None
    try:
        return int(str(location).rstrip("/").split("/")[-1])
    except (ValueError, IndexError):
        return None


def _get_existing_user_id() -> Optional[int]:
    _ensure_cache()
    for user in USER_CACHE:
        value = user.get("id")
        if isinstance(value, int):
            return value
    created = _create_user()
    return created


def _serialize(value: Any) -> str:
    if value is None or value.__class__.__module__ == "schemathesis.core":
        return "null"
    if isinstance(value, (dict, list)):
        try:
            return json.dumps(value, ensure_ascii=False, default=str)
        except (TypeError, ValueError):
            return str(value)
    return str(value)


def _build_curl(case: Any) -> str:
    try:
        return case.as_curl_command()
    except Exception as exc:
        base_url = BASE_URL or getattr(case.operation.schema, "base_url", None) or "http://localhost:8080"
        path = case.formatted_path if hasattr(case, "formatted_path") else case.path
        return f"curl -X {case.method.upper()} {base_url}{path} # failed to render: {exc}"


def _set_valid_case(case: Any, *, include_body: bool, include_career_histories: bool) -> None:
    user_id = _get_existing_user_id()
    if user_id is not None:
        case.path_parameters = {"user_id": user_id}
    if include_body:
        case.headers = case.headers or {}
        case.headers.setdefault("Content-Type", "application/json")
        case.body = _make_payload(include_career_histories=include_career_histories)


@schemathesis.hook
def before_call(ctx: schemathesis.HookContext, case: Any, **kwargs: Any) -> None:
    operation = case.operation
    if operation is None:
        return

    _ensure_cache()

    method = operation.method.upper()
    path = operation.path
    key: Tuple[str, str] = (method, path)
    force_valid = FORCE_VALID_RUN.get(key, False)

    RUN_STATS["total"] += 1
    case_id = RUN_STATS["total"]
    CASE_IDS[id(case)] = case_id

    path_params = case.path_parameters if isinstance(case.path_parameters, dict) else {}
    query = case.query if isinstance(case.query, dict) else {}
    body = case.body if isinstance(case.body, (dict, list)) else case.body

    print(f"[Schemathesis][Case #{case_id}] {method} {path}")
    print(f"  Path params: {_serialize(path_params)}")
    print(f"  Query      : {_serialize(query)}")
    print(f"  Body       : {_serialize(body)}")
    print(f"  Reproduce  : {_build_curl(case)}")

    if path == "/api/v1/users" and method == "POST":
        if isinstance(case.body, dict):
            name = case.body.get("name")
            if isinstance(name, str) and name in KNOWN_NAMES:
                new_name = _generate_unique_name()
                case.body["name"] = new_name
                KNOWN_NAMES.add(new_name)
        if force_valid:
            case.headers = case.headers or {}
            case.headers.setdefault("Content-Type", "application/json")
            case.body = _make_payload(include_career_histories=True)
            FORCE_VALID_RUN[key] = False

    if path == "/api/v1/users/{user_id}":
        if method == "DELETE" and force_valid:
            # Use a dedicated temporary record for deletion
            temp_id = _create_user()
            if temp_id is not None:
                case.path_parameters = {"user_id": temp_id}
            FORCE_VALID_RUN[key] = False
        elif method == "PUT" and force_valid:
            _set_valid_case(case, include_body=True, include_career_histories=False)
            FORCE_VALID_RUN[key] = False
        elif method == "GET" and force_valid:
            _set_valid_case(case, include_body=False, include_career_histories=False)
            FORCE_VALID_RUN[key] = False
        else:
            if not isinstance(case.path_parameters, dict):
                case.path_parameters = {}
            user_id_value = case.path_parameters.get("user_id")
            if isinstance(user_id_value, str) and not user_id_value.isdigit():
                # Use an alphanumeric placeholder so Spring produces JSON error responses
                case.path_parameters["user_id"] = "invalid-id"


@schemathesis.hook
def after_call(ctx: schemathesis.HookContext, case: Any, response: Any) -> None:
    operation = case.operation
    if operation is None:
        return

    method = operation.method.upper()
    path = operation.path

    RUN_STATS["statuses"][response.status_code] += 1
    case_id = CASE_IDS.pop(id(case), None)
    if case_id is not None:
        print(f"[Schemathesis][Case #{case_id}] Status: {response.status_code}")

    if path == "/api/v1/users" and method == "POST" and response.status_code == 201:
        user_id = _extract_id(response.headers.get("Location"))
        if user_id is not None and isinstance(case.body, dict):
            name = case.body.get("name")
            if isinstance(name, str):
                KNOWN_NAMES.add(name)
            USER_CACHE.append({"id": user_id, "name": name})

    if path == "/api/v1/users/{user_id}" and method == "DELETE" and response.status_code == 204:
        removed = case.path_parameters.get("user_id")
        if isinstance(removed, int):
            USER_CACHE[:] = [user for user in USER_CACHE if user.get("id") != removed]
            KNOWN_NAMES.clear()
            KNOWN_NAMES.update(user["name"] for user in USER_CACHE if isinstance(user.get("name"), str))


def _print_stats() -> None:
    print("[Schemathesis] 統計")
    print(f"  合計テストケース数: {RUN_STATS['total']}")
    for status_code in sorted(RUN_STATS["statuses"]):
        print(f"  ステータス {status_code}: {RUN_STATS['statuses'][status_code]}")


import atexit

atexit.register(_print_stats)
