package com.vn.uit.repository

import com.vn.uit.data.remote.ApiClient
import com.vn.uit.data.remote.Pagination
import com.vn.uit.data.remote.PestApi
import com.vn.uit.data.remote.PestPage
import com.vn.uit.model.Pest

class PestRepository(
    private val pestApi: PestApi
) {

    suspend fun getAllPests(
        page: Int = 0,
        size: Int = 50,
        sortBy: String = "id",
        sortDir: String = "asc"
    ): Result<PestPage> {
        return try {
            val response = pestApi.getAllPests(page, size, sortBy, sortDir)
            if (response.status == 200 && response.data != null) {
                Result.success(
                    PestPage(
                        pests = response.data,
                        pagination = response.pagination ?: Pagination(0, 0, 0, 0, true)
                    )
                )
            } else {
                Result.failure(Exception(response.message ?: "Failed to load pests"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPestById(id: String): Result<Pest> {
        return try {
            val response = pestApi.getPestById(id)
            if (response.status === 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load pest"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun increasePestOccurrence(scientificName: String): Result<Pest> {
        return try {
            val response = pestApi.increasePestOccurrence(scientificName)
            if (response.status == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to increase occurrence count"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
