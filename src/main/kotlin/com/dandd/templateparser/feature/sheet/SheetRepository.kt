package com.dandd.templateparser.feature.sheet

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface SheetRepository {
    suspend fun save(sheet: CharacterSheetRender): CharacterSheetRender

    suspend fun findById(id: SheetId): CharacterSheetRender?

    suspend fun findSummaries(type: String?, level: Int?): List<SheetSummary>
}

object CharacterSheetRendersTable : Table("character_sheet_renders") {
    val id = varchar("id", 36)
    val sheetType = varchar("sheet_type", 20)
    val characterName = varchar("character_name", 255)
    val level = integer("level")
    val responseHtml = text("response_html")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

class SheetRepositoryImpl : SheetRepository {
    override suspend fun save(sheet: CharacterSheetRender): CharacterSheetRender {
        newSuspendedTransaction {
            CharacterSheetRendersTable.insert {
                it[id] = sheet.id.value.toString()
                it[sheetType] = sheet.sheetType
                it[characterName] = sheet.characterName
                it[level] = sheet.level
                it[responseHtml] = sheet.responseHtml
                it[createdAt] = sheet.createdAt
            }
        }
        return sheet
    }

    override suspend fun findById(id: SheetId): CharacterSheetRender? =
        newSuspendedTransaction {
            CharacterSheetRendersTable
                .selectAll()
                .where { CharacterSheetRendersTable.id eq id.value.toString() }
                .singleOrNull()
                ?.toSheet()
        }

    override suspend fun findSummaries(type: String?, level: Int?): List<SheetSummary> =
        newSuspendedTransaction {
            CharacterSheetRendersTable
                .select(
                    CharacterSheetRendersTable.id,
                    CharacterSheetRendersTable.sheetType,
                    CharacterSheetRendersTable.characterName,
                    CharacterSheetRendersTable.level,
                    CharacterSheetRendersTable.createdAt,
                )
                .apply {
                    if (type != null) andWhere { CharacterSheetRendersTable.sheetType eq type }
                    if (level != null) andWhere { CharacterSheetRendersTable.level eq level }
                }
                .map { it.toSummary() }
        }

    private fun ResultRow.toSummary() =
        SheetSummary(
            id = SheetId.from(this[CharacterSheetRendersTable.id]),
            sheetType = this[CharacterSheetRendersTable.sheetType],
            characterName = this[CharacterSheetRendersTable.characterName],
            level = this[CharacterSheetRendersTable.level],
            createdAt = this[CharacterSheetRendersTable.createdAt],
        )

    private fun ResultRow.toSheet() =
        CharacterSheetRender(
            id = SheetId.from(this[CharacterSheetRendersTable.id]),
            sheetType = this[CharacterSheetRendersTable.sheetType],
            characterName = this[CharacterSheetRendersTable.characterName],
            level = this[CharacterSheetRendersTable.level],
            responseHtml = this[CharacterSheetRendersTable.responseHtml],
            createdAt = this[CharacterSheetRendersTable.createdAt],
        )
}
