package net.corda.samples.negotiation

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.samples.negotiation.flows.*
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import java.util.*

abstract class FlowTestsBaseV2 {
    protected lateinit var network: MockNetwork
    protected lateinit var a: StartedMockNode
    protected lateinit var b: StartedMockNode
    protected lateinit var c: StartedMockNode


    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("net.corda.samples.negotiation.flows"),
                TestCordapp.findCordapp("net.corda.samples.negotiation.contracts"))))
        a = network.createPartyNode()
        b = network.createPartyNode()
        c = network.createPartyNode()


        val responseFlows = listOf(OrderFlow.Responder::class.java, NoticeFlow.Responder::class.java, AddItineraryFlow.Responder::class.java, UpdateFlow.Responder::class.java,
            ArrivalFlow.Responder::class.java,CompleteFlow.Responder::class.java
        )
        listOf(a, b, c).forEach {
            for (flow in responseFlows) {
                it.registerInitiatedFlow(flow)
            }
        }

        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    fun nodeACreatesOrder(isBuyer: Boolean, good: String, location:String, counterparty: Party): UniqueIdentifier {
        val flow = OrderFlow.Initiator(isBuyer, good, location, counterparty)
        val future = a.startFlow(flow)
        network.runNetwork()
        return future.get()
    }

    fun nodeBNoticeLoad(OrderId: UniqueIdentifier, deliver:Party) {
        val flow = NoticeFlow.Initiator(OrderId,deliver)
        val future = b.startFlow(flow)
        network.runNetwork()
        future.get()

    }

    fun nodeCAddItinerary(OrderId: UniqueIdentifier, expectedTime:Date) {
        val flow = AddItineraryFlow.Initiator(OrderId, expectedTime)
        val future = c.startFlow(flow)
        network.runNetwork()
        future.get()
    }


    fun nodeBUpdate(OrderId: UniqueIdentifier) {
        val flow = UpdateFlow.Initiator(OrderId)
        val future = b.startFlow(flow)
        network.runNetwork()
        future.get()
    }

    fun nodeCArrival(OrderId: UniqueIdentifier, actualTime:Date) {
        val flow = ArrivalFlow.Initiator(OrderId,actualTime)
        val future = c.startFlow(flow)
        network.runNetwork()
        future.get()
    }

    fun nodeBComplete(OrderId: UniqueIdentifier) {
        val flow = CompleteFlow.Initiator(OrderId)
        val future = b.startFlow(flow)
        network.runNetwork()
        future.get()
    }
}