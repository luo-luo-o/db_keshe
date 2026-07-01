#!/usr/bin/env bash
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

JAR_NAME="database-keshe-0.0.1-SNAPSHOT.jar"
TARGET_JAR="$SCRIPT_DIR/$JAR_NAME"
SOURCE_JAR="$ROOT_DIR/Core/target/$JAR_NAME"
LOG_DIR="$ROOT_DIR/logs"
ENV_FILE="$ROOT_DIR/.env"
SPRING_CONFIG_IMPORT="optional:file:$ENV_FILE[.properties]"

RED=$'\033[31m'
GREEN=$'\033[32m'
YELLOW=$'\033[33m'
CYAN=$'\033[36m'
RESET=$'\033[0m'

BG_MODE=false
if [[ "${1:-}" == "-b" || "${1:-}" == "--background" ]]; then
    BG_MODE=true
elif [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    echo "Usage: $0 [-b|--background]"
    exit 0
fi

mkdir -p "$LOG_DIR"

finish() {
    echo "------------------------------------------------------"
    echo "${GREEN}[Finished] Script execution completed.${RESET}"
}

error_exit() {
    echo "[Error] ${RED}$1${RESET}"
    finish
    exit 1
}

load_env_file() {
    [[ -f "$ENV_FILE" ]] || error_exit ".env file was not found: $ENV_FILE"

    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a

    require_env_var "ORACLE_PASSWORD"
    require_env_var "DB_USERNAME"
    require_env_var "DB_PASSWORD"
    require_env_var "DB_URL"
}

require_env_var() {
    local name="$1"
    if [[ -z "${!name:-}" ]]; then
        error_exit "$name is required in $ENV_FILE"
    fi
}

find_java_cmd() {
    if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
        printf '%s\n' "$JAVA_HOME/bin/java"
        return 0
    fi

    local jdk17_java="/usr/lib/jvm/java-17-openjdk-amd64/bin/java"
    if [[ -x "$jdk17_java" ]]; then
        printf '%s\n' "$jdk17_java"
        return 0
    fi

    command -v java
}

find_port_8080_pids() {
    if command -v lsof >/dev/null 2>&1; then
        lsof -tiTCP:8080 -sTCP:LISTEN 2>/dev/null | sort -u
    elif command -v ss >/dev/null 2>&1; then
        ss -ltnp 2>/dev/null | awk '$4 ~ /:8080$/ {print}' | sed -n 's/.*pid=\([0-9][0-9]*\).*/\1/p' | sort -u
    elif command -v netstat >/dev/null 2>&1; then
        netstat -ltnp 2>/dev/null | awk '$4 ~ /:8080$/ && $6 == "LISTEN" {split($7,a,"/"); if (a[1] != "-") print a[1]}' | sort -u
    fi
}

echo "${CYAN}======================================================${RESET}"
echo "   ${CYAN}PSM-Smart 变电站监控系统 - 演示模式${RESET}"
echo "${CYAN}======================================================${RESET}"

load_env_file

echo "[${CYAN}1/3${RESET}] Checking Environment..."

if command -v docker >/dev/null 2>&1 && docker version >/dev/null 2>&1; then
    echo "[Status] ${GREEN}Docker detected, using Docker Oracle.${RESET}"

    pushd "$ROOT_DIR" >/dev/null || error_exit "Cannot enter root directory: $ROOT_DIR"

    running_id="$(docker ps -q -f "name=oracle21c" -f "status=running")"
    if [[ -n "$running_id" ]]; then
        echo "[Status] ${GREEN}Container oracle21c is already running.${RESET}"
    else
        exist_id="$(docker ps -aq -f "name=oracle21c")"
        if [[ -n "$exist_id" ]]; then
            echo "[Status] ${YELLOW}Starting stopped container...${RESET}"
            docker start oracle21c >/dev/null || {
                popd >/dev/null
                error_exit "Database failed to start or initialize."
            }
        else
            echo "[Status] ${RED}Container not found, creating...${RESET}"
            if docker compose version >/dev/null 2>&1; then
                docker compose up -d || {
                    popd >/dev/null
                    error_exit "Database failed to start or initialize."
                }
            elif command -v docker-compose >/dev/null 2>&1; then
                docker-compose up -d || {
                    popd >/dev/null
                    error_exit "Database failed to start or initialize."
                }
            else
                popd >/dev/null
                error_exit "Docker Compose was not found."
            fi
        fi
    fi

    popd >/dev/null || true
else
    error_exit "Docker is not available. Please install/start Docker and run docker compose from this project."
fi

echo "[Wait] ${YELLOW}Initializing (20s)...${RESET}"
sleep 20

echo
echo "[${CYAN}2/3${RESET}] Preparing backend executable..."
echo "------------------------------------------------------"
if [[ -f "$TARGET_JAR" ]]; then
    echo "[Status] ${GREEN}Found $JAR_NAME.${RESET}"
elif [[ -f "$SOURCE_JAR" ]]; then
    cp -f "$SOURCE_JAR" "$TARGET_JAR" || error_exit "Failed to sync jar from Core/target."
    echo "[Done] ${GREEN}Sync from source complete.${RESET}"
else
    error_exit "JAR not found! Run './build/build.sh' first."
fi

echo "[Wait] ${YELLOW}Cleaning up port 8080...${RESET}"
mapfile -t target_pids < <(find_port_8080_pids || true)
if (( ${#target_pids[@]} > 0 )); then
    for pid in "${target_pids[@]}"; do
        [[ -n "$pid" ]] || continue
        echo "[Status] ${GREEN}Found process $pid on 8080, terminating...${RESET}"
        kill -9 "$pid" 2>/dev/null || echo "[Warn] ${YELLOW}Failed to terminate process $pid. You may need sudo.${RESET}"
    done
else
    echo "[Status] ${CYAN}Port 8080 is clear.${RESET}"
fi

JAVA_CMD="$(find_java_cmd || true)"
if [[ -z "$JAVA_CMD" ]]; then
    error_exit "java was not found in PATH. Install JDK 17 or newer."
fi
echo "[Status] ${GREEN}Using Java: $JAVA_CMD${RESET}"

echo "[${CYAN}3/3${RESET}] Starting Service..."
if [[ "$BG_MODE" == "true" ]]; then
    echo "[Info] ${YELLOW}Running in BACKGROUND mode.${RESET}"
    nohup "$JAVA_CMD" -Dfile.encoding=UTF-8 \
        -jar "$TARGET_JAR" \
        --spring.config.import="$SPRING_CONFIG_IMPORT" \
        --app.log.dir="$LOG_DIR" \
        > "$LOG_DIR/app.out" 2>&1 &
    app_pid=$!
    echo "$app_pid" > "$LOG_DIR/app.pid"
    echo "[Status] ${GREEN}Started backend process $app_pid.${RESET}"
    echo "[Log] $LOG_DIR/app.out"
    finish
else
    echo "[Info] ${CYAN}Running in FOREGROUND mode.${RESET}"
    "$JAVA_CMD" -Dfile.encoding=UTF-8 \
        -jar "$TARGET_JAR" \
        --spring.config.import="$SPRING_CONFIG_IMPORT" \
        --app.log.dir="$LOG_DIR"
    finish
fi
