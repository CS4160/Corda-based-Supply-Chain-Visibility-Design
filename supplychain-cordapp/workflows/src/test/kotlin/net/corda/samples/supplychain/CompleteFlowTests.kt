package net.corda.samples.supplychain

import net.corda.core.node.services.queryBy
import net.corda.samples.supplychain.states.TransState
import net.corda.testing.internal.chooseIdentity
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class CompleteFlowTests: FlowTestsBaseV2() {

    @Test
    fun `test complete flow`() {
        testComplete()
    }


    private fun testComplete() {
        val counterparty = b.info.chooseIdentity()
        val deliver = c.info.chooseIdentity()

         val orderId = nodeACreatesOrder(true, "macbook", "Delft",counterparty)
        nodeBNoticeLoad(orderId,deliver)
        val expectedTime = Date()
        nodeCAddItinerary(orderId,expectedTime = expectedTime)
        nodeBUpdate(orderId)
        val actualTime = Date()
        nodeCArrival(orderId,actualTime)
        nodeBComplete(orderId)

        for (node in listOf(a, b, c)) {
            node.transaction {
                val orders = node.services.vaultService.queryBy<TransState>().states
                assertEquals(1, orders.size)
                val order = orders.single().state.data
                assertEquals(order.itinerary.actualTime, actualTime)


                val (buyer, seller, deliver) = listOf(a.info.chooseIdentity(),b.info.chooseIdentity(), c.info.chooseIdentity())

                assertEquals(buyer, order.buyer)
                assertEquals(seller, order.seller)
                assertEquals(deliver, order.deliver)
            }
        }
    }
}