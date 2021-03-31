#!/usr/bin/env bash

PUBLIC_ADDRESS="127.0.0.1"
CORDA_JAR_NAME="corda.jar"
DOORMAN_CN_TO_USE=${DOORMAN_CN:-BasicDoorman}
NETWORKMAP_CN_TO_USE=${NETWORKMAP_CN:-BasicNetworkMap}

rm -rf notary
rm -rf spring-boot-network-map

(
  mkdir notary
  cd notary || exit

  if [[ ! -f ${CORDA_JAR_NAME} ]]; then
    wget -O ${CORDA_JAR_NAME} https://ci-artifactory.corda.r3cev.com/artifactory/corda-releases/net/corda/corda/4.5.1/corda-4.5.1.jar
  fi

  cat >node.conf <<EOT
myLegalName="O=Notary Service,L=London,C=GB"
notary {
    validating=false
}
p2pAddress="${PUBLIC_ADDRESS}:10200"
rpcSettings {
    address="localhost:10003"
    adminAddress="localhost:10004"
}
detectPublicIp=false
rpcUsers=[]
devMode=true
compatibilityZoneURL="http://${PUBLIC_ADDRESS}:8080"
devModeOptions{
    allowCompatibilityZone=true
}
EOT

  java -jar ${CORDA_JAR_NAME} generate-node-info
)

(
  git clone https://github.com/roastario/spring-boot-network-map.git
  cd spring-boot-network-map || exit
  ./gradlew clean build
  cp build/libs/spring-boot-network-map-1.0-SNAPSHOT.jar ../nms.jar
)

(
  cd notary || exit

  cat >run-node.sh <<EOF
while :
do
  rm -f network-parameters
  java -jar corda.jar
done
EOF

)
