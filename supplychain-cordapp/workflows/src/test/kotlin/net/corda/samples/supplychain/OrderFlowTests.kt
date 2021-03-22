package net.corda.samples.supplychain

import net.corda.core.node.services.queryBy
import net.corda.samples.supplychain.states.OrderState
import net.corda.testing.internal.chooseIdentity
import org.junit.Test
import kotlin.test.assertEquals

class OrderFlowTests: FlowTestsBaseV2() {

    @Test
    fun `order flow creates the correct order in both nodes' vaults when initiator is buyer`() {
        testProposal(true)
    }


    private fun testProposal(isBuyer: Boolean) {
        val good = "Macbook"
        val location = "Hague"
        val counterparty = b.info.chooseIdentity()

        nodeACreatesOrder(isBuyer, good, location, counterparty)

        for (node in listOf(a, b)) {
            node.transaction {
                val orders = node.services.vaultService.queryBy<OrderState>().states
                assertEquals(1, orders.size)
                val order = orders.single().state.data

                assertEquals(good, order.good)
                val (buyer, seller) = when {
                    isBuyer -> listOf(a.info.chooseIdentity(), b.info.chooseIdentity())
                    else -> listOf(b.info.chooseIdentity(), a.info.chooseIdentity(), a.info.chooseIdentity(), b.info.chooseIdentity())
                }

                assertEquals(buyer, order.buyer)
//                assertEquals(proposer, proposal.proposer)
                assertEquals(seller, order.seller)
//                assertEquals(proposee, proposal.proposee)
            }
        }
    }
}