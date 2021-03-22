package net.corda.samples.supplychain.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.samples.supplychain.contracts.OrderAndTransContract

import java.util.*

@CordaSerializable
data class Itinerary(
        val location:String,
        val expectedTime: Date?,
        val actualTime:Date?
)



@BelongsToContract(OrderAndTransContract::class)
data class OrderState(
        val buyer: Party,
        val seller: Party,
        val deliver: Party?,
        val good:String,
        val itinerary: Itinerary,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants = listOf(buyer, seller)
}