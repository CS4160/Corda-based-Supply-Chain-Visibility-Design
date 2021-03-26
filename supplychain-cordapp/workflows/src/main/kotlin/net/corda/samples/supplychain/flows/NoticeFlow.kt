package net.corda.samples.supplychain.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.samples.supplychain.contracts.OrderAndTransContract
import net.corda.samples.supplychain.states.OrderState
import net.corda.samples.supplychain.states.TransState

object NoticeFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val orderId: UniqueIdentifier, val deliver: Party) : FlowLogic<UniqueIdentifier>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call(): UniqueIdentifier {
            // Retrieving the input from the vault.
            val orderCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(orderId))
            val orderStateAndRef = serviceHub.vaultService.queryBy<OrderState>(orderCriteria).states.single()
            val order = orderStateAndRef.state.data


            // Creating the output.
            val output = TransState(order.buyer, order.seller, deliver = deliver , good = order.good, itinerary = order.itinerary,
                linearId = orderId, status = "Load"
            )

            // Creating the command.
            val requiredSigners = listOf(order.seller.owningKey, deliver.owningKey)
            val command = Command(OrderAndTransContract.Commands.Load(), requiredSigners)

            // Building the transaction.
//            val notary = orderStateAndRef.state.notary
            val notary = serviceHub.networkMapCache.notaryIdentities.single()
            val txBuilder = TransactionBuilder(notary)
//            txBuilder.addInputState(inputStateAndRef)
            txBuilder.addOutputState(output, OrderAndTransContract.ID)
            txBuilder.addCommand(command)

            // Signing the transaction ourselves.
            val partStx = serviceHub.signInitialTransaction(txBuilder)

            // Gathering the counterparty's signature.
//            val counterparty = deliver
//            val counterpartySession = initiateFlow(counterparty)
//            val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))


            val otherDistributors = output.participants
            val sessionWithOtherDistributors = otherDistributors
                .filterNot { it == ourIdentity }
                .map { initiateFlow(it) }
            val fullyStx = subFlow(CollectSignaturesFlow(partStx, sessionWithOtherDistributors))

            // Finalising the transaction.


            // Finalising the transaction.
//            val finalisedTx = subFlow(FinalityFlow(fullyStx, listOf(counterpartySession)))
            val finalisedTx = subFlow(FinalityFlow(fullyStx, sessionWithOtherDistributors))
            return finalisedTx.tx.outputsOfType<TransState>().single().linearId
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    // No checking to be done.
                }
            }

            val txId = subFlow(signTransactionFlow).id

            subFlow(ReceiveFinalityFlow(counterpartySession, txId))
        }
    }
}
