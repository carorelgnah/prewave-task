package com.prewave.prewavetask.supplychain.api

import com.prewave.prewavetask.core.exception.ProblemDetailException
import com.prewave.prewavetask.supplychain.dto.EdgeCreatedResponse
import com.prewave.prewavetask.supplychain.dto.EdgeRequest
import com.prewave.prewavetask.supplychain.dto.SupplyChainResponse
import com.prewave.prewavetask.supplychain.service.SupplyChainService
import jakarta.validation.Valid
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/supply-chain")
class SupplyChainController(
    private val supplyChainService: SupplyChainService
) {

    @PostMapping("/edge")
    @ResponseStatus(HttpStatus.CREATED)
    fun createEdge(
        @RequestBody
        @Valid
        edgeRequest: EdgeRequest
    ): EdgeCreatedResponse =
        runCatching {
            val createEdge = supplyChainService.createEdge(
                source = edgeRequest.source,
                target = edgeRequest.target
            )
            return EdgeCreatedResponse(id = createEdge.id)
        }.getOrElse { exception ->
            when (exception) {
                is IllegalArgumentException -> throw ProblemDetailException(
                    status = HttpStatus.BAD_REQUEST,
                    title = "Invalid request",
                    detail = "Source and target cannot be the same",
                    cause = exception
                )

                is DuplicateKeyException -> throw ProblemDetailException(
                    status = HttpStatus.CONFLICT,
                    title = "Edge already exists",
                    detail = "Edge with source ${edgeRequest.source} and target ${edgeRequest.target} already exists",
                    cause = exception
                )

                else -> throw ProblemDetailException(
                    status = HttpStatus.INTERNAL_SERVER_ERROR,
                    title = "Unknown error",
                    detail = "Edge creation failed",
                    cause = exception
                )
            }
        }

    @DeleteMapping("/edge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @RequestBody
        @Valid
        edgeRequest: EdgeRequest
    ) = runCatching {
        supplyChainService.deleteEdge(source = edgeRequest.source, target = edgeRequest.target)
    }.getOrElse { exception ->
        when (exception) {
            is NoSuchElementException -> throw ProblemDetailException(
                status = HttpStatus.CONFLICT,
                title = "Edge does not exist",
                detail = "Edge with ${edgeRequest.source} and ${edgeRequest.target} not found and can't be deleted",
                cause = exception
            )

            else -> throw ProblemDetailException(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                title = "Unknown error",
                detail = "Edge deletion failed",
                cause = exception
            )
        }
    }

    @GetMapping("/{sourceId}")
    fun getSupplyChainTree(@PathVariable sourceId: String): SupplyChainResponse =
        runCatching {
            supplyChainService.getSupplyChainTree(rootSourceId = sourceId)
                .toResponse()
        }.getOrElse { exception ->
            when (exception) {
                is NoSuchElementException -> throw ProblemDetailException(
                    status = HttpStatus.NOT_FOUND,
                    title = "Supply chain tree not found",
                    detail = "Supply chain tree for source $sourceId not found",
                    cause = exception
                )

                else -> throw ProblemDetailException(
                    status = HttpStatus.INTERNAL_SERVER_ERROR,
                    title = "Unknown error",
                    detail = "Supply chain tree fetching failed",
                    cause = exception
                )
            }
        }

    private fun Map<String, List<String>>.toResponse(): SupplyChainResponse {
        val root = determineRootKey()

        return SupplyChainResponse(sourceId = root, children = this[root]?.toResponse(map = this))
    }

    private fun List<String>.toResponse(map: Map<String, List<String>>): List<SupplyChainResponse> =
        map { targetId -> SupplyChainResponse(sourceId = targetId, children = map[targetId]?.toResponse(map)) }

    private fun Map<String, List<String>>.determineRootKey(): String =
        keys.firstOrNull { key -> !this.values.flatten().contains(key) }
            ?: throw IllegalStateException("Supply chain tree is not valid")




}