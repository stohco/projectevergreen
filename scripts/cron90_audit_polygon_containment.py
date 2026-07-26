#!/usr/bin/env python3
"""
CRON-90 Diagnostic: Country Polygon Containment Audit

After CRON-88 synced JSON settlement coordinates to Java PlanetSuzakuBlueprint
coordinates, some settlements may fall OUTSIDE their assigned country polygons.
This script identifies all such containment violations.

Uses the ray-casting point-in-polygon algorithm.
"""
import json
import sys

BLUEPRINT_PATH = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/worldgen/blueprint/planet_suzaku.json"

def point_in_polygon(x, z, polygon):
    """
    Ray-casting point-in-polygon test.
    polygon is a list of [x, z] vertices.
    Returns True if (x, z) is inside the polygon.
    """
    n = len(polygon)
    if n < 3:
        return False
    inside = False
    j = n - 1
    for i in range(n):
        xi, zi = polygon[i][0], polygon[i][1]
        xj, zj = polygon[j][0], polygon[j][1]
        # Check if the ray from (x, z) going +X crosses the edge (i, j)
        if ((zi > z) != (zj > z)) and \
           (x < (xj - xi) * (z - zi) / (zj - zi + 1e-12) + xi):
            inside = not inside
        j = i
    return inside

def main():
    with open(BLUEPRINT_PATH) as f:
        data = json.load(f)
    bp = data["blueprint"]

    # Build country name -> polygon map
    countries = {c["id"]: c.get("polygon", []) for c in bp["countries"]}

    print("=" * 72)
    print("CRON-90: Country Polygon Containment Audit")
    print("=" * 72)
    print()

    violations = []
    ok_count = 0
    no_country_count = 0

    for s in bp["settlements"]:
        sid = s["id"]
        x = s.get("x")
        z = s.get("z")
        country_id = s.get("country")

        if x is None or z is None:
            print(f"  SKIP {sid}: no coordinates")
            continue

        if not country_id:
            print(f"  WILDERNESS {sid} ({x},{z}): no country assigned")
            no_country_count += 1
            continue

        if country_id not in countries:
            print(f"  VIOLATION {sid} ({x},{z}): country '{country_id}' not defined in countries list")
            violations.append((sid, x, z, country_id, "UNDEFINED COUNTRY"))
            continue

        poly = countries[country_id]
        if not poly:
            print(f"  VIOLATION {sid} ({x},{z}): country '{country_id}' has empty polygon")
            violations.append((sid, x, z, country_id, "EMPTY POLYGON"))
            continue

        if point_in_polygon(x, z, poly):
            print(f"  OK    {sid} ({x},{z}) -> {country_id}")
            ok_count += 1
        else:
            # Find which country DOES contain it (if any)
            containing = []
            for cid, cpoly in countries.items():
                if cid != country_id and point_in_polygon(x, z, cpoly):
                    containing.append(cid)
            note = f"actually inside: {containing}" if containing else "in wilderness (no country contains it)"
            print(f"  VIOLATION {sid} ({x},{z}) -> assigned {country_id} but OUTSIDE; {note}")
            violations.append((sid, x, z, country_id, note))

    print()
    print("=" * 72)
    print(f"SUMMARY: {ok_count} OK, {len(violations)} violations, {no_country_count} wilderness")
    print("=" * 72)
    if violations:
        print("\nViolations to fix:")
        for sid, x, z, cid, note in violations:
            print(f"  - {sid} ({x},{z}): assigned={cid}, {note}")
        sys.exit(1)
    else:
        print("\nALL SETTLEMENTS INSIDE THEIR ASSIGNED COUNTRY POLYGONS")
        sys.exit(0)

if __name__ == "__main__":
    main()
