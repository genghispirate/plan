package app.haven.intercept.gate

import app.haven.data.model.GateType

/**
 * Immutable description of a raised Action Gate. Built by the interceptor when a
 * blocked app crosses its limit and handed to the overlay composition.
 *
 * The gate is an *active mental transition* (Section 1.4): the user cannot pass
 * by simply waiting — they must complete the micro-habit for [gateType].
 */
data class ActionGateRequest(
    val packageName: String,
    val appLabel: String,
    val gateType: GateType,
    /** Stable seed so the same block renders the same quote/prompt. */
    val seed: Long,
) {
    val quote: Quote? = if (gateType == GateType.PHILOSOPHY_QUOTE) PhilosophyQuotes.forSeed(seed) else null
}

/** The outcome the overlay reports back to the interceptor service. */
sealed interface ActionGateResult {
    /** Micro-habit completed — grant a short grace window into the app. */
    data class Overridden(val gateType: GateType) : ActionGateResult

    /** User chose the healthy exit — send them home; no grace granted. */
    data object Left : ActionGateResult
}
