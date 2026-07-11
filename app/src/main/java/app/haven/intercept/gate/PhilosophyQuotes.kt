package app.haven.intercept.gate

/** A single curated reflection shown for the PHILOSOPHY_QUOTE gate. */
data class Quote(val text: String, val author: String)

/**
 * A small, hand-curated set of Stoic / contemplative lines. Deliberately not a
 * network feed — the interceptor stays fully offline, and the curation keeps
 * the tone calm rather than motivational-poster loud.
 */
object PhilosophyQuotes {

    private val quotes: List<Quote> = listOf(
        Quote("You have power over your mind — not outside events. Realize this, and you will find strength.", "Marcus Aurelius"),
        Quote("We suffer more often in imagination than in reality.", "Seneca"),
        Quote("It is not that we have a short time to live, but that we waste a lot of it.", "Seneca"),
        Quote("No man is free who is not master of himself.", "Epictetus"),
        Quote("The impediment to action advances action. What stands in the way becomes the way.", "Marcus Aurelius"),
        Quote("Wealth consists not in having great possessions, but in having few wants.", "Epictetus"),
        Quote("Waste no more time arguing about what a good person should be. Be one.", "Marcus Aurelius"),
        Quote("He who is not contented with what he has, would not be contented with what he would like to have.", "Socrates"),
        Quote("Nature does not hurry, yet everything is accomplished.", "Lao Tzu"),
        Quote("The nearer a man comes to a calm mind, the closer he is to strength.", "Marcus Aurelius"),
    )

    /** Deterministic pick so re-entering the same block shows a stable quote. */
    fun forSeed(seed: Long): Quote = quotes[(seed % quotes.size).toInt().let { if (it < 0) it + quotes.size else it }]

    fun random(): Quote = quotes.random()
}
