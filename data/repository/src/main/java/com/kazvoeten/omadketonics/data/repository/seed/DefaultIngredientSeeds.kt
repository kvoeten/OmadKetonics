package com.kazvoeten.omadketonics.data.repository.seed

import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.IngredientNutrition

private data class IngredientSeed(
    val code: String?,
    val productName: String,
    val brand: String?,
    val nutriScore: String?,
    val nutrition: IngredientNutrition,
)

object DefaultIngredientSeeds {
    private val seeds: Map<String, IngredientSeed> = mapOf(
        "beef hotpot slices" to IngredientSeed("2333762105323", "Beef Slices Raw", "Jumbo", null, IngredientNutrition(104f, 21f, 0f, 2f)),
        "bok choy" to IngredientSeed("00982535", "Baby Shanghai Bok Choy", "Trader Joe's", null, IngredientNutrition(12f, 1.2f, 2.3f, 0f)),
        "bone broth" to IngredientSeed("0851702007565", "Bone Broth", "Kettle & Fire", null, IngredientNutrition(17f, 4f, 0.2f, 0f)),
        "cabbage" to IngredientSeed(null, "Green Cabbage", null, null, IngredientNutrition(25f, 1.3f, 5.8f, 0.1f)),
        "canned corn" to IngredientSeed("0037100036622", "Whole Kernel Sweet Corn", "Libby's", null, IngredientNutrition(89f, 3f, 19f, 1.2f)),
        "canned tuna" to IngredientSeed("0080000517166", "Chunk Light Tuna In Water", "Starkist", null, IngredientNutrition(95f, 23f, 0f, 0.7f)),
        "carrots" to IngredientSeed("6410402023479", "Carrot, Raw", null, null, IngredientNutrition(33f, 0.9f, 7.6f, 0.2f)),
        "chicken breast" to IngredientSeed(null, "Chicken Breast Raw", null, null, IngredientNutrition(120f, 22.5f, 0f, 2.6f)),
        "chicken drumsticks" to IngredientSeed("0230843807263", "Chicken Drumstick Raw With Skin", null, null, IngredientNutrition(152f, 18.8f, 0f, 8f)),
        "chicken thighs" to IngredientSeed("2814365005666", "Chicken Thigh Raw", null, null, IngredientNutrition(168f, 18f, 0f, 11f)),
        "coconut cream" to IngredientSeed("4002359654701", "Coconut Cream", null, null, IngredientNutrition(230f, 2.3f, 6f, 23f)),
        "cucumber" to IngredientSeed("10015599", "Cucumber", null, null, IngredientNutrition(15f, 0.7f, 3.6f, 0.1f)),
        "curry powder spices" to IngredientSeed("5010204914312", "Mild Curry Powder", null, null, IngredientNutrition(296f, 12f, 53f, 14f)),
        "eggs" to IngredientSeed("2006050047918", "Eggs", "Picota", null, IngredientNutrition(143f, 12.6f, 0.7f, 9.5f)),
        "firm tofu" to IngredientSeed("0057864000783", "Fresh Medium Firm Tofu", null, null, IngredientNutrition(86f, 8.1f, 2f, 4.8f)),
        "frozen blueberries" to IngredientSeed("8480000610935", "Frozen Blueberries", "Hacendado", null, IngredientNutrition(52f, 0.7f, 11f, 0.3f)),
        "frozen tofu" to IngredientSeed(null, "Frozen Tofu", null, null, IngredientNutrition(86f, 8.1f, 2f, 4.8f)),
        "ginger" to IngredientSeed(null, "Ginger Root", null, null, IngredientNutrition(80f, 1.8f, 18f, 0.8f)),
        "greek yogurt" to IngredientSeed("0894700010434", "Greek Yogurt Whole Milk Plain", "Chobani", null, IngredientNutrition(100f, 8.8f, 4.1f, 5.9f)),
        "green onions" to IngredientSeed("0033383605036", "Green Onions", null, null, IngredientNutrition(33f, 1.8f, 7.3f, 0.2f)),
        "ground beef" to IngredientSeed("0233724729999", "88% Lean/12% Fat Ground Beef (Raw)", null, null, IngredientNutrition(188f, 20f, 0f, 12f)),
        "ground pork" to IngredientSeed("4099100137811", "Ground Pork 80% Lean 20% Fat", null, null, IngredientNutrition(268f, 19.6f, 0f, 21f)),
        "iceberg lettuce" to IngredientSeed("10010488", "Iceberg Lettuce", null, null, IngredientNutrition(14f, 0.9f, 3f, 0.1f)),
        "lemon" to IngredientSeed("8437018830183", "Raw Lemon", null, null, IngredientNutrition(29f, 1.1f, 9.3f, 0.3f)),
        "mackerel" to IngredientSeed("6111250478205", "Mackerel Fillets", null, null, IngredientNutrition(205f, 19f, 0f, 14f)),
        "mushrooms" to IngredientSeed("0070475659713", "Baby Bella Mushrooms", null, null, IngredientNutrition(22f, 3.1f, 3.3f, 0.3f)),
        "olive oil" to IngredientSeed("8005510007961", "Extra Virgin Olive Oil", "Monini", null, IngredientNutrition(884f, 0f, 0f, 100f)),
        "onion" to IngredientSeed("00399494", "Sliced Red Onions", null, null, IngredientNutrition(40f, 1.1f, 9.3f, 0.1f)),
        "peanut butter" to IngredientSeed("8906069403306", "Peanut Butter Unsweetened", "Veeba", null, IngredientNutrition(620f, 25f, 20f, 50f)),
        "pork belly slices" to IngredientSeed("90075391037105", "Pork, Belly Raw", null, null, IngredientNutrition(290f, 14f, 0f, 25f)),
        "pork shoulder" to IngredientSeed("2003201006282", "Pork Shoulder Raw", null, null, IngredientNutrition(247f, 17f, 0f, 19f)),
        "red bell pepper" to IngredientSeed(null, "Red Bell Pepper Raw", null, null, IngredientNutrition(31f, 1f, 6f, 0.3f)),
        "sesame oil" to IngredientSeed("5051008865622", "Sesame Oil", null, null, IngredientNutrition(884f, 0f, 0f, 100f)),
        "shacha sauce" to IngredientSeed(null, "Shacha Sauce", null, null, IngredientNutrition(520f, 6f, 10f, 50f)),
        "shredded cheese" to IngredientSeed("0060383195830", "Part Skim Mozzarella Shredded Cheese", null, null, IngredientNutrition(267f, 25f, 2f, 19f)),
        "soy sauce" to IngredientSeed("6921180820025", "Soy Sauce", "Pearl River Bridge", null, IngredientNutrition(53f, 8f, 4.9f, 0.6f)),
        "spicy mayo" to IngredientSeed("8710605030044", "Sriracha Mayo Sauce", "Go-Tan", null, IngredientNutrition(465f, 0.5f, 11f, 47f)),
        "tomato" to IngredientSeed(null, "Tomato Raw", null, null, IngredientNutrition(18f, 0.9f, 3.9f, 0.2f)),
        "white fish slices" to IngredientSeed("0688267069314", "Raw Skinless Boneless Cod Fillets", null, null, IngredientNutrition(82f, 18f, 0f, 0.7f)),
    )

    fun apply(ingredient: Ingredient): Ingredient {
        val key = normalizeName(ingredient.name)
        val seed = seeds[key] ?: return ingredient
        return ingredient.copy(
            apiProductCode = ingredient.apiProductCode ?: seed.code,
            apiProductName = ingredient.apiProductName ?: seed.productName,
            apiBrand = ingredient.apiBrand ?: seed.brand,
            apiNutriScore = ingredient.apiNutriScore ?: seed.nutriScore,
            nutrientsPer100g = ingredient.nutrientsPer100g ?: seed.nutrition,
        )
    }

    private fun normalizeName(raw: String): String {
        return raw
            .lowercase()
            .replace(Regex("\\s*\\([^)]*\\)"), " ")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

