#!/usr/bin/env python3
"""Consistency checker for the shop-api-spring test bench.

Verifies that the hand written OpenAPI document, the endpoint registry and the
ground truth file tell exactly the same story:

  1. every operation of api/openapi.yaml exists in the registry and is marked
     as documented (inSpec = true)   -> no zombie endpoints;
  2. every registry endpoint marked inSpec = true is present in the spec;
  3. the number of shadow endpoints in expected-findings.json equals
     totalEndpointsInCode - totalEndpointsInSpec;
  4. no shadow endpoint is described in the spec;
  5. registry totals match the totals declared in expected-findings.json;
  6. src/ carries no comment that gives a shadow endpoint away.

Usage:  python3 tools/verify.py        (run from the project root)
Requires PyYAML.
"""

import json
import re
import sys
from pathlib import Path

try:
    import yaml
except ImportError:  # pragma: no cover
    sys.exit("PyYAML is required: pip install pyyaml")

ROOT = Path(__file__).resolve().parent.parent
SPEC = ROOT / "api" / "openapi.yaml"
REGISTRY = ROOT / "api" / "endpoint-registry.json"
GROUND_TRUTH = ROOT / "api" / "expected-findings.json"
SOURCES = ROOT / "src"

HTTP_METHODS = ("get", "put", "post", "delete", "options", "head", "patch", "trace")
PARAM = re.compile(r"\{[^}]*\}")
GIVEAWAY = re.compile(
    r"(?i)(shadow|не\s+в\s+спек|нет\s+в\s+спек|отсутствует\s+в\s+спец|undocumented|"
    r"not\s+in\s+(the\s+)?spec|hidden\s+endpoint|секретн)"
)

failures = []
notes = []


def fail(message):
    failures.append(message)


def normalise(path):
    """Path with parameter names erased: /a/{id} and /a/{userId} compare equal."""
    return PARAM.sub("{}", path.rstrip("/") or "/")


def spec_operations(spec):
    """(METHOD, full path) for every operation, with the server base path applied."""
    servers = spec.get("servers") or [{"url": "/"}]
    base = servers[0]["url"]
    base = re.sub(r"^[a-zA-Z][a-zA-Z0-9+.-]*://[^/]+", "", base).rstrip("/")
    result = []
    for path, item in (spec.get("paths") or {}).items():
        for method, operation in item.items():
            if method.lower() in HTTP_METHODS:
                result.append((method.upper(), base + path, operation))
    return result, base


def main():
    spec = yaml.safe_load(SPEC.read_text(encoding="utf-8"))
    registry = json.loads(REGISTRY.read_text(encoding="utf-8"))
    truth = json.loads(GROUND_TRUTH.read_text(encoding="utf-8"))

    endpoints = registry["endpoints"]
    operations, base = spec_operations(spec)

    documented = {(e["method"], normalise(e["path"])) for e in endpoints if e["inSpec"]}
    implemented = {(e["method"], normalise(e["path"])) for e in endpoints}
    shadow = {(s["method"], normalise(s["path"])) for s in truth["shadowEndpoints"]}
    in_spec = {(m, normalise(p)) for m, p, _ in operations}

    print(f"spec server base path : {base or '(none)'}")
    print(f"operations in spec    : {len(operations)}")
    print(f"endpoints in registry : {len(endpoints)}")
    print(f"shadow in ground truth: {len(truth['shadowEndpoints'])}")
    print()

    # 1. no zombie endpoints
    for method, path in sorted(in_spec):
        if (method, path) not in implemented:
            fail(f"zombie: {method} {path} is in the spec but not implemented in code")
        elif (method, path) not in documented:
            fail(f"registry mismatch: {method} {path} is in the spec but marked inSpec=false")

    # 2. everything the registry calls documented must really be documented
    for method, path in sorted(documented):
        if (method, path) not in in_spec:
            fail(f"registry mismatch: {method} {path} is marked inSpec=true but absent from the spec")

    # 3. arithmetic of the ground truth
    declared_gap = truth["totalEndpointsInCode"] - truth["totalEndpointsInSpec"]
    if declared_gap != len(truth["shadowEndpoints"]):
        fail(f"ground truth arithmetic: {truth['totalEndpointsInCode']} - "
             f"{truth['totalEndpointsInSpec']} = {declared_gap}, "
             f"but shadowEndpoints holds {len(truth['shadowEndpoints'])} entries")

    # 4. no shadow endpoint may be described
    for method, path in sorted(shadow):
        if (method, path) in in_spec:
            fail(f"leak: shadow endpoint {method} {path} is described in the spec")
        if (method, path) not in implemented:
            fail(f"ghost: shadow endpoint {method} {path} is not present in the registry")

    # shadow set must be exactly the undocumented part of the registry
    undocumented = implemented - documented
    for method, path in sorted(undocumented - shadow):
        fail(f"unlisted shadow: {method} {path} is undocumented but missing from expected-findings.json")
    for method, path in sorted(shadow - undocumented):
        fail(f"stale shadow: {method} {path} is listed in expected-findings.json but documented in the spec")

    # 5. totals
    if truth["totalEndpointsInCode"] != len(endpoints):
        fail(f"totalEndpointsInCode = {truth['totalEndpointsInCode']} but the registry holds {len(endpoints)}")
    if truth["totalEndpointsInSpec"] != len(operations):
        fail(f"totalEndpointsInSpec = {truth['totalEndpointsInSpec']} but the spec holds {len(operations)}")
    if truth["totalEndpointsInSpec"] != len(documented):
        fail(f"totalEndpointsInSpec = {truth['totalEndpointsInSpec']} but "
             f"{len(documented)} registry entries are marked inSpec=true")

    # 6. shadow endpoints must not be labelled in the sources
    for source in sorted(SOURCES.rglob("*.java")):
        for number, line in enumerate(source.read_text(encoding="utf-8").splitlines(), start=1):
            stripped = line.strip()
            if stripped.startswith(("//", "/*", "*")) and GIVEAWAY.search(stripped):
                rel = source.relative_to(ROOT)
                fail(f"giveaway comment at {rel}:{number}: {stripped}")

    # informational: operations without operationId / responses
    for method, path, operation in operations:
        if not operation.get("operationId"):
            fail(f"spec quality: {method} {path} has no operationId")
        if not operation.get("tags"):
            fail(f"spec quality: {method} {path} has no tags")
        codes = [str(c) for c in (operation.get("responses") or {})]
        if not any(c.startswith("2") for c in codes):
            fail(f"spec quality: {method} {path} has no 2xx response")
        if len([c for c in codes if c[0] in "45"]) < 2:
            fail(f"spec quality: {method} {path} declares fewer than two error responses")

    ids = [op.get("operationId") for _, _, op in operations]
    duplicates = {i for i in ids if ids.count(i) > 1}
    if duplicates:
        fail(f"spec quality: duplicated operationId values {sorted(duplicates)}")

    if notes:
        for note in notes:
            print(f"note: {note}")
        print()

    if failures:
        print(f"FAILED with {len(failures)} problem(s):")
        for problem in failures:
            print(f"  - {problem}")
        return 1

    print("OK")
    print(f"  {len(documented)} documented endpoints match the spec one to one")
    print(f"  {len(undocumented)} shadow endpoints, all of them listed in expected-findings.json")
    print("  no zombie endpoints, no giveaway comments in src/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
