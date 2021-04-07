package net.corda.samples.supplychain.server

import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.toX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.NodeInfo
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.samples.supplychain.contracts.OrderAndTransContract
import net.corda.samples.supplychain.flows.*

import net.corda.samples.supplychain.server.NodeRPCConnection
import net.corda.samples.supplychain.states.OrderState
import net.corda.samples.supplychain.states.Itinerary
import net.corda.samples.supplychain.states.TransState
import org.apache.qpid.proton.amqp.transport.End
import net.corda.samples.supplychain.flows.OrderFlow.Initiator as Orderflow
import net.corda.samples.supplychain.flows.NoticeFlow.Initiator as Noticeflow
import net.corda.samples.supplychain.flows.AddItineraryFlow.Initiator as AddItineraryflow
import net.corda.samples.supplychain.flows.UpdateFlow.Initiator as Updateorder
import net.corda.samples.supplychain.flows.ArrivalFlow.Initiator as Arrivalorder
import net.corda.samples.supplychain.flows.CompleteFlow.Initiator as Completeorder
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle


import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*


val SERVICE_NAMES = listOf("Notary", "Network Map Service")

/**
 *  A Spring Boot Server API controller for interacting with the node via RPC.
 */

@RestController
@RequestMapping("/api/iou/") // The paths for requests are relative to this base path.
class MainController(rpc: NodeRPCConnection) {

    private val proxy = rpc.proxy
    private val me = proxy.nodeInfo().legalIdentities.first().name



    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    fun X500Name.toDisplayString() : String  = BCStyle.INSTANCE.toString(this)

    /** Helpers for filtering the network map cache. */
    private fun isNotary(nodeInfo: NodeInfo) = proxy.notaryIdentities().any { nodeInfo.isLegalIdentity(it) }
    private fun isMe(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.first().name == me
    private fun isNetworkMap(nodeInfo : NodeInfo) = nodeInfo.legalIdentities.single().name.organisation == "Network Map Service"


    /**
     * Returns the node's name.
     */
    @GetMapping(value = [ "me" ], produces = [ APPLICATION_JSON_VALUE ])
    fun whoami() = mapOf("me" to me.toString())

    @GetMapping(value = ["identity"], produces = [APPLICATION_JSON_VALUE])
    fun getIdentity(): Map<String, String>  {
        val indexStart = me.toString().indexOf("OU",0)+3
        val indexEnd = me.toString().indexOf(",",0)

        val identity = me.toString().substring(indexStart, indexEnd)
        return mapOf(Pair("identity", identity))
    }

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GetMapping(value = [ "peers" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getPeers(): Map<String, List<String>> {
        return mapOf("peers" to proxy.networkMapSnapshot()
                .filter { isNotary(it).not() && isMe(it).not() && isNetworkMap(it).not() }
                .map { it.legalIdentities.first().name.toX500Name().toDisplayString() })
    }

    /**
     * Task 1
     * Displays all IOU states that exist in the node's vault.
     * TODO: Return a list of IOUStates on ledger
     * Hint - Use [rpcOps] to query the vault all unconsumed [IOUState]s
     */
    @GetMapping(value = [ "orders" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getOrders(@RequestParam(value = "state_type") state_type: String): List<StateAndRef<ContractState>> {
        // Filter by state type: IOU.
        if (state_type == "orderState"){
            return proxy.vaultQueryBy<OrderState>().states
        }
        else{
            return proxy.vaultQueryBy<TransState>().states
        }
    }

    @PutMapping(value = [ "create-order" ], produces = [ TEXT_PLAIN_VALUE ])
    fun createOrder(@RequestParam(value = "location") location: String,
                 @RequestParam(value = "good") good: String,
                 @RequestParam(value = "company") party: String): ResponseEntity<String> {
        // Get party objects for myself and the counterparty.
        val me = proxy.nodeInfo().legalIdentities.first()
        val seller = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(party)) ?: throw IllegalArgumentException("Unknown party name.")
        // Create a new IOU state using the parameters given.
        try {

            val result = proxy.startTrackedFlow(::Orderflow,true,good,location,seller).returnValue.get()
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("order id ${result} has been confirmed by \n${party}")

            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (e: Exception) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.message)

        }
    }

    @PutMapping(value = [ "notice-order" ], produces = [ TEXT_PLAIN_VALUE ])
    fun noticeOrder(@RequestParam(value = "id") id: String,
                    @RequestParam(value = "driver") driver: String): ResponseEntity<String> {
            val linearId = UniqueIdentifier.fromString(id)
            val newdriver = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(driver))
                    ?: throw IllegalArgumentException("Unknown party name.")
            try{
                val result = proxy.startTrackedFlow(::Noticeflow, linearId, newdriver).returnValue.get()
                return ResponseEntity.status(HttpStatus.CREATED).body("Order id $id transferred by $newdriver.")
            } catch (e: Exception) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
            }

    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @PutMapping(value = [ "driver-add" ], produces = [ TEXT_PLAIN_VALUE ])
    fun addItinerary(@RequestParam(value = "id") id: String,
                  @RequestParam(value = "expectedtime") expectedtime:String) {
        val linearId = UniqueIdentifier.fromString(id)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val expectedTime = formatter.parse(expectedtime)
        proxy.startTrackedFlow(::AddItineraryflow, linearId, expectedTime)
//        proxy.startTrackedFlow(::Updateorder,linearId)
    }


    @PutMapping(value = [ "driver-arrival" ], produces = [ TEXT_PLAIN_VALUE ])
    fun arrivalOrder(@RequestParam(value = "id") id: String,
                     @RequestParam(value = "arrivaltime") arrivaltime:String) {
        val linearId = UniqueIdentifier.fromString(id)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val arrivaltime = formatter.parse(arrivaltime)
        proxy.startTrackedFlow(::Arrivalorder, linearId, arrivaltime)
//        proxy.startTrackedFlow(::Completeorder,linearId)
    }

    @PutMapping(value = [ "update-order" ], produces = [ TEXT_PLAIN_VALUE ])
    fun updateOrder(@RequestParam(value = "id") id: String): ResponseEntity<String> {
        val linearId = UniqueIdentifier.fromString(id)
        proxy.startTrackedFlow(::Updateorder,linearId)

        try{
            return ResponseEntity.status(HttpStatus.CREATED).body("Order id $id updated")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }

    }

    @PutMapping(value = [ "complete-order" ], produces = [ TEXT_PLAIN_VALUE ])
    fun completeOrder(@RequestParam(value = "id") id: String): ResponseEntity<String> {
        val linearId = UniqueIdentifier.fromString(id)

        val orderCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val state = proxy.vaultQueryBy<OrderState>(orderCriteria).states.single().state.data

        if(state.status == "Updated") {
            proxy.startTrackedFlow(::Completeorder, linearId)
        }
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body("Order id $id completed")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }

    }





}
