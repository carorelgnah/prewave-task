package com.prewave.prewavetask.supplychain.api

import com.prewave.prewavetask.core.exception.ProblemDetailException
import com.prewave.prewavetask.supplychain.service.SupplyChainService
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
    fun createEdge(@RequestBody edgeCreateRequest: EdgeCreateRequest): EdgeCreatedResponse =
        runCatching {
            val createEdge = supplyChainService.createEdge(
                source = edgeCreateRequest.source,
                target = edgeCreateRequest.target
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
                    detail = "Edge with source ${edgeCreateRequest.source} and target ${edgeCreateRequest.target} already exists",
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

    @DeleteMapping("/edge/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) =
        supplyChainService.deleteEdge(id = id)

    @GetMapping("/{id}")
    fun getSupplyChainTree(@PathVariable id: UUID): SupplyChainResponse {
        supplyChainService.getSupplyChainTree(rootId = id)
        return SupplyChainResponse(id = id)
    }

    data class EdgeCreateRequest(val source: String, val target: String)
    data class EdgeCreatedResponse(val id: UUID)
    data class SupplyChainResponse(val id: UUID, val children: List<SupplyChainResponse> = emptyList())


}