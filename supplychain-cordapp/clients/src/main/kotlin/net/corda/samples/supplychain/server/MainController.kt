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
import net.corda.samples.supplychain.contracts.OrderAndTransContract
import net.corda.samples.supplychain.flows.*

//import net.corda.samples.supplychain.server.NodeRPCConnection
import net.corda.samples.supplychain.states.OrderState
import net.corda.samples.supplychain.states.Itinerary
import net.corda.samples.supplychain.states.TransState
import net.corda.samples.supplychain.flows.OrderFlow.Initiator as Orderflow
import net.corda.samples.supplychain.flows.NoticeFlow.Initiator as Noticeflow
import net.corda.samples.supplychain.flows.AddItineraryFlow.Initiator as AddItineraryflow
import net.corda.samples.supplychain.flows.UpdateFlow.Initiator as Updateorder
import net.corda.samples.supplychain.flows.ArrivalFlow.Initiator as Arrivalorder
import net.corda.samples.supplychain.flows.CompleteFlow.Initiator as Completeorder
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle


import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
    fun getOrders(): List<StateAndRef<ContractState>> {
        // Filter by state type: IOU.
        return proxy.vaultQueryBy<OrderState>().states
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
        val newdriver = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(driver)) ?: throw IllegalArgumentException("Unknown party name.")
        return try {
            proxy.startFlow(::Noticeflow, linearId, newdriver).returnValue.get()
            ResponseEntity.status(HttpStatus.CREATED).body("Order id $id transferred by $driver.")

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }


    @PutMapping(value = [ "driver" ], produces = [ TEXT_PLAIN_VALUE ])
    fun addItinerary(@RequestParam(value = "id") id: String,
                  @RequestParam(value = "expectedtime") expectedtime:Date) {
        val linearId = UniqueIdentifier.fromString(id)
        proxy.startFlow(::AddItineraryflow, linearId, expectedtime)
        proxy.startFlow(::Updateorder,linearId)
    }


    @PutMapping(value = [ "driver" ], produces = [ TEXT_PLAIN_VALUE ])
    fun arrivalOrder(@RequestParam(value = "id") id: String,
                     @RequestParam(value = "arrivaltime") arrivaltime:Date) {
        val linearId = UniqueIdentifier.fromString(id)
        proxy.startFlow(::Arrivalorder, linearId, arrivaltime)
        proxy.startFlow(::Completeorder,linearId)
    }



}
