package com.prewave.prewavetask.supplychain.repository

import com.prewave.prewavetask.jooq.tables.Edge.Companion.EDGE
import com.prewave.prewavetask.jooq.tables.records.EdgeRecord
import org.jooq.DSLContext
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

}
