package net.corda.samples.negotiation

import net.corda.core.node.services.queryBy
import net.corda.samples.negotiation.states.OrderState
import net.corda.samples.negotiation.states.TransState
import net.corda.testing.internal.chooseIdentity
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class UpdateFlowTests: FlowTestsBaseV2() {

    @Test
    fun `test update flow`() {
        testUpdate()
    }


    private fun testUpdate() {
        val counterparty = b.info.chooseIdentity()
        val deliver = c.info.chooseIdentity()

        val orderId = nodeACreatesOrder(true, "macbook", "Delft",counterparty)
        nodeBNoticeLoad(orderId,deliver)
        val expectedTime = Date()
        nodeCAddItinerary(orderId,expectedTime = expectedTime)
        nodeBUpdate(orderId)

        for (node in listOf(a, b)) {
            node.transaction {
                val orders = node.services.vaultService.queryBy<OrderState>().states
                assertEquals(1, orders.size)
                val order = orders.single().state.data


                val (buyer, seller) = listOf(a.info.chooseIdentity(),b.info.chooseIdentity())

                assertEquals(buyer, order.buyer)
                assertEquals(seller, order.seller)
//                assertEquals(deliver, order.seller)
            }
        }
    }
}