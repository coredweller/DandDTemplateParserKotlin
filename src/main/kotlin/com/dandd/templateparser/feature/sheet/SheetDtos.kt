package com.dandd.templateparser.feature.sheet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SheetSummaryResponse(
    val id: String,
    val sheetType: String,
    val characterName: String,
    val level: Int,
    val createdAt: String,
)

fun SheetSummary.toResponse() =
    SheetSummaryResponse(
        id = id.value.toString(),
        sheetType = sheetType,
        characterName = characterName,
        level = level,
        createdAt = createdAt.toString(),
    )

@Serializable
data class AbilityScore(
    @SerialName("Score") val score: Int,
    @SerialName("Modifier") val modifier: String,
)

@Serializable
data class AbilityScores(
    @SerialName("Strength") val strength: AbilityScore,
    @SerialName("Dexterity") val dexterity: AbilityScore,
    @SerialName("Constitution") val constitution: AbilityScore,
    @SerialName("Intelligence") val intelligence: AbilityScore,
    @SerialName("Wisdom") val wisdom: AbilityScore,
    @SerialName("Charisma") val charisma: AbilityScore,
)

@Serializable
data class SheetEquipment(
    @SerialName("Armor") val armor: String = "",
    @SerialName("Weapons") val weapons: String = "",
    @SerialName("Other") val other: String = "",
)

@Serializable
data class GeneralSheetRequest(
    @SerialName("CharacterName") val characterName: String,
    @SerialName("Level") val level: Int,
    @SerialName("Race") val race: String,
    @SerialName("Class") val creatureClass: String,
    @SerialName("Alignment") val alignment: String,
    @SerialName("HP") val hp: String,
    @SerialName("AC") val ac: Int,
    @SerialName("Speed") val speed: String,
    @SerialName("AbilityScores") val abilityScores: AbilityScores,
    @SerialName("SavingThrows") val savingThrows: Map<String, String> = emptyMap(),
    @SerialName("Skills") val skills: Map<String, String> = emptyMap(),
    @SerialName("Senses") val senses: String = "",
    @SerialName("Languages") val languages: String = "",
    @SerialName("SpecialTraits") val specialTraits: Map<String, String> = emptyMap(),
    @SerialName("Actions") val actions: Map<String, String> = emptyMap(),
    @SerialName("Equipment") val equipment: SheetEquipment = SheetEquipment(),
    @SerialName("Notes") val notes: String = "",
)

@Serializable
data class LegendaryActionOptions(
    @SerialName("Option1") val option1: String = "",
    @SerialName("Option2") val option2: String = "",
    @SerialName("Option3") val option3: String = "",
)

@Serializable
data class LegendaryActionsBlock(
    @SerialName("Legendary Action Uses") val uses: String = "3",
    @SerialName("Options") val options: LegendaryActionOptions = LegendaryActionOptions(),
)

@Serializable
data class MythicTrait(
    @SerialName("Name") val name: String = "",
    @SerialName("Description") val description: String = "",
)

@Serializable
data class LegendarySheetRequest(
    @SerialName("CharacterName") val characterName: String,
    @SerialName("Level") val level: Int,
    @SerialName("Race") val race: String,
    @SerialName("Class") val creatureClass: String,
    @SerialName("Alignment") val alignment: String,
    @SerialName("HP") val hp: String,
    @SerialName("AC") val ac: Int,
    @SerialName("Speed") val speed: String,
    @SerialName("AbilityScores") val abilityScores: AbilityScores,
    @SerialName("SavingThrows") val savingThrows: Map<String, String> = emptyMap(),
    @SerialName("Skills") val skills: Map<String, String> = emptyMap(),
    @SerialName("DamageResistances") val damageResistances: String = "",
    @SerialName("DamageImmunities") val damageImmunities: String = "",
    @SerialName("ConditionImmunities") val conditionImmunities: String = "",
    @SerialName("Senses") val senses: String = "",
    @SerialName("Languages") val languages: String = "",
    @SerialName("ChallengeRating") val challengeRating: String = "",
    @SerialName("ProficiencyBonus") val proficiencyBonus: String = "",
    @SerialName("SpecialTraits") val specialTraits: Map<String, String> = emptyMap(),
    @SerialName("Actions") val actions: Map<String, String> = emptyMap(),
    @SerialName("BonusActions") val bonusActions: Map<String, String> = emptyMap(),
    @SerialName("Reactions") val reactions: Map<String, String> = emptyMap(),
    @SerialName("LegendaryTraits") val legendaryTraits: Map<String, String> = emptyMap(),
    @SerialName("LegendaryActions") val legendaryActions: LegendaryActionsBlock = LegendaryActionsBlock(),
    @SerialName("MythicTrait") val mythicTrait: MythicTrait = MythicTrait(),
    @SerialName("LairActions") val lairActions: Map<String, String> = emptyMap(),
    @SerialName("RegionalEffects") val regionalEffects: List<String> = emptyList(),
    @SerialName("Equipment") val equipment: SheetEquipment = SheetEquipment(),
    @SerialName("Notes") val notes: String = "",
)
