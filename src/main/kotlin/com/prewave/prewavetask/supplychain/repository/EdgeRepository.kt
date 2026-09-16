package com.prewave.prewavetask.supplychain.repository

import com.prewave.prewavetask.jooq.tables.Edge.Companion.EDGE
import com.prewave.prewavetask.jooq.tables.records.EdgeRecord
import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType.VARCHAR
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class EdgeRepository(private val dsl: DSLContext) {

    fun createEdge(source: String, target: String): EdgeRecord? = dsl
        .insertInto(EDGE, EDGE.ID, EDGE.SOURCE_ID, EDGE.TARGET_ID)
        .values(UUID.randomUUID(), source, target)
        .returning(EDGE.ID, EDGE.SOURCE_ID, EDGE.TARGET_ID)
        .fetchOne()

    fun getEdgeById(id: UUID): EdgeRecord? = dsl
        .selectFrom(EDGE)
        .where(EDGE.ID.eq(id))
        .fetchOne()

    fun getEdge(source: String, target: String): EdgeRecord? = dsl
        .selectFrom(EDGE)
        .where(EDGE.SOURCE_ID.eq(source).and(EDGE.TARGET_ID.eq(target)))
        .fetchOne()

    fun deleteEdge(source: String, target: String) = dsl
        .deleteFrom(EDGE)
        .where(EDGE.SOURCE_ID.eq(source).and(EDGE.TARGET_ID.eq(target)))
        .execute()


    fun getSupplyChainEdgesBySource(sourceId: String): List<EdgeRecord> {
        //TODO: not completed yet: check to avoid cycles necessary!!
        val treeTableName = "tree"
        val targetIdFieldName = "target_id"
        val sourceIdFieldName = "source_id"
        val idFieldName = "id"
        val treeTable = name(treeTableName)

        val anchorSelect = select(EDGE.ID, EDGE.SOURCE_ID, EDGE.TARGET_ID)
            .from(EDGE)
            .where(EDGE.SOURCE_ID.eq(sourceId))

        val recursiveSelect = select(EDGE.ID, EDGE.SOURCE_ID, EDGE.TARGET_ID)
            .from(EDGE)
            .innerJoin(table(treeTable))
            .on(
                field(name(treeTableName, targetIdFieldName), VARCHAR)
                    .eq(EDGE.SOURCE_ID)
            )

        val tree = treeTable
            .fields(idFieldName, sourceIdFieldName, targetIdFieldName)
            .`as`(anchorSelect.unionAll(recursiveSelect))

        return dsl
            .withRecursive(tree)
            .selectFrom(tree)
            .fetch { it.into(EDGE)}
    }

}
