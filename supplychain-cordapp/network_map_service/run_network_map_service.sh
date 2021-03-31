#!/usr/bin/env bash

PUBLIC_ADDRESS="127.0.0.1"
CORDA_JAR_NAME="corda.jar"
DOORMAN_CN_TO_USE=${DOORMAN_CN:-BasicDoorman}
NETWORKMAP_CN_TO_USE=${NETWORKMAP_CN:-BasicNetworkMap}

java -Djava.security.egd=file:/dev/urandom -jar nms.jar --nodesDirectoryUrl=file:notary/ --doormanCN="${DOORMAN_CN_TO_USE}" --networkMapCN="${NETWORKMAP_CN_TO_USE}" &
NMS_PID=$!

let EXIT_CODE=255
while [ ${EXIT_CODE} -gt 0 ]; do
  sleep 2
  echo "Waiting for network map to start"
  wget -t 1 -O /dev/null http://localhost:8080/network-map
  let EXIT_CODE=$?
done

(
  echo "Started nms with PID=${NMS_PID}"

  function ctrl_c() {
    kill ${NMS_PID}
    wait ${NMS_PID}
    kill $$
  }

  # trap ctrl-c and call ctrl_c()
  trap ctrl_c INT

  while :; do
    sleep 1
  done
)
