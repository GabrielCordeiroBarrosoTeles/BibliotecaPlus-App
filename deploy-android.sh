#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════════════════════
#  deploy-android.sh — BibliotecaPlus
#  Detecta o IP local da máquina e injeta como BASE_URL no build do Android.
#  Suporte: macOS e Linux
# ═══════════════════════════════════════════════════════════════════════════════
set -euo pipefail

# ─── Cores ────────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ─── Config ───────────────────────────────────────────────────────────────────
API_PORT="${API_PORT:-4000}"
BUILD_TYPE="${BUILD_TYPE:-debug}"           # debug | release
APP_DIR="$(cd "$(dirname "$0")/app" && pwd)"
GRADLE_WRAPPER="$APP_DIR/gradlew"

# ─── Detectar IP local ────────────────────────────────────────────────────────
detect_local_ip() {
  local ip=""

  if [[ "$(uname)" == "Darwin" ]]; then
    # macOS: tenta Wi-Fi (en0) depois Ethernet (en1) depois qualquer ativa
    ip=$(ipconfig getifaddr en0 2>/dev/null) \
      || ip=$(ipconfig getifaddr en1 2>/dev/null) \
      || ip=$(ipconfig getifaddr en2 2>/dev/null) \
      || true

    if [[ -z "$ip" ]]; then
      # Fallback: rota padrão
      ip=$(route get default 2>/dev/null | awk '/interface:/ {print $2}' \
           | xargs -I{} ipconfig getifaddr {} 2>/dev/null) || true
    fi
  else
    # Linux: pega o IP da interface com rota padrão
    local iface
    iface=$(ip route show default 2>/dev/null | awk '/default/ {print $5; exit}') || true
    if [[ -n "$iface" ]]; then
      ip=$(ip addr show "$iface" 2>/dev/null \
           | awk '/inet / {gsub(/\/.*/, "", $2); print $2; exit}') || true
    fi

    # Último recurso: hostname -I
    if [[ -z "$ip" ]]; then
      ip=$(hostname -I 2>/dev/null | awk '{print $1}') || true
    fi
  fi

  echo "$ip"
}

# ─── Banner ───────────────────────────────────────────────────────────────────
echo ""
echo -e "${CYAN}${BOLD}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}${BOLD}║        BibliotecaPlus — Android Deploy           ║${NC}"
echo -e "${CYAN}${BOLD}╚══════════════════════════════════════════════════╝${NC}"
echo ""

# ─── Validações ───────────────────────────────────────────────────────────────
if [[ ! -d "$APP_DIR" ]]; then
  echo -e "${RED}[ERRO] Diretório 'app/' não encontrado: $APP_DIR${NC}"
  exit 1
fi

if [[ ! -f "$GRADLE_WRAPPER" ]]; then
  echo -e "${RED}[ERRO] gradlew não encontrado em: $GRADLE_WRAPPER${NC}"
  exit 1
fi

# ─── Detectar IP ──────────────────────────────────────────────────────────────
LOCAL_IP=$(detect_local_ip)

if [[ -z "$LOCAL_IP" ]]; then
  echo -e "${RED}[ERRO] Não foi possível detectar o IP local.${NC}"
  echo -e "${YELLOW}Defina manualmente: LOCAL_IP=192.168.x.x $0${NC}"
  exit 1
fi

BASE_URL="http://${LOCAL_IP}:${API_PORT}/api/v1/"

echo -e "${GREEN}[OK]${NC} IP detectado:   ${BOLD}${LOCAL_IP}${NC}"
echo -e "${GREEN}[OK]${NC} API porta:       ${BOLD}${API_PORT}${NC}"
echo -e "${GREEN}[OK]${NC} BASE_URL:        ${BOLD}${BASE_URL}${NC}"
echo -e "${GREEN}[OK]${NC} Build type:      ${BOLD}${BUILD_TYPE}${NC}"
echo ""

