#!/bin/bash
set -e
export PROJECT_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && cd .. && pwd )"

CURRENT_USER=${USER:-default}

cd $PROJECT_HOME/grabbill-backend
mvn -pl grabbill-server install -am -Dmaven.test.skip=true
mvn -pl grabbill-server spring-boot:run -Dspring-boot.run.profiles=dev-$CURRENT_USER
