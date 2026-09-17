package com.prewave.prewavetask.supplychain.dto

data class SupplyChainResponse(val sourceId: String?, val children: List<SupplyChainResponse>?)
