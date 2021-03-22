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

object UpdateFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val orderId: UniqueIdentifier) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            // Retrieving the input from the vault.
            val inputCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(orderId))
            val inputStateAndRef= serviceHub.vaultService.queryBy<TransState>(inputCriteria).states.single()
            val input = inputStateAndRef.state.data


            // Creating the output.
            val output = TransState(input.buyer, input.seller, input.deliver,input.good, input.itinerary, linearId = orderId, status = "Update")

            // Creating the command.
            val requiredSigners = listOf(input.buyer.owningKey, input.seller.owningKey, input.deliver.owningKey)
            val command = Command(OrderAndTransContract.Commands.Update(), requiredSigners)

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
            val counterparty = input.buyer
            val counterpartySession = initiateFlow(counterparty)
            val counterpartySession2 = initiateFlow(input.deliver)

            val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession, counterpartySession2)))

            // Finalising the transaction.
            subFlow(FinalityFlow(fullyStx, listOf(counterpartySession, counterpartySession2)))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    val ledgerTx = stx.toLedgerTransaction(serviceHub, false)
                    val seller = ledgerTx.inputsOfType<TransState>().single().seller
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
