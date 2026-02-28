package com.kazvoeten.omadketonics.data.repository.impl

import com.kazvoeten.omadketonics.core.common.AppDispatchers
import com.kazvoeten.omadketonics.core.common.AppResult
import com.kazvoeten.omadketonics.data.local.dao.SearchCacheDao
import com.kazvoeten.omadketonics.data.local.entity.IngredientSearchCacheEntity
import com.kazvoeten.omadketonics.data.remote.OpenFoodFactsRemoteDataSource
import com.kazvoeten.omadketonics.domain.policy.ProductRankingPolicy
import com.kazvoeten.omadketonics.domain.repository.IngredientSearchRepository
import com.kazvoeten.omadketonics.model.IngredientNutrition
import com.kazvoeten.omadketonics.model.OpenFoodFactsProduct
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class IngredientSearchRepositoryImpl @Inject constructor(
    private val remoteDataSource: OpenFoodFactsRemoteDataSource,
    private val searchCacheDao: SearchCacheDao,
    private val productRankingPolicy: ProductRankingPolicy,
    private val dispatchers: AppDispatchers,
) : IngredientSearchRepository {
    override suspend fun searchProducts(query: String): AppResult<List<OpenFoodFactsProduct>> {
        return withContext(dispatchers.io) {
            val trimmed = query.trim()
            if (trimmed.isBlank()) return@withContext AppResult.Success(emptyList())

            val cacheKey = trimmed.lowercase(Locale.US)
            val now = System.currentTimeMillis()
            val minEpoch = now - CACHE_TTL_MS
            searchCacheDao.clearExpired(minEpoch)

            val cached = searchCacheDao.getQueryCache(cacheKey).map { it.toDomain() }

            runCatching {
                remoteDataSource.searchProducts(trimmed)
            }.fold(
                onSuccess = { products ->
                    val ranked = productRankingPolicy.rank(trimmed, products)
                    searchCacheDao.clearQuery(cacheKey)
                    searchCacheDao.upsertAll(
                        ranked.take(20).map { product ->
                            IngredientSearchCacheEntity(
                                query = cacheKey,
                                code = product.code,
                                name = product.name,
                                brand = product.brand,
                                nutriScore = product.nutriScoreGrade,
                                calories = product.nutritionPer100g.calories,
                                protein = product.nutritionPer100g.protein,
                                carbs = product.nutritionPer100g.carbs,
                                fat = product.nutritionPer100g.fat,
                                createdAtEpoch = now,
                            )
                        },
                    )
                    AppResult.Success(ranked)
                },
                onFailure = { error ->
                    if (cached.isNotEmpty()) {
                        AppResult.Success(cached)
                    } else {
                        AppResult.Error(
                            message = error.message ?: "Search failed",
                            cause = error,
                        )
                    }
                },
            )
        }
    }

    private fun IngredientSearchCacheEntity.toDomain(): OpenFoodFactsProduct {
        return OpenFoodFactsProduct(
            code = code,
            name = name,
            brand = brand,
            nutriScoreGrade = nutriScore,
            nutritionPer100g = IngredientNutrition(
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
            ),
        )
    }

    private companion object {
        const val CACHE_TTL_MS = 7L * 24L * 60L * 60L * 1000L
    }
}
