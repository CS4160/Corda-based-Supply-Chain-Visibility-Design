# CS4160
# A Corda-based Supply Chain Visibility Design

This CorDapp shows how our supply chain visibility blockchain network could complete the transport process.
The Customer, Supplier, and Trucker Interfaces are also available in our CorDapp.

## Usage

### Network Map Service

The network map service is cloned from this Github https://github.com/roastario/spring-boot-network-map. Although the author provides an example of using docker, we prefer not to use docker right now. It would be much easier to run shell script directly. 

In the following instructions, we will run a network map service first and then add nodes to the network one by one.

#### Envrionment
###### Ubuntu 18.04

#### STEP 1: Build network map servie and create a notary

```bash
sh run_build_notary_nms.sh
```

#### STEP 2: Run network map service
```bash
sh run_network_map_service.sh
```

#### STEP 3: Run notary
```bash
cd notary
sh run-node.sh
```

#### STEP 4: Build the Cordapp and Predefined Nodes
The predefined nodes are configured in <path-to-CS4160>/CS4160/supplychain-cordapp/
build.gradle 
```bash
cd <path-to-CS4160>/CS4160/supplychain-cordapp/
./gradlew deployNodes
cd <path-to-CS4160>/CS4160/supplychain-cordapp/network_map_service/
cp -r <path-to-CS4160>/CS4160/supplychain-cordapp/build/nodes/${PartyName} .
```
Before we run the script for cloning, we need to carefully edit several variables in run_clone_node.sh. 
Here is an example. Please note that the port should not be reused. In other words, you need to use a different port for different nodes.
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

#### STEP 5: RUN the NEW Registered Node
```bash
cd ${NewPartyName}
java -jar corda.jar
```

#### STEP 6: 
Repeat the step 5 if you need to create another new node.

#### STEP 7:
After having completed adding new nodes, we should add several TaskrunServer commands manually in the build.gradle file in the clients folder. At this step, we will remember the RPC connection address of each new added nodes. An example of commands is shown as follows:
```
task runAppleServer(type: JavaExec, dependsOn: jar) {
    classpath = sourceSets.main.runtimeClasspath
    main = 'net.corda.samples.supplychain.server.ServerKt'
    args '--server.port=10069', '--config.rpc.host=localhost', '--config.rpc.port=10017', '--config.rpc.username=user1', '--config.rpc.password=test'
}
```
If we want to establish the server for each new nodes, we can simply change the '--config.rpc.port' to the corresponding RPC connection address, and the '--server.port' that is set to be as the front-end port which should be differentiated with each other.
### Running the CorDapp

you can run our CorDapp and interact with it via a web server.

Open a terminal and go to the project root directory and type:
```
./gradlew clean deployNodes
```
Then type:(to run the nodes we have added in the Network Map Service part)
```
./build/nodes/runnodes
```
#### Starting the webserver
Once the nodes are up, we can start the webservers next. Generally, this app consists of one notary and several nodes. The number of nodes depends on how many nodes are added in the Network Map Service procedure.

For instance, we assume there is one buyer node named Alice, one seller node named Apple, and one trucker named Tom.
We should start 3 webservers separately. Open a new tab of the terminal(make sure you are still in the project directory)
and run:

```
./gradlew runAliceServer
```
and 

```
./gradlew runAppleServer
```
and
```
./gradlew runTomServer
```

#### Interacting with the CorDapp
Once all three servers have started up (look for `Webserver started up in XXX sec` in the terminal), you can interact with the app via a web browser.
* From a Node Driver configuration, look for `Starting webserver on address localhost:100XX` for the addresses.

For instance, as for Node Alice:`localhost:10066`, you could see the buyer interface in the webserver as follows:
![image](https://github.com/CS4160/CS4160/blob/front-end-zhuoran/images/buyer.png)

The customer could create an order by clicking the button "create order" at the top of the page. Thencustomer could input order information to create a new order. Significantly, Only the supplier’s information is visiblewhen a customer chooses a supplier.

For Node Apple:`localhost:11000`, you could see the seller interface in the webserver as follows:
![image](https://github.com/CS4160/CS4160/blob/front-end-zhuoran/images/seller1.png)

The Supplier could assign a trucker to a specific order after clicking the " check order" button.Similarly, Only trucker information is visible when the Supplier assigns a trucker to an order.After assigning the trucker function, all the order information will be displayed on the supplier page for tracking theorder information.

For Node Tom:`localhost:10070`, you could see the Trucker interface in the webserver as follows:
![image](https://github.com/CS4160/CS4160/blob/front-end-zhuoran/images/seller2.png)
![image](https://github.com/CS4160/CS4160/blob/front-end-zhuoran/images/trucker1.png)

As the above images show, the Trucker Interface consists of two main parts: Uncompleted Orders and Complete Orders. Initially, As we can see in the Trucker interface, the trucker can click "accept order" button to check all orders assigned to this trucker.  Subsequently, thetrucker inputs the expected time to a specific order and starts shipment. All the orders that are in the shipment will bepresent in the uncompleted orders. A trucker can click "arrive" button when the shipment has been completed, afterwhich time this order is shown in the completed order area.As the requirement of this project, all the buyer’s information is invisible in the trucker interface.

In the Customer Interface, the information of order that this customer creates can be updated synchronously.
![image](https://github.com/CS4160/CS4160/blob/front-end-zhuoran/images/buyer1.png)
