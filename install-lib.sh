#!/usr/bin/env bash
# Install all jars under ./lib into local Maven repo.
# GAV is read from each jar's META-INF/maven/**/pom.properties.
# Default: skip if already present. Force: ./install-lib.sh --force

set -euo pipefail
cd "$(dirname "$0")"

FORCE=0
if [[ "${1:-}" == "--force" || "${1:-}" == "-f" ]]; then
  FORCE=1
fi

if [[ ! -d lib ]]; then
  echo "[ERROR] lib directory not found: $(pwd)/lib"
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "[ERROR] mvn not found in PATH"
  exit 1
fi

if [[ -n "${MAVEN_REPO:-}" ]]; then
  LOCAL_REPO="$MAVEN_REPO"
else
  LOCAL_REPO="$(mvn -q help:evaluate -Dexpression=settings.localRepository -DforceStdout 2>/dev/null || true)"
fi
LOCAL_REPO="${LOCAL_REPO:-$HOME/.m2/repository}"

echo "Local Maven repo: $LOCAL_REPO"
if [[ "$FORCE" == "1" ]]; then
  echo "Mode: FORCE overwrite"
else
  echo "Mode: skip if already installed"
fi
echo

read_gav() {
  local jar="$1"
  # unzip -p may fail on some jars; python is more reliable
  python - "$jar" <<'PY'
import sys, zipfile, re
jar = sys.argv[1]
with zipfile.ZipFile(jar) as z:
    props = [n for n in z.namelist() if n.startswith("META-INF/maven/") and n.endswith("pom.properties")]
    if not props:
        sys.exit(2)
    text = z.read(props[0]).decode("utf-8", "replace")
    poms = [n for n in z.namelist() if n.startswith("META-INF/maven/") and n.endswith("pom.xml")]
    packaging = "jar"
    if poms:
        pom = z.read(poms[0]).decode("utf-8", "replace")
        m = re.search(r"<packaging>\s*([^<]+)\s*</packaging>", pom)
        if m:
            packaging = m.group(1).strip()
gav = {}
for line in text.splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    k, v = line.split("=", 1)
    if k in ("groupId", "artifactId", "version"):
        gav[k] = v.strip()
if not all(k in gav for k in ("groupId", "artifactId", "version")):
    sys.exit(2)
print(gav["groupId"])
print(gav["artifactId"])
print(gav["version"])
print(packaging)
PY
}

COUNT=0
INSTALLED=0
SKIPPED=0
FAILED=0

shopt -s nullglob
for jar in lib/*.jar; do
  COUNT=$((COUNT + 1))
  if ! mapfile -t GAV < <(read_gav "$jar"); then
    echo "[MISS] no/invalid pom.properties in $(basename "$jar")"
    FAILED=$((FAILED + 1))
    continue
  fi
  GID="${GAV[0]}"
  AID="${GAV[1]}"
  VER="${GAV[2]}"
  PACKAGING="${GAV[3]:-jar}"

  GID_PATH="${GID//.//}"
  DEST="$LOCAL_REPO/$GID_PATH/$AID/$VER/$AID-$VER.jar"

  if [[ -f "$DEST" && "$FORCE" != "1" ]]; then
    echo "[SKIP] $GID:$AID:$VER"
    SKIPPED=$((SKIPPED + 1))
    continue
  fi

  echo "[INSTALL] $GID:$AID:$VER ($PACKAGING)  <- $(basename "$jar")"
  if ! mvn -q install:install-file \
      -Dfile="$jar" \
      -DgroupId="$GID" \
      -DartifactId="$AID" \
      -Dversion="$VER" \
      -Dpackaging="$PACKAGING"; then
    echo "[FAIL] $GID:$AID:$VER"
    FAILED=$((FAILED + 1))
    continue
  fi
  INSTALLED=$((INSTALLED + 1))
done

echo
echo "Done. jars=$COUNT installed=$INSTALLED skipped=$SKIPPED failed=$FAILED"
if [[ "$FAILED" -gt 0 ]]; then
  exit 1
fi
