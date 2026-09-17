package com.example.model

data class TarotCard(
    val name: String,
    val arcana: String, // "Major" or "Minor"
    val suit: String?, // "Wands", "Cups", "Swords", "Pentacles", or null for Major
    val uprightMeaning: String,
    val reversedMeaning: String,
    val description: String
)

object TarotData {
    val cards = listOf(
        TarotCard("0. The Fool", "Major", null, "New beginnings, innocence, spontaneity, a free spirit.", "Naivety, foolishness, recklessness, risk-taking.", "A young traveler stepping off a cliff with complete faith in the universe."),
        TarotCard("I. The Magician", "Major", null, "Manifestation, resourcefulness, power, inspired action.", "Manipulation, poor planning, untapped talents.", "A master of elements standing before an altar with tools of creation."),
        TarotCard("II. The High Priestess", "Major", null, "Intuition, sacred knowledge, divine feminine, subconscious.", "Secrets, disconnected intuition, superficiality.", "A veiled figure seated between pillars of light and darkness."),
        TarotCard("III. The Empress", "Major", null, "Femininity, beauty, nature, nurturing, abundance.", "Creative block, dependence on others, emptiness.", "A majestic queen surrounded by golden wheat fields and cascading waterfalls."),
        TarotCard("IV. The Emperor", "Major", null, "Authority, structure, control, father figure, establishment.", "Tyranny, rigidity, coldness, lack of discipline.", "A stern ruler seated on a stone throne adorned with ram heads."),
        TarotCard("V. The Hierophant", "Major", null, "Spiritual wisdom, religious beliefs, conformity, tradition, institutions.", "Personal beliefs, rebellion, unorthodox approaches.", "A spiritual teacher bestowing blessings in a sacred temple."),
        TarotCard("VI. The Lovers", "Major", null, "Love, harmony, vital relationships, values alignment, choices.", "Disharmony, imbalance, misaligned values, bad choices.", "Two souls standing under the blessing of an angelic figure in Eden."),
        TarotCard("VII. The Chariot", "Major", null, "Control, willpower, success, action, determination, overcoming obstacles.", "Lack of control, opposition, directionless wandering.", "A victorious warrior steering a chariot pulled by sphinxes of will."),
        TarotCard("VIII. Strength", "Major", null, "Courage, compassion, inner power, patience, gentle control.", "Self-doubt, raw emotion, weakness, lack of confidence.", "A calm figure gently closing the jaws of a majestic lion."),
        TarotCard("IX. The Hermit", "Major", null, "Soul-searching, introspection, solitude, inner guidance, wisdom.", "Isolation, loneliness, withdrawal, lost direction.", "An old sage holding a glowing lantern high on a snowy mountain peak."),
        TarotCard("X. Wheel of Fortune", "Major", null, "Good luck, karma, life cycles, destiny, turning point, inevitability.", "Bad luck, negative cycles, resistance to change.", "A cosmic wheel turning through epochs with figures of fate ascending and descending."),
        TarotCard("XI. Justice", "Major", null, "Fairness, truth, cause and effect, law, clarity, accountability.", "Dishonesty, unfairness, lack of accountability, bias.", "An impartial judge holding the scales of truth and a double-edged sword."),
        TarotCard("XII. The Hanged Man", "Major", null, "Surrender, pause, new perspective, letting go, enlightenment.", "Stalling, needless sacrifice, fear of sacrifice.", "A serene figure suspended upside down from a living wooden cross."),
        TarotCard("XIII. Death", "Major", null, "Transformation, endings, transition, rebirth, shedding the old.", "Resistance to change, stagnation, fear of inevitable endings.", "An armored skeleton riding a white horse across a threshold of sun and dawn."),
        TarotCard("XIV. Temperance", "Major", null, "Balance, moderation, patience, purpose, divine timing, harmony.", "Imbalance, excess, clashing energies, lack of patience.", "An angelic being pouring fluid between two golden chalices with perfect balance."),
        TarotCard("XV. The Devil", "Major", null, "Shadow self, attachment, addiction, restriction, materialism.", "Freedom, reclaiming power, overcoming addiction, release.", "Bound figures standing before a horned deity of worldly illusion."),
        TarotCard("XVI. The Tower", "Major", null, "Sudden change, upheaval, revelation, awakening, breaking illusions.", "Disaster avoided, delayed trauma, fear of suffering.", "Lightning striking a stone tower as flames burst from the windows."),
        TarotCard("XVII. The Star", "Major", null, "Hope, faith, purpose, renewal, inspiration, spiritual blessing.", "Despair, lack of faith, discouragement, pessimism.", "A celestial maiden pouring healing waters into a serene starlit pool."),
        TarotCard("XVIII. The Moon", "Major", null, "Illusion, fear, anxiety, subconscious, intuition, dreams, uncertainty.", "Release of fear, repressed emotion surfacing, clarity dawning.", "A mystical night landscape illuminated by a crescent moon and howling wolves."),
        TarotCard("XIX. The Sun", "Major", null, "Joy, success, celebration, vitality, positivity, warmth, clarity.", "Temporary sadness, overly optimistic view, clouded joy.", "A radiant golden sun shining down upon a joyful child riding a white horse."),
        TarotCard("XX. Judgement", "Major", null, "Rebirth, inner calling, absolution, awakening, reckoning, forgiveness.", "Self-doubt, harsh self-judgment, ignoring the call.", "An angelic trumpet sounding as figures rise joyfully from earthly graves."),
        TarotCard("XXI. The World", "Major", null, "Completion, accomplishment, travel, wholeness, fulfillment, unity.", "Lack of closure, unfinished business, delays in success.", "A dancing figure framed by a laurel wreath surrounded by the four cosmic guardians.")
    )
}
