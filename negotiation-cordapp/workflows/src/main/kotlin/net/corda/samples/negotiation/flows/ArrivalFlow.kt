package net.corda.samples.negotiation.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.samples.negotiation.contracts.OrderAndTransContract
import net.corda.samples.negotiation.states.Itinerary
import net.corda.samples.negotiation.states.TransState
import java.util.*

object ArrivalFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val orderId: UniqueIdentifier, val actualTime: Date) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            // Retrieving the input from the vault.
            val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(orderId))
            val inputStateAndRef = serviceHub.vaultService.queryBy<TransState>(inputCriteria).states.single()
            val input = inputStateAndRef.state.data

            // Creating the output.
            val new_itinerary = Itinerary(input.itinerary.location, input.itinerary.expectedTime, actualTime)
            val output = TransState(input.buyer,input.seller, input.deliver, input.good, new_itinerary)

            // Creating the command.
            val requiredSigners = listOf(input.seller.owningKey, input.deliver.owningKey)
            val command = Command(OrderAndTransContract.Commands.Arrive(), requiredSigners)

            // Building the transaction.
            val notary = inputStateAndRef.state.notary
            val txBuilder = TransactionBuilder(notary)
            txBuilder.addInputState(inputStateAndRef)
            txBuilder.addOutputState(output, OrderAndTransContract.ID)
            txBuilder.addCommand(command)

            // Signing the transaction ourselves.
            val partStx = serviceHub.signInitialTransaction(txBuilder)

            // Gathering the counterparty's signature.
//            val counterparty = if (ourIdentity == input.proposer) input.proposee else input.proposer
            val counterparty = input.seller
            val counterpartySession = initiateFlow(counterparty)
            val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

            // Finalising the transaction.
            subFlow(FinalityFlow(fullyStx, listOf(counterpartySession)))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    val ledgerTx = stx.toLedgerTransaction(serviceHub, false)
                    val deliver = ledgerTx.inputsOfType<TransState>().single().deliver
                    if (deliver != counterpartySession.counterparty) {
                        throw FlowException("Only the deliver can send arrival information.")
                    }
                }
            }

            val txId = subFlow(signTransactionFlow).id

            subFlow(ReceiveFinalityFlow(counterpartySession, txId))
        }
    }
}
