package com.vn.uit.data.remote

import com.vn.uit.model.Pest
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface PestApi {

    @GET("pests")
    suspend fun getAllPests(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortBy") sortBy: String = "id",
        @Query("sortDir") sortDir: String = "asc"
    ): ApiResponse<List<Pest>>

    @GET("pests/{id}")
    suspend fun getPestById(
        @Path("id") id: String
    ): ApiResponse<Pest>

    @GET("pests/scientificName/{scientificName}")
    suspend fun getPestByScientificName(
        @Path("scientificName") scientificName: String
    ): ApiResponse<Pest>


    @PATCH("pests/{scientificName}/increment-occurrence")
    suspend fun increasePestOccurrence(@Path("scientificName") scientificName: String): ApiResponse<Pest>

}