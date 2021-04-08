<!---

# Network Map Service

The network map service is cloned from this github https://github.com/roastario/spring-boot-network-map. Although the author provides an example of using docker, we prefer not to use docker right now. It would be much easier to run shell script directly. 

In the following instructions, we will run a network map service first and then add nodes to the network one by one.

## Envrionment
#### Ubuntu 18.04

## STEP 1: Build network map servie and create a notary

```bash
sh run_build_notary_nms.sh
```

## STEP 2: Run network map service
```bash
sh run_network_map_service.sh
```

## STEP 3: Run notary
```bash
cd notary
sh run-node.sh
```

## STEP 4: Build the Cordapp and Predefined Nodes
The predefined nodes are configured in <path-to-CS4160>/CS4160/supplychain-cordapp/
build.gradle 
```bash
cd <path-to-CS4160>/CS4160/supplychain-cordapp/
./gradlew deployNodes
cd <path-to-CS4160>/CS4160/supplychain-cordapp/network_map_service/
cp -r <path-to-CS4160>/CS4160/supplychain-cordapp/build/nodes/${PartyName} .
```
Before we run the script for cloning, we need to carefully edit several variables in run_clone_node.sh. 
Here is an example. Please note that the port should not be reused. In other words, you need to use different port for different nodes.
```bash
CLONED_NODE_NAME="Alice"
NEW_NODE_NAME="AliceCopy"
PUBLIC_ADDRESS="127.0.0.1"
P2P_PORT="10030"
RPC_ADDR_PORT="10031"
RPC_ADMIN_ADDR_PORT="10060"
```
After the configuration, we can clone and register the new node to the network map server now.
```bash
sh run_clone_node.sh
``` 

## STEP 5: RUN the NEW Registered Node
```bash
cd ${NewPartyName}
java -jar corda.jar
```

## STEP 6: 
Repeat the step 5 if you need to create another new node.

--->