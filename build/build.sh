#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

FRONT_DIR="$ROOT_DIR/Front"
CORE_DIR="$ROOT_DIR/Core"
STATIC_DIR="$CORE_DIR/src/main/resources/static"
DB_SQL_DIR="$CORE_DIR/src/main/resources/db"
DIST_DIR="$FRONT_DIR/dist"
RELEASE_DIR="$SCRIPT_DIR"
RELEASE_SQL_DIR="$RELEASE_DIR/sql"
JAR_NAME="database-keshe-0.0.1-SNAPSHOT.jar"

configure_java() {
    if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" && -x "$JAVA_HOME/bin/javac" ]]; then
        echo "[INFO] Using JAVA_HOME: $JAVA_HOME"
        return
    fi

    local jdk17_home="/usr/lib/jvm/java-17-openjdk-amd64"
    if [[ -x "$jdk17_home/bin/java" && -x "$jdk17_home/bin/javac" ]]; then
        export JAVA_HOME="$jdk17_home"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "[INFO] JAVA_HOME not set. Using detected JDK 17: $JAVA_HOME"
        return
    fi

    if command -v javac >/dev/null 2>&1; then
        local javac_path
        javac_path="$(readlink -f "$(command -v javac)")"
        export JAVA_HOME="$(cd "$(dirname "$javac_path")/.." && pwd)"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "[INFO] JAVA_HOME not set. Using detected JDK: $JAVA_HOME"
        return
    fi

    echo "[ERROR] javac was not found. Install JDK 17 or set JAVA_HOME to a JDK."
    exit 1
}

fail() {
    echo
    echo "[FAIL] Build failed."
    exit 1
}

trap fail ERR

echo "======================================================"
echo "Build PSM-Smart System"
echo "======================================================"

if [[ ! -f "$FRONT_DIR/package.json" ]]; then
    echo "[ERROR] Frontend project not found: $FRONT_DIR"
    exit 1
fi

if [[ ! -f "$CORE_DIR/pom.xml" ]]; then
    echo "[ERROR] Backend project not found: $CORE_DIR"
    exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
    echo "[ERROR] npm was not found in PATH."
    exit 1
fi

echo
echo "[1/5] Building frontend..."
(
    cd "$FRONT_DIR"
    if [[ ! -d node_modules ]]; then
        echo "[INFO] node_modules not found. Running npm install..."
        npm install
    elif [[ ! -d node_modules/element-plus ]]; then
        echo "[INFO] Frontend dependencies changed. Running npm install..."
        npm install
    fi

    npm run build
)

if [[ ! -f "$DIST_DIR/index.html" ]]; then
    echo "[ERROR] Frontend build output not found: $DIST_DIR"
    exit 1
fi

echo
echo "[2/5] Copying frontend files to Spring Boot static resources..."
rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -a "$DIST_DIR"/. "$STATIC_DIR"/

echo
echo "[3/5] Packaging backend jar..."
configure_java

if command -v mvn >/dev/null 2>&1; then
    MAVEN_CMD=(mvn)
elif [[ -x "$CORE_DIR/mvnw" ]]; then
    MAVEN_CMD=("$CORE_DIR/mvnw")
    export MAVEN_USER_HOME="${MAVEN_USER_HOME:-$CORE_DIR/.m2-home}"
    mkdir -p "$MAVEN_USER_HOME"
    echo "[INFO] Using Maven wrapper cache: $MAVEN_USER_HOME"
else
    echo "[ERROR] Maven command not found. Install Maven or add Core/mvnw."
    exit 1
fi

(
    cd "$CORE_DIR"
    "${MAVEN_CMD[@]}" clean package -DskipTests
)

echo
echo "[4/5] Copying jar to release directory..."
SOURCE_JAR="$CORE_DIR/target/$JAR_NAME"
TARGET_JAR="$RELEASE_DIR/$JAR_NAME"

if [[ ! -f "$SOURCE_JAR" ]]; then
    echo "[ERROR] Jar not found: $SOURCE_JAR"
    exit 1
fi

cp -f "$SOURCE_JAR" "$TARGET_JAR"

echo
echo "[5/5] Copying database init SQL to release directory..."
if [[ ! -d "$DB_SQL_DIR" ]]; then
    echo "[ERROR] Database init SQL directory not found: $DB_SQL_DIR"
    exit 1
fi

rm -rf "$RELEASE_SQL_DIR"
mkdir -p "$RELEASE_SQL_DIR"
find "$DB_SQL_DIR" -maxdepth 1 -type f -name "*.sql" -exec cp -f {} "$RELEASE_SQL_DIR"/ \;

echo
echo "[DONE] Build finished."
echo "Jar: $TARGET_JAR"
echo "SQL: $RELEASE_SQL_DIR"
