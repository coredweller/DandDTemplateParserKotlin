package com.dandd.templateparser.feature.sheet

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.dandd.templateparser.common.DomainError
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset

class SheetService(private val repo: SheetRepository) {
    private val logger = LoggerFactory.getLogger(SheetService::class.java)

    suspend fun renderGeneral(request: GeneralSheetRequest): Either<DomainError, String> {
        if (request.characterName.isBlank()) {
            return DomainError.ValidationFailed(listOf("CharacterName must not be blank")).left()
        }
        val html = buildGeneralHtml(request)
        val sheet = CharacterSheetRender(
            id = SheetId.new(),
            sheetType = "general",
            characterName = request.characterName,
            level = request.level,
            responseHtml = html,
            createdAt = LocalDateTime.now(ZoneOffset.UTC),
        )
        repo.save(sheet)
        logger.info("Saved general sheet id={} name={}", sheet.id.value, sheet.characterName)
        return html.right()
    }

    suspend fun renderLegendary(request: LegendarySheetRequest): Either<DomainError, String> {
        if (request.characterName.isBlank()) {
            return DomainError.ValidationFailed(listOf("CharacterName must not be blank")).left()
        }
        val html = buildLegendaryHtml(request)
        val sheet = CharacterSheetRender(
            id = SheetId.new(),
            sheetType = "legendary",
            characterName = request.characterName,
            level = request.level,
            responseHtml = html,
            createdAt = LocalDateTime.now(ZoneOffset.UTC),
        )
        repo.save(sheet)
        logger.info("Saved legendary sheet id={} name={}", sheet.id.value, sheet.characterName)
        return html.right()
    }

    private fun String.esc() =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    private fun statBlockCss() =
        """
        <style>
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body { background: #e8d9bb; padding: 24px; font-family: 'Palatino Linotype', Palatino, Georgia, serif; }
          .stat-block {
            background: #fdf1dc;
            border: 2px solid #7a200d;
            outline: 5px solid #d5a97d;
            outline-offset: 3px;
            max-width: 640px;
            margin: 0 auto;
            padding: 14px 18px;
            box-shadow: 5px 7px 16px rgba(0,0,0,0.35);
          }
          .creature-name { font-size: 26px; font-weight: 900; font-variant: small-caps; letter-spacing: .04em; }
          .creature-subtitle { font-style: italic; font-size: 13px; color: #555; margin-top: 3px; }
          hr.thick { border: none; border-top: 2.5px solid #9c2f15; margin: 9px 0 5px; }
          hr.thin  { border: none; border-top: 1px   solid #9c2f15; margin: 5px 0; }
          .prop { font-size: 14px; margin: 4px 0; line-height: 1.4; }
          .prop b { color: #1a1a1a; }
          .abilities { display: flex; text-align: center; margin: 7px 0; }
          .ab { flex: 1; border-right: 1px solid #c8a97a; padding: 3px 0; }
          .ab:last-child { border-right: none; }
          .ab-name  { font-size: 9px; font-weight: bold; text-transform: uppercase; letter-spacing: .1em; color: #7a200d; }
          .ab-score { font-size: 15px; font-weight: bold; margin-top: 2px; }
          .ab-mod   { font-size: 12px; color: #666; }
          .section { font-size: 17px; font-weight: bold; font-variant: small-caps; color: #7a200d; border-bottom: 1px solid #9c2f15; margin: 11px 0 5px; padding-bottom: 1px; letter-spacing: .03em; }
          .trait { margin: 5px 0; font-size: 14px; line-height: 1.5; }
          .tname { font-weight: bold; font-style: italic; }
          .notes-block { font-size: 13px; font-style: italic; color: #4a4a4a; margin-top: 5px; line-height: 1.5; }
          .legendary-uses { font-size: 13px; font-style: italic; color: #555; margin-bottom: 4px; }
          ul.regional { margin: 4px 0 0 18px; font-size: 14px; line-height: 1.5; }
        </style>
        """.trimIndent()

