package net.corda.samples.supplychain.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.samples.supplychain.states.TransState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.samples.supplychain.contracts.OrderAndTransContract
import net.corda.samples.supplychain.states.OrderState

object UpdateFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val orderId: UniqueIdentifier) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            // Retrieving the input from the vault.
            val orderCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(orderId))
            val orderStateAndRef= serviceHub.vaultService.queryBy<OrderState>(orderCriteria).states.single()
            val order = orderStateAndRef.state.data

            val TransCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(orderId))
            val TransStateAndRef = serviceHub.vaultService.queryBy<TransState>(TransCriteria).states.single()
            val transState = TransStateAndRef.state.data


            // Creating the output.
            val output = OrderState(order.buyer, order.seller, transState.deliver,order.good, transState.itinerary, status = "Updated", linearId = orderId)

            // Creating the command.
            val requiredSigners = listOf(order.buyer.owningKey, order.seller.owningKey)
            val command = Command(OrderAndTransContract.Commands.Update(), requiredSigners)

            // Building the transaction.
            val notary = orderStateAndRef.state.notary
            val txBuilder = TransactionBuilder(notary)
            txBuilder.addInputState(orderStateAndRef)
            txBuilder.addOutputState(output, OrderAndTransContract.ID)
            txBuilder.addCommand(command)

            // Signing the transaction ourselves.
            val partStx = serviceHub.signInitialTransaction(txBuilder)

            // Gathering the counterparty's signature.
//            val counterparty = if (ourIdentity == input.proposer) input.proposee else input.proposer
            val otherDistributors = output.participants
            val sessionWithOtherDistributors = otherDistributors
                .filterNot { it == ourIdentity }
                .map { initiateFlow(it) }
            val fullyStx = subFlow(CollectSignaturesFlow(partStx, sessionWithOtherDistributors))

            // Finalising the transaction.
            subFlow(FinalityFlow(fullyStx, sessionWithOtherDistributors))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    val ledgerTx = stx.toLedgerTransaction(serviceHub, false)
                    val seller = ledgerTx.inputsOfType<OrderState>().single().seller
                    if (seller != counterpartySession.counterparty) {
                        throw FlowException("Only the seller can send the update information.")
                    }
                }
            }

            val txId = subFlow(signTransactionFlow).id

            subFlow(ReceiveFinalityFlow(counterpartySession, txId))
        }
    }
}
