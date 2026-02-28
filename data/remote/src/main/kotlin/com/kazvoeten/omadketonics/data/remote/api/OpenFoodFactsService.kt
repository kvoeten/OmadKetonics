package com.kazvoeten.omadketonics.data.remote.api

import com.kazvoeten.omadketonics.data.remote.model.OpenFoodFactsSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFoodFactsService {
    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 24,
        @Query("fields") fields: String = "code,product_name,product_name_en,brands,nutriscore_grade,nutriments",
    ): OpenFoodFactsSearchResponseDto
}
