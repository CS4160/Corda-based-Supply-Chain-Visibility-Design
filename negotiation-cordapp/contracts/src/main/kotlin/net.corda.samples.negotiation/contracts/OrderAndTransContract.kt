package net.corda.samples.negotiation.contracts

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.samples.negotiation.states.OrderState
import net.corda.samples.negotiation.states.TransState

class OrderAndTransContract : Contract {
    companion object {
        const val ID = "net.corda.samples.negotiation.contracts.OrderAndTransContract"
    }

    // A transaction is considered valid if the verify() function of the contract of each of the transaction's input
    // and output states does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val cmd = tx.commands.requireSingleCommand<Commands>()

        when (cmd.value) {
            is Commands.Order -> requireThat {
                "There are no inputs" using (tx.inputStates.isEmpty())
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type OrderState" using (tx.outputsOfType<OrderState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<OrderState>().single()
//                "The buyer and seller are the proposer and the proposee" using (setOf(output.buyer, output.seller) == setOf(output.proposer, output.proposee))

                "The buyer is a required signer" using (cmd.signers.contains(output.buyer.owningKey))
                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
            }

            is Commands.Load -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type OrderState" using (tx.inputsOfType<OrderState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type TransState" using (tx.outputsOfType<TransState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val input = tx.inputsOfType<OrderState>().single()
                val output = tx.outputsOfType<TransState>().single()

                "The good is unmodified in the output" using (output.good == input.good)
                "The seller is unmodified in the output" using (input.seller == output.seller)
//                "The deliver is not empty in the output" using (output.deliver!=null)

                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
                "The deliver is a required signer" using (cmd.signers.contains(output.deliver.owningKey))
            }

            is Commands.AddItinerary -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type TransState" using (tx.inputsOfType<TransState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type TransState" using (tx.outputsOfType<TransState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<TransState>().single()
                val input = tx.inputsOfType<TransState>().single()

                "The good is unmodified in the output" using (output.good == input.good)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)
                "The itinerary now has a expected time" using (output.itinerary.expectedTime!=null)


                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
                "The deliver is a required signer" using (cmd.signers.contains(output.deliver.owningKey))
            }

            is Commands.Update -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type TransState" using (tx.inputsOfType<TransState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type OrderState" using (tx.outputsOfType<OrderState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<OrderState>().single()
                val input = tx.inputsOfType<TransState>().single()

                "The good is unmodified in the output" using (output.good == input.good)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)
                "The itinerary now has an expected time" using (output.itinerary.expectedTime!=null)


                "The buyer is a required signer" using (cmd.signers.contains(output.buyer.owningKey))
                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
            }

            is Commands.Arrive -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type TransState" using (tx.inputsOfType<TransState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type TransState" using (tx.outputsOfType<TransState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<TransState>().single()
                val input = tx.inputsOfType<TransState>().single()

                "The good is unmodified in the output" using (output.good == input.good)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)
                "The itinerary now has an expected time" using (output.itinerary.expectedTime!=null)
                "The itinerary now has an actual time" using (output.itinerary.actualTime!=null)


                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
                "The deliver is a required signer" using (cmd.signers.contains(output.deliver.owningKey))
            }

            is Commands.Deliver -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type TransState" using (tx.inputsOfType<TransState>().size == 1)
                "There is exactly one output" using (tx.outputStates.size == 1)
                "The single output is of type OrderState" using (tx.outputsOfType<OrderState>().size == 1)
                "There is exactly one command" using (tx.commands.size == 1)
                "There is no timestamp" using (tx.timeWindow == null)

                val output = tx.outputsOfType<OrderState>().single()
                val input = tx.inputsOfType<TransState>().single()

                "The good is unmodified in the output" using (output.good == input.good)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)
                "The itinerary now has an expected time" using (output.itinerary.expectedTime!=null)
                "The itinerary now has an actual time" using (output.itinerary.actualTime!=null)


                "The buyer is a required signer" using (cmd.signers.contains(output.buyer.owningKey))
                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
            }
        }
    }

    // Used to indicate the transaction's intent.
    sealed class Commands : TypeOnlyCommandData() {
        class Order : Commands()
        class Load : Commands()
        class AddItinerary : Commands()
        class Update: Commands()
        class Arrive: Commands()
        class Deliver: Commands()
    }
}