    private fun abilityScoresHtml(a: AbilityScores) =
        """
        <div class="abilities">
          <div class="ab"><div class="ab-name">STR</div><div class="ab-score">${a.strength.score}</div><div class="ab-mod">(${a.strength.modifier.esc()})</div></div>
          <div class="ab"><div class="ab-name">DEX</div><div class="ab-score">${a.dexterity.score}</div><div class="ab-mod">(${a.dexterity.modifier.esc()})</div></div>
          <div class="ab"><div class="ab-name">CON</div><div class="ab-score">${a.constitution.score}</div><div class="ab-mod">(${a.constitution.modifier.esc()})</div></div>
          <div class="ab"><div class="ab-name">INT</div><div class="ab-score">${a.intelligence.score}</div><div class="ab-mod">(${a.intelligence.modifier.esc()})</div></div>
          <div class="ab"><div class="ab-name">WIS</div><div class="ab-score">${a.wisdom.score}</div><div class="ab-mod">(${a.wisdom.modifier.esc()})</div></div>
          <div class="ab"><div class="ab-name">CHA</div><div class="ab-score">${a.charisma.score}</div><div class="ab-mod">(${a.charisma.modifier.esc()})</div></div>
        </div>
        """.trimIndent()

    private fun propLine(label: String, value: String) = "<p class=\"prop\"><b>${label.esc()}</b> ${value.esc()}</p>"

    private fun sectionHtml(title: String, items: Map<String, String>): String {
        if (items.isEmpty()) return ""
        val traits = items.entries.joinToString("\n") { (name, desc) ->
            "<div class=\"trait\"><span class=\"tname\">${name.esc()}.</span> ${desc.esc()}</div>"
        }
        return "<div class=\"section\">${title.esc()}</div>\n$traits"
    }

    private fun equipmentHtml(eq: SheetEquipment): String {
        val lines = buildList {
            if (eq.armor.isNotBlank()) add(propLine("Armor", eq.armor))
            if (eq.weapons.isNotBlank()) add(propLine("Weapons", eq.weapons))
            if (eq.other.isNotBlank()) add(propLine("Other", eq.other))
        }
        if (lines.isEmpty()) return ""
        return "<div class=\"section\">Equipment</div>\n${lines.joinToString("\n")}"
    }

