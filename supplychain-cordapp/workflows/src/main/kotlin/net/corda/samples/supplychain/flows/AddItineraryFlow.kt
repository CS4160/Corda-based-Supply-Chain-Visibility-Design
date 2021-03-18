package net.corda.samples.supplychain.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.samples.supplychain.contracts.OrderAndTransContract
import net.corda.samples.supplychain.states.Itinerary
import net.corda.samples.supplychain.states.TransState
import java.util.*

object AddItineraryFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val OrderId: UniqueIdentifier, val expectedTime: Date) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            // Retrieving the input from the vault.
            val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(OrderId))
            val inputStateAndRef = serviceHub.vaultService.queryBy<TransState>(inputCriteria).states.single()
            val input = inputStateAndRef.state.data

            // Creating the output.
            val new_itinerary = Itinerary(input.itinerary.location,expectedTime,null)
            val output = TransState(input.buyer, input.seller, input.deliver,input.good,new_itinerary, linearId = OrderId, status = "AddItinerary")


            // Creating the command.
            val requiredSigners = listOf(input.buyer.owningKey, input.seller.owningKey, input.deliver.owningKey)
            val command = Command(OrderAndTransContract.Commands.AddItinerary(), requiredSigners)

            // Building the transaction.
            val notary = inputStateAndRef.state.notary
            val txBuilder = TransactionBuilder(notary)
            txBuilder.addInputState(inputStateAndRef)
            txBuilder.addOutputState(output, OrderAndTransContract.ID)
            txBuilder.addCommand(command)

            // Signing the transaction ourselves.
            val partStx = serviceHub.signInitialTransaction(txBuilder)

            // Gathering the counterparty's signature.
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
                    val deliver = ledgerTx.inputsOfType<TransState>().single().deliver
                    if (deliver != counterpartySession.counterparty) {
                        throw FlowException("Only the deliver can add the itinerary items .")
                    }
                }
            }

            val txId = subFlow(signTransactionFlow).id

            subFlow(ReceiveFinalityFlow(counterpartySession, txId))
        }
    }
}
