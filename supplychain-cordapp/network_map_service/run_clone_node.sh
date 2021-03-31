#!/bin/bash

CLONED_NODE_NAME="Alice"
NEW_NODE_NAME="AliceCopy"
PUBLIC_ADDRESS="127.0.0.1"
P2P_PORT="10030"
RPC_ADDR_PORT="10031"
RPC_ADMIN_ADDR_PORT="10060"

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
myLegalName="O=$NEW_NODE_NAME,L=London,C=GB"
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
