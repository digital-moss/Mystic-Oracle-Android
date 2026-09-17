package com.example.model

data class Rune(
    val name: String,
    val symbol: String,
    val phonetic: String,
    val meaning: String,
    val reversedMeaning: String,
    val element: String
)

data class Hexagram(
    val number: Int,
    val name: String,
    val chineseName: String,
    val trigramUpper: String,
    val trigramLower: String,
    val judgment: String,
    val image: String
)

object RuneData {
    val runes = listOf(
        Rune("Fehu", "ᚠ", "F", "Wealth, abundance, success, financial strength, new beginnings.", "Greed, loss, financial delay, poverty.", "Fire"),
        Rune("Uruz", "ᚢ", "U", "Strength, vitality, courage, health, raw power, untamed potential.", "Weakness, obsession, misdirected force, illness.", "Earth"),
        Rune("Thurisaz", "ᚦ", "Th", "Gateway, protective force, catharsis, breaking through obstacles.", "Danger, defensiveness, betrayal, dullness.", "Fire"),
        Rune("Ansuz", "ᚨ", "A", "Divine communication, wisdom, inspiration, truth, spoken word.", "Misunderstanding, vanity, manipulation, deceit.", "Air"),
        Rune("Raido", "ᚱ", "R", "Journey, travel, rhythm, destiny, right direction, personal growth.", "Stagnation, crisis, dislocation, bad timing.", "Air"),
        Rune("Kenaz", "ᚲ", "K", "Torch, illumination, creativity, technical skill, inner light, revelation.", "Disease, breakup, lack of creativity, illusion.", "Fire"),
        Rune("Gebo", "ᚷ", "G", "Partnership, gifts, generosity, balance, exchange of energy.", "Greed, loneliness, obligation, isolation.", "Air"),
        Rune("Wunjo", "ᚹ", "W", "Joy, fellowship, harmony, emotional satisfaction, success, light.", "Sorrow, discord, alienation, intoxication.", "Earth"),
        Rune("Hagalaz", "ᚺ", "H", "Hail, elemental disruption, radical change, natural forces, trial.", "Disaster, stagnation, loss, suffering.", "Water"),
        Rune("Nauthiz", "ᚾ", "N", "Need, constraint, endurance, self-reliance, overcoming hardship.", "Constraint, toil, starvation, emotional hunger.", "Fire"),
        Rune("Isa", "ᛁ", "I", "Ice, standstill, concentration, self-control, patience, introspection.", "Egoism, dullness, blindness, deceit.", "Water"),
        Rune("Jera", "ᛃ", "J", "Harvest, fruitful season, reward for labor, cycle, peace, justice.", "Major setback, bad timing, poverty, conflict.", "Earth"),
        Rune("Eihwaz", "ᛇ", "Ei", "Yew tree, resilience, reliability, endurance, defense, spiritual growth.", "Confusion, destruction, weakness, dissatisfaction.", "Earth"),
        Rune("Perthro", "ᛈ", "P", "Initiation, mystery, hidden things, fate, chance, occult knowledge.", "Addiction, stagnation, loneliness, illusion.", "Water"),
        Rune("Algiz", "ᛉ", "Z", "Protection, sanctuary, shield, divine connection, higher self guidance.", "Hidden danger, consumption, loss of divine link.", "Air"),
        Rune("Sowilo", "ᛊ", "S", "Sun, success, vitality, wholeness, healing, high energy, victory.", "False goals, bad advice, vanity, wrath, destruction.", "Fire"),
        Rune("Tiwaz", "ᛏ", "T", "Tyr, justice, leadership, honor, sacrifice, strategic victory, truth.", "Injustice, imbalance, over-analysis, waning energy.", "Air"),
        Rune("Berkano", "ᛒ", "B", "Birch, fertility, new growth, regeneration, birth, sanctuary, healing.", "Family problems, domestic trouble, deceit, sterility.", "Earth"),
        Rune("Ehwaz", "ᛖ", "E", "Horse, trust, progress, transportation, partnership, loyalty, teamwork.", "Restlessness, mistrust, disharmony, blocked progress.", "Earth"),
        Rune("Mannaz", "ᛗ", "M", "Humanity, mankind, social order, intelligence, cooperation, self-awareness.", "Depression, mortality, isolation, manipulation.", "Air"),
        Rune("Laguz", "ᛚ", "L", "Water, intuition, flow, emotions, dreams, psychic awareness, the unconscious.", "Fear, circular motion, avoidance, madness, despair.", "Water"),
        Rune("Ingwaz", "ᛜ", "Ng", "Ing, fertility, internal growth, virtues, new life, accomplishment.", "Impotence, movement without purpose, stagnation.", "Earth"),
        Rune("Othala", "ᛟ", "O", "Inheritance, homeland, ancestral heritage, estate, property, core values.", "Total loss, homelessness, bad custom, prejudice.", "Earth"),
        Rune("Dagaz", "ᛞ", "D", "Day, dawn, breakthrough, awakening, transformation, hope, clarity.", "Blindness, hopelessness, completion without renewal, night.", "Fire")
    )
}

object IChingData {
    val hexagrams = listOf(
        Hexagram(1, "The Creative", "Qián", "Heaven", "Heaven", "The Creative works sublime success, furthering through perseverance.", "Heaven moving in strength. The superior person makes themselves strong and untiring."),
        Hexagram(2, "The Receptive", "Kūn", "Earth", "Earth", "The Receptive brings about sublime success, furthering through the persistence of a mare.", "The earth's condition is receptive devotion. The superior person supports all things with broad virtue."),
        Hexagram(3, "Difficulty at the Beginning", "Zhūn", "Water", "Thunder", "Difficulty at the beginning works supreme success, furthering through perseverance.", "Clouds and thunder: Difficulty at the beginning. The superior person brings order out of confusion."),
        Hexagram(4, "Youthful Folly", "Méng", "Mountain", "Water", "Youthful folly has success. It is not I who seek the young fool, the young fool seeks me.", "A spring wells up at the foot of the mountain: Youthful folly. The superior person nourishes character."),
        Hexagram(5, "Waiting (Nourishment)", "Xū", "Water", "Heaven", "Waiting. If you are sincere, you have light and success. Perseverance brings good fortune.", "Clouds rise up to heaven: Waiting. The superior person eats and drinks, rests and is joyous."),
        Hexagram(6, "Conflict", "Sōng", "Heaven", "Water", "Conflict. You are sincere and are obstructed. A cautious halt brings good fortune.", "Heaven and water go their opposite ways: Conflict. The superior person considers well before acting."),
        Hexagram(7, "The Army", "Shī", "Earth", "Water", "The Army. The elder needs perseverance and a strong man. Good fortune without blame.", "Water in the earth: The Army. The superior person nourishes the people by gathering masses."),
        Hexagram(8, "Holding Together", "Bǐ", "Water", "Earth", "Holding together brings good fortune. Inquire of the oracle if you possess primary virtue.", "Water on the earth: Holding together. The kings of antiquity established states and gave security to families."),
        Hexagram(9, "Small Taming", "Xiǎo Xù", "Wind", "Heaven", "Small taming has success. Dense clouds, no rain from our western region.", "The wind blows across heaven: Small taming. The superior person refines outward virtue."),
        Hexagram(10, "Treading (Conduct)", "Lǚ", "Heaven", "Lake", "Treading upon the tail of the tiger. It does not bite the man. Success.", "Heaven above, the lake below: Treading. The superior person discriminates between high and low.")
    )
}
