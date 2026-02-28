package com.kazvoeten.omadketonics.data.repository.policy

import com.kazvoeten.omadketonics.domain.policy.ProductRankingPolicy
import com.kazvoeten.omadketonics.model.OpenFoodFactsProduct
import java.util.Locale
import javax.inject.Inject

class DefaultProductRankingPolicy @Inject constructor() : ProductRankingPolicy {
    override fun rank(query: String, products: List<OpenFoodFactsProduct>): List<OpenFoodFactsProduct> {
        val qTokens = query.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 3 }

        fun score(item: OpenFoodFactsProduct): Int {
            val text = "${item.name} ${item.brand.orEmpty()}".lowercase(Locale.US)
            val hits = qTokens.count { text.contains(it) }
            var s = hits * 8
            if (qTokens.isNotEmpty() && hits == qTokens.size) s += 8
            if (text.contains("raw") || text.contains("plain") || text.contains("unsweetened")) s += 3
            val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (words.size > 8) s -= (words.size - 8) * 2
            if (
                Regex(
                    "chips|cookie|chocolate|bar|soda|cola|granola|cracker|ramen|soup|sandwich|ravioli|pizza|burger|dip|dressing|ketchup|beer|tea|drink|flavored|blend|mix|pancake|bowl|wrap|pocket|snack|sausage|bun|breaded|stuffed|pickled|honey|marinated",
                ).containsMatchIn(text)
            ) {
                s -= 25
            }
            return s
        }

        return products
            .distinctBy { it.code }
            .sortedByDescending(::score)
            .take(18)
    }
}
