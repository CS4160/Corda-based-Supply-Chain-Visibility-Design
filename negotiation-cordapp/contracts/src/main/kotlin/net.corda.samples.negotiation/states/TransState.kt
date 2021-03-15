package net.corda.samples.negotiation.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.samples.negotiation.contracts.OrderAndTransContract


@BelongsToContract(OrderAndTransContract::class)
data class TransState(
        val buyer: Party,
        val seller: Party,
        val deliver: Party,
        val good: String,
        val itinerary: Itinerary,
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants = listOf(buyer,seller,deliver)
}