    private fun buildGeneralHtml(r: GeneralSheetRequest): String {
        val levelStr = if (r.level > 0) "Level ${r.level}" else ""
        val subtitle = listOf(levelStr, r.race.esc(), r.creatureClass.esc(), r.alignment.esc())
            .filter { it.isNotBlank() }
            .joinToString(" · ")

        val savingThrowsLine = if (r.savingThrows.isNotEmpty())
            propLine("Saving Throws", r.savingThrows.entries.joinToString(", ") { "${it.key} ${it.value}" }) else ""
        val skillsLine = if (r.skills.isNotEmpty())
            propLine("Skills", r.skills.entries.joinToString(", ") { "${it.key} ${it.value}" }) else ""
        val sensesLine = if (r.senses.isNotBlank()) propLine("Senses", r.senses) else ""
        val languagesLine = if (r.languages.isNotBlank()) propLine("Languages", r.languages) else ""

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <title>${r.characterName.esc()}</title>
              ${statBlockCss()}
            </head>
            <body>
            <div class="stat-block">
              <div class="creature-name">${r.characterName.esc()}</div>
              <div class="creature-subtitle">$subtitle</div>
              <hr class="thick">
              ${propLine("Armor Class", r.ac.toString())}
              ${propLine("Hit Points", r.hp)}
              ${propLine("Speed", r.speed)}
              <hr class="thick">
              ${abilityScoresHtml(r.abilityScores)}
              <hr class="thick">
              $savingThrowsLine
              $skillsLine
              $sensesLine
              $languagesLine
              <hr class="thin">
              ${sectionHtml("Special Traits", r.specialTraits)}
              ${sectionHtml("Actions", r.actions)}
              ${equipmentHtml(r.equipment)}
              ${if (r.notes.isNotBlank()) "<div class=\"section\">Notes</div><p class=\"notes-block\">${r.notes.esc()}</p>" else ""}
            </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildLegendaryHtml(r: LegendarySheetRequest): String {
        val levelStr = if (r.level > 0) "Level ${r.level}" else ""
        val subtitle = listOf(levelStr, r.race.esc(), r.creatureClass.esc(), r.alignment.esc())
            .filter { it.isNotBlank() }
            .joinToString(" · ")

        val savingThrowsLine = if (r.savingThrows.isNotEmpty())
            propLine("Saving Throws", r.savingThrows.entries.joinToString(", ") { "${it.key} ${it.value}" }) else ""
        val skillsLine = if (r.skills.isNotEmpty())
            propLine("Skills", r.skills.entries.joinToString(", ") { "${it.key} ${it.value}" }) else ""
        val resistancesLine = if (r.damageResistances.isNotBlank()) propLine("Damage Resistances", r.damageResistances) else ""
        val immunitiesLine = if (r.damageImmunities.isNotBlank()) propLine("Damage Immunities", r.damageImmunities) else ""
        val conditionLine = if (r.conditionImmunities.isNotBlank()) propLine("Condition Immunities", r.conditionImmunities) else ""
        val sensesLine = if (r.senses.isNotBlank()) propLine("Senses", r.senses) else ""
        val languagesLine = if (r.languages.isNotBlank()) propLine("Languages", r.languages) else ""
        val crLine = if (r.challengeRating.isNotBlank()) propLine("Challenge", "${r.challengeRating} (PB ${r.proficiencyBonus})") else ""

        val mythicHtml = if (r.mythicTrait.name.isNotBlank())
            "<div class=\"section\">Mythic Trait</div>\n<div class=\"trait\"><span class=\"tname\">${r.mythicTrait.name.esc()}.</span> ${r.mythicTrait.description.esc()}</div>"
            else ""

        val legendaryActionsHtml = buildString {
            if (r.legendaryActions.uses.isNotBlank() || r.legendaryActions.options.option1.isNotBlank()) {
                append("<div class=\"section\">Legendary Actions</div>\n")
                append("<p class=\"legendary-uses\">Can take ${r.legendaryActions.uses.esc()} legendary actions per round.</p>\n")
                listOf(r.legendaryActions.options.option1, r.legendaryActions.options.option2, r.legendaryActions.options.option3)
                    .filter { it.isNotBlank() }
                    .forEach { append("<div class=\"trait\">${it.esc()}</div>\n") }
            }
        }

        val regionalEffectsHtml = if (r.regionalEffects.isNotEmpty()) buildString {
            append("<div class=\"section\">Regional Effects</div>\n<ul class=\"regional\">\n")
            r.regionalEffects.forEach { append("<li>${it.esc()}</li>\n") }
            append("</ul>")
        } else ""

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <title>${r.characterName.esc()}</title>
              ${statBlockCss()}
            </head>
            <body>
            <div class="stat-block">
              <div class="creature-name">${r.characterName.esc()}</div>
              <div class="creature-subtitle">$subtitle</div>
              <hr class="thick">
              ${propLine("Armor Class", r.ac.toString())}
              ${propLine("Hit Points", r.hp)}
              ${propLine("Speed", r.speed)}
              <hr class="thick">
              ${abilityScoresHtml(r.abilityScores)}
              <hr class="thick">
              $savingThrowsLine
              $skillsLine
              $resistancesLine
              $immunitiesLine
              $conditionLine
              $sensesLine
              $languagesLine
              $crLine
              <hr class="thin">
              ${sectionHtml("Special Traits", r.specialTraits)}
              ${sectionHtml("Actions", r.actions)}
              ${sectionHtml("Bonus Actions", r.bonusActions)}
              ${sectionHtml("Reactions", r.reactions)}
              ${sectionHtml("Legendary Traits", r.legendaryTraits)}
              $legendaryActionsHtml
              $mythicHtml
              ${sectionHtml("Lair Actions", r.lairActions)}
              $regionalEffectsHtml
              ${equipmentHtml(r.equipment)}
              ${if (r.notes.isNotBlank()) "<div class=\"section\">Notes</div><p class=\"notes-block\">${r.notes.esc()}</p>" else ""}
            </div>
            </body>
            </html>
        """.trimIndent()
    }
}
