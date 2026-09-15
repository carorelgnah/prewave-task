package com.prewave.prewavetask.supplychain.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SupplyChainService {

    fun createEdge(source: UUID, target: UUID): Edge {
        /*
        TODO: implement
            - check source != target
            - unique constratint on db on source,target
            - avoid cycle??
         */
        return Edge(id = UUID.randomUUID(), source = source, target = target)
    }

    fun deleteEdge(id: UUID) {
        //TODO: implement
    }

    fun getSupplyChainTree(rootId: UUID) {
        //TODO: implement
    }

    //TODO; move to its own file as entity
    data class Edge(val id: UUID, val source: UUID, val target: UUID)
}