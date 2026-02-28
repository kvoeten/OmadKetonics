package com.kazvoeten.omadketonics.data.remote.model

import com.squareup.moshi.Json

data class OpenFoodFactsSearchResponseDto(
    @Json(name = "products")
    val products: List<ProductDto>?,
)

data class ProductDto(
    @Json(name = "code")
    val code: String?,
    @Json(name = "product_name")
    val productName: String?,
    @Json(name = "product_name_en")
    val productNameEn: String?,
    @Json(name = "brands")
    val brands: String?,
    @Json(name = "nutriscore_grade")
    val nutriScoreGrade: String?,
    @Json(name = "nutriments")
    val nutriments: NutrimentsDto?,
)

data class NutrimentsDto(
    @Json(name = "energy-kcal_100g")
    val energyKcal100g: Double?,
    @Json(name = "energy-kcal")
    val energyKcal: Double?,
    @Json(name = "proteins_100g")
    val proteins100g: Double?,
    @Json(name = "proteins")
    val proteins: Double?,
    @Json(name = "carbohydrates_100g")
    val carbohydrates100g: Double?,
    @Json(name = "carbohydrates")
    val carbohydrates: Double?,
    @Json(name = "fat_100g")
    val fat100g: Double?,
    @Json(name = "fat")
    val fat: Double?,
)
