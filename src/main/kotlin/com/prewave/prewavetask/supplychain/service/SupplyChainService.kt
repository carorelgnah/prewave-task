package com.prewave.prewavetask.supplychain.service

import com.prewave.prewavetask.jooq.tables.records.EdgeRecord
import com.prewave.prewavetask.supplychain.repository.EdgeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class SupplyChainService(private val edgeRepository: EdgeRepository) {

    fun createEdge(source: String, target: String): EdgeRecord {
        require(source != target) { "Source and target cannot be the same" }

        return edgeRepository.createEdge(source = source, target = target)
            ?: throw RuntimeException("Edge creation failed")
    }

    fun deleteEdge(source: String, target: String) {
        edgeRepository.getEdge(source = source, target = target)
            ?: throw NoSuchElementException("Edge with $source and $target not found and can't be deleted")

        edgeRepository.deleteEdge(source = source, target = target)
    }

    @Transactional(readOnly = true)
    fun getSupplyChainTree(rootId: UUID) {
        //TODO: implement
    }
}