package net.corda.samples.negotiation

import net.corda.core.node.services.queryBy
import net.corda.samples.negotiation.states.TransState
import net.corda.testing.internal.chooseIdentity
import org.junit.Test
import kotlin.test.assertEquals

class NoticeFlowTests: FlowTestsBaseV2() {

    @Test
    fun `test notice flow`() {
        testNotice()
    }


//    @Test
//    fun `modification flow throws an error is the proposer tries to modify the proposal`() {
//        val oldAmount = 1
//        val newAmount = 2
//        val counterparty = b.info.chooseIdentity()
//        val proposalId = nodeACreatesProposal(true, oldAmount, counterparty)
//
//        val flow = ModificationFlow.Initiator(proposalId, newAmount)
//        val future = a.startFlow(flow)
//        network.runNetwork()
//        val exceptionFromFlow = assertFailsWith<ExecutionException> {
//            future.get()
//        }.cause!!
//        assertEquals(FlowException::class, exceptionFromFlow::class)
//        assertEquals("Only the proposee can modify a proposal.", exceptionFromFlow.message)
//    }

    private fun testNotice() {
        val counterparty = b.info.chooseIdentity()
        val deliver = c.info.chooseIdentity()

        val orderId = nodeACreatesOrder(true, "macbook", "Delft",counterparty)
        nodeBNoticeLoad(orderId,deliver)

        for (node in listOf(a,b,c)) {
            node.transaction {
                val orders = node.services.vaultService.queryBy<TransState>().states
                assertEquals(1, orders.size)
                val order = orders.single().state.data

                assertEquals(deliver, order.deliver)
                val (buyer, seller, deliver) = listOf(a.info.chooseIdentity(),b.info.chooseIdentity(),c.info.chooseIdentity())

                assertEquals(buyer, order.buyer)
                assertEquals(seller, order.seller)
//                assertEquals(deliver, order.seller)
            }
        }
    }
}