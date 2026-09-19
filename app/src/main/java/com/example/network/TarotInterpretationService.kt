package com.example.network

object TarotInterpretationService {
    data class TarotInsight(
        val cardName: String,
        val meaning: String,
        val keyword: String,
        val astrologicalCorrespondence: String,
        val element: String,
        val numerology: String
    )

    private val insights = mapOf(
        "0. The Fool" to TarotInsight(
            cardName = "0. The Fool",
            meaning = "New beginnings, infinite potential, innocence, spontaneity, and leaping into the unknown with absolute faith.",
            keyword = "New Beginnings",
            astrologicalCorrespondence = "Uranus",
            element = "Air",
            numerology = "0 (The Void & Potential)"
        ),
        "I. The Magician" to TarotInsight(
            cardName = "I. The Magician",
            meaning = "Manifestation, resourcefulness, power, inspired action, and aligning conscious will with divine creation.",
            keyword = "Manifestation",
            astrologicalCorrespondence = "Mercury",
            element = "Air",
            numerology = "1 (Initiation)"
        ),
        "II. The High Priestess" to TarotInsight(
            cardName = "II. The High Priestess",
            meaning = "Intuition, sacred knowledge, the subconscious mind, divine feminine wisdom, and stillness before revelation.",
            keyword = "Intuition",
            astrologicalCorrespondence = "Moon",
            element = "Water",
            numerology = "2 (Dualism & Mystery)"
        ),
        "III. The Empress" to TarotInsight(
            cardName = "III. The Empress",
            meaning = "Abundance, fertility, nurturing growth, sensory pleasure, nature, and creative manifestation in the physical realm.",
            keyword = "Abundance",
            astrologicalCorrespondence = "Venus",
            element = "Earth",
            numerology = "3 (Creation)"
        ),
        "IV. The Emperor" to TarotInsight(
            cardName = "IV. The Emperor",
            meaning = "Authority, structure, establishing order, leadership, stability, and strategic foundational planning.",
            keyword = "Structure",
            astrologicalCorrespondence = "Aries",
            element = "Fire",
            numerology = "4 (Foundation)"
        ),
        "V. The Hierophant" to TarotInsight(
            cardName = "V. The Hierophant",
            meaning = "Spiritual wisdom, traditional guidance, institutional structures, mentorship, and sacred teachings.",
            keyword = "Tradition",
            astrologicalCorrespondence = "Taurus",
            element = "Earth",
            numerology = "5 (Spirit in Matter)"
        ),
        "VI. The Lovers" to TarotInsight(
            cardName = "VI. The Lovers",
            meaning = "Divine union, harmonious relationships, deeply aligned values, conscious choices, and heart-centered integrity.",
            keyword = "Union",
            astrologicalCorrespondence = "Gemini",
            element = "Air",
            numerology = "6 (Harmony)"
        ),
        "VII. The Chariot" to TarotInsight(
            cardName = "VII. The Chariot",
            meaning = "Willpower, triumphant determination, overcoming obstacles through focus, momentum, and self-mastery.",
            keyword = "Triumph",
            astrologicalCorrespondence = "Cancer",
            element = "Water",
            numerology = "7 (Spiritual Quest)"
        )
    )

    fun getInsight(cardName: String): TarotInsight {
        return insights[cardName] ?: TarotInsight(
            cardName = cardName,
            meaning = "A profound revelation regarding $cardName, highlighting inner truth, balanced energies, and unfolding destiny.",
            keyword = "Guidance",
            astrologicalCorrespondence = "Cosmic Alignment",
            element = "Spirit",
            numerology = "Universal"
        )
    }

    fun getSpreadInterpretation(spreadName: String, cards: List<String>): String {
        val sb = StringBuilder()
        sb.append("=== TAROT SYNTHESIS REPORT ===\n\n")
        sb.append("Spread Type: $spreadName\n")
        sb.append("Drawn Cards Count: ${cards.size}\n\n")
        cards.forEachIndexed { index, card ->
            val insight = getInsight(card)
            sb.append("Position ${index + 1}: $card\n")
            sb.append(" • Keyword: ${insight.keyword}\n")
            sb.append(" • Meaning: ${insight.meaning}\n")
            sb.append(" • Astrology / Element: ${insight.astrologicalCorrespondence} (${insight.element})\n\n")
        }
        sb.append("Overall Synthesis: The energies represented in this $spreadName spread converge toward profound personal clarity, encouraging balanced action and mindful reflection.")
        return sb.toString()
    }
}
