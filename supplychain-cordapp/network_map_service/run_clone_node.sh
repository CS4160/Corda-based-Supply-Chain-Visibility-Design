#!/bin/bash

CLONED_NODE_NAME="Template"
NEW_NODE_NAME="Tim"
NEW_NODE_CONF_NAME="O=Tim,L=Rotterdam,C=NL,OU=Trucker"
PUBLIC_ADDRESS="127.0.0.1"
P2P_PORT="10027"
RPC_ADDR_PORT="10028"
RPC_ADMIN_ADDR_PORT="10029"

if [ -d $NEW_NODE_NAME ]
then
  echo "$NEW_NODE_NAME already exits"
  exit
fi

cp -r $CLONED_NODE_NAME $NEW_NODE_NAME
cd $NEW_NODE_NAME || exit

declare -a arr=("additional-node-infos" "artemis" "brokers" "certificates" "logs" "shell-commands")
for i in "${arr[@]}"
do
  if [ -d "$i" ] 
  then
    rm -rf $i
  fi
done

declare -a arr=("network-parameters" nodeInfo-* "node.conf" persistence*)
for i in "${arr[@]}"
do
  if [ -f "$i" ] 
  then
    rm $i
  fi
done

mkdir certificates
curl -o certificates/network-root-truststore.jks http://${PUBLIC_ADDRESS}:8080/truststore

(
  cat >node.conf <<EOT
devMode=true
myLegalName="${NEW_NODE_CONF_NAME}"
p2pAddress="${PUBLIC_ADDRESS}:${P2P_PORT}"
rpcSettings {
    address="localhost:${RPC_ADDR_PORT}"
    adminAddress="localhost:${RPC_ADMIN_ADDR_PORT}"
}
security {
    authService {
        dataSource {
            type=INMEMORY
            users=[
                {
                    password=test
                    permissions=[
                        ALL
                    ]
                    user=user1
                }
            ]
        }
    }
}
compatibilityZoneURL="http://${PUBLIC_ADDRESS}:8080"
devModeOptions{
    allowCompatibilityZone=true
}
EOT
)

java -jar corda.jar initial-registration  --network-root-truststore-password trustpass
