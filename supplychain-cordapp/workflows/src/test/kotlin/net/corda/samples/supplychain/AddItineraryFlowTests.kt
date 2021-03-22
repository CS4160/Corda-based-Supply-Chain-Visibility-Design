package net.corda.samples.supplychain

import net.corda.core.node.services.queryBy
import net.corda.samples.supplychain.states.TransState
import net.corda.testing.internal.chooseIdentity
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class AddItineraryFlowTests: FlowTestsBaseV2() {

    @Test
    fun `test add itinerary flow`() {
        testAddItinerary()
    }


    private fun testAddItinerary() {
        val counterparty = b.info.chooseIdentity()
        val deliver = c.info.chooseIdentity()

        val orderId = nodeACreatesOrder(true, "macbook", "Delft",counterparty)
        nodeBNoticeLoad(orderId,deliver)
        val expectedTime = Date()
        nodeCAddItinerary(orderId,expectedTime = expectedTime)

        for (node in listOf(a, b, c)) {
            node.transaction {
                val orders = node.services.vaultService.queryBy<TransState>().states
                assertEquals(1, orders.size)
                val order = orders.single().state.data

                assertEquals(order.itinerary.expectedTime, expectedTime)
                val (buyer, seller, deliver) = listOf(a.info.chooseIdentity(),b.info.chooseIdentity(),c.info.chooseIdentity())

                assertEquals(deliver, order.deliver)
                assertEquals(buyer, order.buyer)
                assertEquals(seller, order.seller)
//                assertEquals(deliver, order.seller)
            }
        }
    }
}