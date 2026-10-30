#!/usr/bin/env bash
# CI-only introspection tool: dumps compact javap signatures for the classes
# listed in tools/api-dump-classes.txt, resolved from the Mojang-mapped
# Minecraft jar and remapped Fabric API jars on the compile classpath.
set -u
set -o pipefail

OUT="${CI_WORKSPACE:-build}/api-dump"
CLASSLIST="${CI_WORKSPACE:-tools}/api-dump-classes.txt"
mkdir -p "$OUT"

CPFILE=build/api-dump/api-classpath.txt
if [ ! -f "$CPFILE" ]; then
  gradle printClasspath --no-daemon -q || true
fi
if [ ! -f "$CPFILE" ]; then
  echo "No classpath dump found; aborting introspection." > "$OUT/ERROR.txt"
  exit 0
fi

mapfile -t JARS < "$CPFILE"
CP=$(IFS=:; echo "${JARS[*]}")
CP="${CP// /\\ }"   # jar paths have no spaces in CI, but escape defensively

> "$OUT/api-dump.txt"
> "$OUT/missing.txt"

javap_cmd() {
  javap -classpath "$CP" -p "$1" 2>>"$OUT/missing.txt"
}

n=0
while read -r cls; do
  [ -z "$cls" ] && continue
  [[ "$cls" == \#* ]] && continue
  n=$((n+1))
  {
    echo "===== $cls ====="
    javap_cmd "$cls"
    echo
  } >> "$OUT/api-dump.txt"
done < "$CLASSLIST"

# Also dump the top-level structures of key Fabric API modules (class names).
{
  echo "===== fabric-api jar listings (selected modules) ====="
  for j in "${JARS[@]}"; do
    case "$j" in
      *fabric-api*)
        if unzip -l "$j" >/dev/null 2>&1; then
          unzip -l "$j" | grep -oE "net/fabricmc/fabric/api/[A-Za-z0-9/._]+\.class" | sed 's#net/fabricmc/fabric/api/##; s#\.class##; s#/#.#g' | sort -u
        else
          echo "unzip failed for $j"
        fi
        ;;
    esac
  done
} >> "$OUT/api-dump.txt"

echo "Introspection dump: $OUT/api-dump.txt ($(wc -l < "$OUT/api-dump.txt") lines), missing: $(grep -c . "$OUT/missing.txt" 2>/dev/null || echo 0)"
