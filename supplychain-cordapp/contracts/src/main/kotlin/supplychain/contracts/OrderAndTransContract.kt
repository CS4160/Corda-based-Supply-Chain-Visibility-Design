package net.corda.samples.supplychain.contracts

import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction
import net.corda.samples.supplychain.states.OrderState
import net.corda.samples.supplychain.states.TransState

class OrderAndTransContract : Contract {
    companion object {
        const val ID = "net.corda.samples.supplychain.contracts.OrderAndTransContract"
    }

    // A transaction is considered valid if the verify() function of the contract of each of the transaction's input
    // and output states does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val cmd = tx.commands.requireSingleCommand<Commands>()
        "There is exactly one output" using (tx.outputStates.size == 1)
        "There is exactly one command" using (tx.commands.size == 1)
        "There is no timestamp" using (tx.timeWindow == null)

        when (cmd.value) {
            is Commands.Order -> requireThat {
                "There are no inputs" using (tx.inputStates.isEmpty())
                "The single output is of type OrderState" using (tx.outputsOfType<OrderState>().size == 1)
                val output = tx.outputsOfType<OrderState>().single()
//                "The buyer and seller are the proposer and the proposee" using (setOf(output.buyer, output.seller) == setOf(output.proposer, output.proposee))
                "The status is initial" using(output.status == "Initial")

                "The buyer is a required signer" using (cmd.signers.contains(output.buyer.owningKey))
                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
            }

            is Commands.Load -> requireThat {
//                "There is exactly one input" using (tx.inputStates.size == 1)
//                "The single input is of type OrderState" using (tx.inputsOfType<OrderState>().size == 1)
                "There are no inputs" using (tx.inputStates.isEmpty())
                "The single output is of type TransState" using (tx.outputsOfType<TransState>().size == 1)

//                val input = tx.inputsOfType<OrderState>().single()
                val output = tx.outputsOfType<TransState>().single()

//                "The good is unmodified in the output" using (output.good == input.good)
//                "The seller is unmodified in the output" using (input.seller == output.seller)
                "The status is Load" using(output.status == "Load")
//                "The deliver is not empty in the output" using (output.deliver!=null)

                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
                "The deliver is a required signer" using (cmd.signers.contains(output.deliver.owningKey))
            }

            is Commands.AddItinerary -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type TransState" using (tx.inputsOfType<TransState>().size == 1)
                "The single output is of type TransState" using (tx.outputsOfType<TransState>().size == 1)


                val output = tx.outputsOfType<TransState>().single()
                val input = tx.inputsOfType<TransState>().single()

                "The good is unmodified in the output" using (output.good == input.good)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)
                "The itinerary now has a expected time" using (output.itinerary.expectedTime!=null)
                "The input status is AddItinerary" using(input.status == "Load")
                "The output status is AddItinerary" using(output.status == "AddItinerary")



                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
                "The deliver is a required signer" using (cmd.signers.contains(output.deliver.owningKey))
            }

            is Commands.Update -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type OrderState" using (tx.inputsOfType<OrderState>().size == 1)
                "The single output is of type OrderState" using (tx.outputsOfType<OrderState>().size == 1)


                val output = tx.outputsOfType<OrderState>().single()
                val input = tx.inputsOfType<OrderState>().single()

                "The good is unmodified in the output" using (output.good == input.good)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)
                "The itinerary now has an expected time" using (output.itinerary.expectedTime!=null)
                "The input status is Update" using(input.status == "Initial")
                "The output status is Update" using(output.status == "Updated")



                "The buyer is a required signer" using (cmd.signers.contains(output.buyer.owningKey))
                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
            }

            is Commands.Arrive -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type TransState" using (tx.inputsOfType<TransState>().size == 1)
                "The single output is of type TransState" using (tx.outputsOfType<TransState>().size == 1)


                val output = tx.outputsOfType<TransState>().single()
                val input = tx.inputsOfType<TransState>().single()

                "The good is unmodified in the output" using (output.good == input.good)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)
                "The itinerary now has an expected time" using (output.itinerary.expectedTime!=null)
                "The itinerary now has an actual time" using (output.itinerary.actualTime!=null)
                "The input status is AddItinerary" using(input.status == "AddItinerary")
                "The output status is Arrival" using(output.status == "Arrival")



                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
                "The deliver is a required signer" using (cmd.signers.contains(output.deliver.owningKey))
            }

            is Commands.Deliver -> requireThat {
                "There is exactly one input" using (tx.inputStates.size == 1)
                "The single input is of type OrderState" using (tx.inputsOfType<OrderState>().size == 1)
                "The single output is of type OrderState" using (tx.outputsOfType<OrderState>().size == 1)


                val output = tx.outputsOfType<OrderState>().single()
                val input = tx.inputsOfType<OrderState>().single()

                "The good is unmodified in the output" using (output.good == input.good)
                "The buyer is unmodified in the output" using (input.buyer == output.buyer)
                "The seller is unmodified in the output" using (input.seller == output.seller)
                "The itinerary now has an expected time" using (output.itinerary.expectedTime!=null)
                "The itinerary now has an actual time" using (output.itinerary.actualTime!=null)
                "The input status is Updated" using(input.status == "Updated")
                "The output status is Complete" using(output.status == "Completed")



                "The buyer is a required signer" using (cmd.signers.contains(output.buyer.owningKey))
                "The seller is a required signer" using (cmd.signers.contains(output.seller.owningKey))
            }
            else -> require(false) {
                "Unsupported command"
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


