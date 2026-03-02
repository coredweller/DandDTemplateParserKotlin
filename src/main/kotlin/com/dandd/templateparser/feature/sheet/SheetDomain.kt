package com.dandd.templateparser.feature.sheet

import java.time.LocalDateTime
import java.util.UUID

@JvmInline
value class SheetId(val value: UUID) {
    companion object {
        fun new() = SheetId(UUID.randomUUID())

        fun from(value: String) = SheetId(UUID.fromString(value))
    }
}

data class CharacterSheetRender(
    val id: SheetId,
    val sheetType: String,
    val characterName: String,
    val level: Int,
    val responseHtml: String,
    val createdAt: LocalDateTime,
)

data class SheetSummary(
    val id: SheetId,
    val sheetType: String,
    val characterName: String,
    val level: Int,
    val createdAt: LocalDateTime,
)
