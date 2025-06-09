package com.vn.uit.data.remote

import com.vn.uit.model.Pest

data class PestPage(
    val pests: List<Pest>,
    val pagination: Pagination
)