# ─── Verificar se a API está acessível ────────────────────────────────────────
echo -e "${YELLOW}[~]${NC} Verificando se a API está rodando..."
if curl -sf --max-time 3 "http://${LOCAL_IP}:${API_PORT}/api/v1/health" > /dev/null 2>&1; then
  echo -e "${GREEN}[OK]${NC} API respondendo em http://${LOCAL_IP}:${API_PORT}"
else
  echo -e "${YELLOW}[AVISO]${NC} API não respondeu (pode ainda estar iniciando)"
  echo -e "         Certifique-se de que ${BOLD}docker compose up${NC} está rodando em api/"
  echo ""
fi

# ─── Confirmar antes de buildar ───────────────────────────────────────────────
if [[ "${SKIP_CONFIRM:-0}" != "1" ]]; then
  echo -e "${YELLOW}Continuar com o build Android? [s/N]${NC} \c"
  read -r answer
  if [[ ! "$answer" =~ ^[sSyY]$ ]]; then
    echo "Build cancelado."
    exit 0
  fi
fi

# ─── Build ────────────────────────────────────────────────────────────────────
echo ""
echo -e "${CYAN}[~]${NC} Iniciando Gradle build (${BUILD_TYPE})..."
echo ""

cd "$APP_DIR"
chmod +x gradlew

TASK="assemble$(tr '[:lower:]' '[:upper:]' <<< "${BUILD_TYPE:0:1}")${BUILD_TYPE:1}"

./gradlew "$TASK" \
  -PbaseUrl="$BASE_URL" \
  --stacktrace \
  2>&1 | grep -E "(BUILD|FAILED|ERROR|> Task|Installing)" || true

# captura exit code real do gradle (não do grep)
./gradlew "$TASK" -PbaseUrl="$BASE_URL" --quiet

# ─── Localizar APK gerado ─────────────────────────────────────────────────────
APK_PATH=$(find "$APP_DIR/app/build/outputs/apk/${BUILD_TYPE}" \
           -name "*.apk" 2>/dev/null | head -1)

echo ""
if [[ -n "$APK_PATH" ]]; then
  echo -e "${GREEN}${BOLD}[BUILD CONCLUÍDO]${NC}"
  echo -e "APK: ${BOLD}${APK_PATH}${NC}"
  echo ""

  # ─── Instalar via ADB se dispositivo conectado ──────────────────────────────
  if command -v adb &> /dev/null; then
    DEVICES=$(adb devices 2>/dev/null | grep -v "List of" | grep "device$" | wc -l)
    if [[ "$DEVICES" -gt 0 ]]; then
      echo -e "${CYAN}[~]${NC} Dispositivo Android detectado. Instalando APK..."
      adb install -r "$APK_PATH"
      echo -e "${GREEN}[OK]${NC} APK instalado com sucesso!"
      echo ""
      echo -e "${GREEN}[INFO]${NC} O app vai conectar em: ${BOLD}${BASE_URL}${NC}"
    else
      echo -e "${YELLOW}[INFO]${NC} Nenhum dispositivo ADB conectado."
      echo -e "         Copie o APK para seu dispositivo manualmente:"
      echo -e "         ${BOLD}${APK_PATH}${NC}"
    fi
  else
    echo -e "${YELLOW}[INFO]${NC} ADB não encontrado no PATH."
    echo -e "         APK gerado em: ${BOLD}${APK_PATH}${NC}"
  fi
else
  echo -e "${RED}[ERRO]${NC} APK não encontrado após o build."
  exit 1
fi

echo ""
echo -e "${CYAN}${BOLD}═══════════════════════════════════════════════════${NC}"
echo -e "${CYAN}${BOLD}  Dispositivos na mesma rede Wi-Fi conectarão em:${NC}"
echo -e "${CYAN}${BOLD}  ${BASE_URL}${NC}"
echo -e "${CYAN}${BOLD}═══════════════════════════════════════════════════${NC}"
echo ""
