set -e
export USERNAME=${1:-admin}
export FILENAME=${2:-traefik}

echo Add user [${USERNAME}] to [${FILENAME}]...
htpasswd -c ${FILENAME} ${USERNAME}
