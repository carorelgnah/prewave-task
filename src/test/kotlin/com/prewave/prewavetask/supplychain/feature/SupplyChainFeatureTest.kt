package com.prewave.prewavetask.supplychain.feature

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import com.prewave.prewavetask.jooq.tables.records.EdgeRecord
import com.prewave.prewavetask.supplychain.api.SupplyChainController.*
import com.prewave.prewavetask.supplychain.repository.EdgeRepository
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SupplyChainFeatureTest(
    private val mockMvc: MockMvc,
    private val flyway: Flyway,
    private val objectMapper: ObjectMapper,
    private val edgeRepository: EdgeRepository,
) {
    @BeforeEach
    fun clearDatabase() {
        flyway.clean()
        flyway.migrate()
    }

    @Nested
    inner class CreateEdge {
        @Test
        fun `should create new edge`() {
            val response: EdgeCreatedResponse =
                mockMvc.createEdge(EdgeRequest(source = SOURCE_ID, target = TARGET_ID))
                    .andExpect { status { isCreated() } }
                    .andReturn().response.contentAsString.let { objectMapper.readValue<EdgeCreatedResponse>(it) }

            assertThat(response).prop(EdgeCreatedResponse::id).isNotNull()

            val savedEdge = edgeRepository.getEdgeById(response.id)
            assertThat(savedEdge).isNotNull().all {
                prop(EdgeRecord::sourceId).isEqualTo(SOURCE_ID)
                prop(EdgeRecord::targetId).isEqualTo(TARGET_ID)
            }
        }

        @Test
        fun `should respond with error if edge already exists`() {
            edgeRepository.createEdge(source = SOURCE_ID, target = TARGET_ID)

            mockMvc.createEdge(EdgeRequest(source = SOURCE_ID, target = TARGET_ID))
                .andExpect {
                    status { isConflict() }
                    content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                    jsonPath("$.title").value("Edge already exists")
                    jsonPath("$.detail").value("Edge with source $SOURCE_ID and target $TARGET_ID already exists")
                }
        }

        @Test
        fun `should respond with error if request is infalid`() {
            mockMvc.createEdge(EdgeRequest(source = SOURCE_ID, target = SOURCE_ID))
                .andExpect {
                    status { isBadRequest() }
                    content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                    jsonPath("$.title").value("Invalid request")
                    jsonPath("$.detail").value("Source and target cannot be the same")
                }
        }

        private fun MockMvc.createEdge(edgeRequest: EdgeRequest) =
            post("/supply-chain/edge") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(edgeRequest)
            }
    }

    @Nested
    inner class DeleteEdge {
        @Test
        fun `should delete existing edge`() {
            edgeRepository.createEdge(source = SOURCE_ID, target = TARGET_ID)

            mockMvc.deleteEdge(EdgeRequest(source = SOURCE_ID, target = TARGET_ID))
                .andExpect { status { isNoContent() } }
        }

        @Test
        fun `should respond with error if deleting non existent edge`() {
            mockMvc.deleteEdge(EdgeRequest(source = SOURCE_ID, target = TARGET_ID))
                .andExpect {
                    status { isConflict() }
                    content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                    jsonPath("$.title").value("Edge does not exist")
                    jsonPath("$.detail").value("Edge with $SOURCE_ID and $TARGET_ID not found and can't be deleted")
                }
        }

        private fun MockMvc.deleteEdge(edgeRequest: EdgeRequest) =
            delete("/supply-chain/edge") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(edgeRequest)
            }
    }

    @Nested
    inner class GetSupplyChainTree {
        @BeforeEach
        fun setUpTestData() {
            edgeRepository.createEdge(source = "Node 1", target = "Node 2")
            edgeRepository.createEdge(source = "Node 1", target = "Node 3")
            edgeRepository.createEdge(source = "Node 2", target = "Node 4")
            edgeRepository.createEdge(source = "Node 2", target = "Node 5")
            edgeRepository.createEdge(source = "Node 3", target = "Node 6")
            //another supply chain should not be connected to the other edges
            edgeRepository.createEdge(source = "Node 7", target = "Node 8")
            edgeRepository.createEdge(source = "Node 8", target = "Node 9")
            edgeRepository.createEdge(source = "Node 8", target = "Node 10")
        }

        @Test
        fun `should return supply chain tree from root node`() {
            val response = mockMvc.getSupplyChainTree(rootSourceId = "Node 1")
                .andExpect { status { isOk() } }
                .andReturn().response.contentAsString.let { objectMapper.readValue<SupplyChainResponse>(it) }

            assertThat(response).isNotNull().all {
                prop(SupplyChainResponse::sourceId).isEqualTo("Node 1")
                prop(SupplyChainResponse::children).isNotNull().all {
                    hasSize(2)
                    index(0).all {
                        prop(SupplyChainResponse::sourceId).isEqualTo("Node 2")
                        prop(SupplyChainResponse::children).isNotNull().all {
                            hasSize(2)
                            index(0).all {
                                prop(SupplyChainResponse::sourceId).isEqualTo("Node 4")
                                prop(SupplyChainResponse::children).isNull()
                            }
                            index(1).all {
                                prop(SupplyChainResponse::sourceId).isEqualTo("Node 5")
                                prop(SupplyChainResponse::children).isNull()
                            }
                        }
                    }
                    index(1).all {
                        prop(SupplyChainResponse::sourceId).isEqualTo("Node 3")
                        prop(SupplyChainResponse::children).isNotNull().single().all {
                            prop(SupplyChainResponse::sourceId).isEqualTo("Node 6")
                            prop(SupplyChainResponse::children).isNull()
                        }
                    }
                }
            }
        }

        @Test
        fun `should return error when root node was not found to fetch supply chain tree`() {
            mockMvc.getSupplyChainTree(rootSourceId = "Unknown Node")
                .andExpect {
                    status { isNotFound() }
                    content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
                    jsonPath("$.title").value("Supply chain tree not found")
                    jsonPath("$.detail").value("Supply chain tree for source Unknown Node not found")
                }
        }

        private fun MockMvc.getSupplyChainTree(rootSourceId: String) =
            get("/supply-chain/$rootSourceId")
    }

    companion object {

        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:18")

        private const val SOURCE_ID: String = "Node 1"
        private const val TARGET_ID: String = "Node 2"
    }
}