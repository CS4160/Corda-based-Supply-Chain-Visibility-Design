package net.corda.samples.supplychain.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.samples.supplychain.contracts.OrderAndTransContract


@BelongsToContract(OrderAndTransContract::class)
data class TransState(
        val buyer: Party,
        val seller: Party,
        val deliver: Party,
        val good: String,
        val itinerary: Itinerary,
        val status: String,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants = listOf(seller,deliver)
}