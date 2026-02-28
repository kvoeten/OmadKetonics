package com.kazvoeten.omadketonics.data.repository.seed

import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.IngredientCategory
import com.kazvoeten.omadketonics.model.Recipe

object RecipeCatalog {
    val recipes: List<Recipe> = listOf(
        Recipe(
            id = "m1",
            name = "Chicken \"Heaven\" Wrap",
            calories = 1816,
            protein = 111,
            carbs = 30,
            fat = 140,
            ingredients = listOf(
                Ingredient(name = "Chicken Breast (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (120g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Iceberg Lettuce (90g)", category = IngredientCategory.Produce),
                Ingredient(name = "Red Bell Pepper (80g)", category = IngredientCategory.Produce),
                Ingredient(name = "Onion (40g)", category = IngredientCategory.Produce),
                Ingredient(name = "Spicy Mayo (35g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Shredded Cheese (60g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Olive Oil (85g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Greek Yogurt (120g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (70g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Pan-sear chicken strips in olive oil.",
                "Sauté onion and bell pepper.",
                "Fry 2 eggs on the side.",
                "Wrap chicken and veg in lettuce, top with spicy mayo and cheese.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m2",
            name = "Greek Burger Feast",
            calories = 1799,
            protein = 110,
            carbs = 28,
            fat = 144,
            ingredients = listOf(
                Ingredient(name = "Ground Beef (200g)", category = IngredientCategory.Meat),
                Ingredient(name = "Canned Tuna (120g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Iceberg Lettuce (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Cucumber (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Tomato (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Greek Yogurt (190g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Shredded Cheese (90g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Olive Oil (90g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Frozen Blueberries (70g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Form beef into patties and fry.",
                "Mix tuna with mayo.",
                "Make Tzatziki: Yogurt, peeled cucumber, oil, garlic powder.",
                "Wrap patties in lettuce, smother in Tzatziki and cheese.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m3",
            name = "Egg Roll in a Bowl",
            calories = 1801,
            protein = 90,
            carbs = 27,
            fat = 150,
            ingredients = listOf(
                Ingredient(name = "Ground Pork (320g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (160g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Cabbage (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Carrots (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Green Onions (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Sesame Oil (20g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Spicy Mayo (25g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Soy Sauce (40g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Olive Oil (35g)", category = IngredientCategory.Pantry)
            ),
            instructions = listOf(
                "Brown pork in olive oil.",
                "Add shredded cabbage and carrot, sauté until soft.",
                "Fry 3 eggs to top the bowl.",
                "Drizzle with sesame oil, soy sauce, and spicy mayo.",
                "No dessert (High volume meal)."
            )
        ),
        Recipe(
            id = "m4",
            name = "Omega-3 Fish Fry",
            calories = 1800,
            protein = 93,
            carbs = 29,
            fat = 148,
            ingredients = listOf(
                Ingredient(name = "Mackerel (310g)", category = IngredientCategory.Meat),
                Ingredient(name = "Firm Tofu (180g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Bok Choy (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Red Bell Pepper (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Ginger (15g)", category = IngredientCategory.Produce),
                Ingredient(name = "Olive Oil (85g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Lemon (80g)", category = IngredientCategory.Produce),
                Ingredient(name = "Greek Yogurt (180g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (0g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Pan-fry Mackerel until skin is crispy.",
                "Cube firm tofu and pan-fry.",
                "Stir-fry bok choy, bell pepper, and ginger.",
                "Squeeze lemon over fish.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m5",
            name = "Chicken Souvlaki Plate",
            calories = 1800,
            protein = 109,
            carbs = 30,
            fat = 142,
            ingredients = listOf(
                Ingredient(name = "Chicken Thighs (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (140g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Cucumber (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Onion (80g)", category = IngredientCategory.Produce),
                Ingredient(name = "Red Bell Pepper (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Greek Yogurt (170g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Shredded Cheese (80g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Olive Oil (70g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Frozen Blueberries (30g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Pan-fry chicken thighs until crispy.",
                "Fry 2 eggs.",
                "Grill onion and bell pepper.",
                "Serve with peeled cucumber, cheese, and Tzatziki sauce.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m6",
            name = "Surf & Turf Hot Pot",
            calories = 1810,
            protein = 110,
            carbs = 30,
            fat = 136,
            ingredients = listOf(
                Ingredient(name = "Beef Hotpot Slices (200g)", category = IngredientCategory.Meat),
                Ingredient(name = "White Fish Slices (120g)", category = IngredientCategory.Freezer),
                Ingredient(name = "Frozen Tofu (180g)", category = IngredientCategory.Freezer),
                Ingredient(name = "Mushrooms (20g)", category = IngredientCategory.Produce),
                Ingredient(name = "Cabbage (10g)", category = IngredientCategory.Produce),
                Ingredient(name = "Bone Broth (400g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Shacha Sauce (245g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Soy Sauce (0g)", category = IngredientCategory.Pantry)
            ),
            instructions = listOf(
                "Boil broth, add cabbage and mushrooms until soft.",
                "Add frozen tofu to soak up flavors.",
                "Quickly boil beef and fish slices.",
                "Dip everything heavily in Shacha and Soy sauce.",
                "No dessert (Drink the soup for fat/calories)."
            )
        ),
        Recipe(
            id = "m7",
            name = "Mexican Taco Salad",
            calories = 1800,
            protein = 95,
            carbs = 29,
            fat = 148,
            ingredients = listOf(
                Ingredient(name = "Ground Beef (200g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (150g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Iceberg Lettuce (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Canned Corn (15g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Onion (90g)", category = IngredientCategory.Produce),
                Ingredient(name = "Red Bell Pepper (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Shredded Cheese (80g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Greek Yogurt (150g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Olive Oil (85g)", category = IngredientCategory.Pantry)
            ),
            instructions = listOf(
                "Brown beef with taco spices (cumin/paprika/salt).",
                "Sauté onion and pepper.",
                "Assemble over lettuce with 2 tbsp corn and 3 boiled eggs.",
                "Top with generous cheese and yogurt (as sour cream).",
                "No dessert."
            )
        ),
        Recipe(
            id = "m8",
            name = "Pork Belly Cabbage Stir-fry",
            calories = 1805,
            protein = 102,
            carbs = 28,
            fat = 139,
            ingredients = listOf(
                Ingredient(name = "Pork Belly Slices (460g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (240g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Cabbage (150g)", category = IngredientCategory.Produce),
                Ingredient(name = "Ginger (45g)", category = IngredientCategory.Produce),
                Ingredient(name = "Green Onions (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Soy Sauce (40g)", category = IngredientCategory.Pantry)
            ),
            instructions = listOf(
                "Fry pork belly slices until crispy and fat renders.",
                "Sauté ginger and large amounts of cabbage in the pork fat until soft.",
                "Scramble 2 eggs into the mix.",
                "Top with green onions and soy sauce.",
                "No dessert."
            )
        ),
        Recipe(
            id = "m9",
            name = "Creamy Keto Chicken & Bok Choy",
            calories = 1796,
            protein = 107,
            carbs = 20,
            fat = 145,
            ingredients = listOf(
                Ingredient(name = "Chicken Breast (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Bok Choy (110g)", category = IngredientCategory.Produce),
                Ingredient(name = "Greek Yogurt (200g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Shredded Cheese (80g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Olive Oil (110g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Frozen Blueberries (70g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Pan sear cubed chicken in olive oil.",
                "Steam or sauté bok choy until soft.",
                "Lower heat, stir in 3 tbsp Greek yogurt and handful of cheese to make a cream sauce.",
                "Toss chicken and greens in sauce.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m10",
            name = "Smashed Beef & Mushroom Boats",
            calories = 1792,
            protein = 103,
            carbs = 30,
            fat = 144,
            ingredients = listOf(
                Ingredient(name = "Ground Beef (200g)", category = IngredientCategory.Meat),
                Ingredient(name = "Mushrooms (20g)", category = IngredientCategory.Produce),
                Ingredient(name = "Iceberg Lettuce (40g)", category = IngredientCategory.Produce),
                Ingredient(name = "Onion (20g)", category = IngredientCategory.Produce),
                Ingredient(name = "Shredded Cheese (200g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Spicy Mayo (160g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Greek Yogurt (120g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (0g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Smash beef into thin patties in a hot pan to crisp edges.",
                "Sauté mushrooms and onions in the beef fat.",
                "Load into thick lettuce leaves, top with cheese and spicy mayo.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m11",
            name = "Mediterranean Tuna Salad Boats",
            calories = 1800,
            protein = 91,
            carbs = 25,
            fat = 153,
            ingredients = listOf(
                Ingredient(name = "Canned Tuna (140g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Cucumber (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Tomato (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Eggs (150g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Olive Oil (110g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Greek Yogurt (170g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (70g)", category = IngredientCategory.Freezer),
                Ingredient(name = "Shredded Cheese (90g)", category = IngredientCategory.Dairy)
            ),
            instructions = listOf(
                "Hollow out cucumber halves to make boats.",
                "Mix 2 cans tuna with mayo, olive oil, diced tomato.",
                "Fill boats, top with 3 sliced hardboiled eggs and cheese.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m12",
            name = "Keto Cabbage & Pork \"Okonomiyaki\"",
            calories = 1799,
            protein = 98,
            carbs = 30,
            fat = 145,
            ingredients = listOf(
                Ingredient(name = "Ground Pork (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (160g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Cabbage (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Green Onions (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Olive Oil (40g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Soy Sauce (20g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Spicy Mayo (35g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Greek Yogurt (160g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (40g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Brown the ground pork, then let it cool slightly.",
                "In a bowl, mix 4 eggs, 2 cups finely shredded cabbage, the cooked pork, and green onions.",
                "Heat olive oil in a pan, pour in the mixture, press flat. Cook until golden on both sides (like a thick pancake).",
                "Drizzle heavily with soy sauce and spicy mayo.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m13",
            name = "Steakhouse Cobb Salad",
            calories = 1800,
            protein = 101,
            carbs = 28,
            fat = 146,
            ingredients = listOf(
                Ingredient(name = "Beef Hotpot Slices (200g)", category = IngredientCategory.Meat),
                Ingredient(name = "Iceberg Lettuce (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Tomato (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Cucumber (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Eggs (150g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Shredded Cheese (90g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Olive Oil (100g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Greek Yogurt (170g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (70g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Quickly pan-fry hotpot beef slices until browned.",
                "Build a massive salad: Lettuce, peeled cucumber, tomato.",
                "Top with beef, 2 hardboiled eggs, cheese.",
                "Dress with olive oil and vinegar/lemon.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m14",
            name = "Ginger Scallion Chicken",
            calories = 1800,
            protein = 93,
            carbs = 30,
            fat = 149,
            ingredients = listOf(
                Ingredient(name = "Chicken Thighs (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Ginger (25g)", category = IngredientCategory.Produce),
                Ingredient(name = "Green Onions (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Bok Choy (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Olive Oil (90g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Eggs (160g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Greek Yogurt (170g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (70g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Pan-fry chicken thighs.",
                "Make sauce: Mince ginger and scallions, mix with 3 tbsp hot olive oil and salt.",
                "Steam bok choy. Fry 2 eggs.",
                "Pour ginger scallion sauce over chicken and veg.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m15",
            name = "Indonesian Coconut Chicken Satay",
            calories = 1801,
            protein = 94,
            carbs = 28,
            fat = 150,
            ingredients = listOf(
                Ingredient(name = "Chicken Thighs (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (150g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Cabbage (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Cucumber (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Peanut Butter (62g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Coconut Cream (70g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Soy Sauce (20g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Olive Oil (55g)", category = IngredientCategory.Pantry)
            ),
            instructions = listOf(
                "Pan-fry chicken thighs.",
                "Make Satay sauce: Mix peanut butter, coconut cream, soy sauce, and a splash of hot water.",
                "Stir-fry cabbage in olive oil.",
                "Serve chicken over cabbage with peeled cucumber sticks.",
                "Drizzle heavily with Satay sauce.",
                "No dessert."
            )
        ),
        Recipe(
            id = "m16",
            name = "Beef & Tomato Stir-fry",
            calories = 1807,
            protein = 90,
            carbs = 28,
            fat = 150,
            ingredients = listOf(
                Ingredient(name = "Beef Hotpot Slices (200g)", category = IngredientCategory.Meat),
                Ingredient(name = "Tomato (150g)", category = IngredientCategory.Produce),
                Ingredient(name = "Onion (140g)", category = IngredientCategory.Produce),
                Ingredient(name = "Eggs (320g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Soy Sauce (40g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Olive Oil (115g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Iceberg Lettuce (150g)", category = IngredientCategory.Produce)
            ),
            instructions = listOf(
                "Scramble 3 eggs softly, remove from pan.",
                "Sauté onion and thick tomato wedges until they break down slightly.",
                "Add beef slices and soy sauce, cook fast.",
                "Toss eggs back in. Serve with crisp lettuce on the side.",
                "No dessert."
            )
        ),
        Recipe(
            id = "m17",
            name = "Crispy Mackerel & Egg Salad",
            calories = 1781,
            protein = 110,
            carbs = 30,
            fat = 136,
            ingredients = listOf(
                Ingredient(name = "Mackerel (360g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (200g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Iceberg Lettuce (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Cucumber (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Spicy Mayo (120g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Greek Yogurt (160g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (20g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Pan-fry Mackerel until very crispy, flake into chunks.",
                "Chop 3 hardboiled eggs.",
                "Toss fish and eggs over lettuce and peeled cucumber.",
                "Dress heavily with spicy mayo and olive oil.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m18",
            name = "Slow-Cooked Pork Rendang",
            calories = 1806,
            protein = 91,
            carbs = 27,
            fat = 149,
            ingredients = listOf(
                Ingredient(name = "Pork Shoulder (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Coconut Cream (130g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Curry Powder/Spices (16g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Cabbage (150g)", category = IngredientCategory.Produce),
                Ingredient(name = "Olive Oil (35g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Eggs (260g)", category = IngredientCategory.Dairy)
            ),
            instructions = listOf(
                "Slow cook pork shoulder chunks in coconut cream and rendang spices until tender and reduced.",
                "Fry 2 eggs on the side.",
                "Stir-fry cabbage in olive oil to use as a base.",
                "Serve the rich pork rendang over the cabbage.",
                "No dessert. (Pro-tip: Cook a double batch of pork and freeze half!)"
            )
        ),
        Recipe(
            id = "m19",
            name = "Keto Pulled Pork Wraps",
            calories = 1800,
            protein = 110,
            carbs = 30,
            fat = 139,
            ingredients = listOf(
                Ingredient(name = "Pork Shoulder (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Iceberg Lettuce (90g)", category = IngredientCategory.Produce),
                Ingredient(name = "Red Bell Pepper (70g)", category = IngredientCategory.Produce),
                Ingredient(name = "Onion (40g)", category = IngredientCategory.Produce),
                Ingredient(name = "Spicy Mayo (85g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Shredded Cheese (170g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Greek Yogurt (160g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (0g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Slow cook pork shoulder until it pulls apart easily with a fork.",
                "Sauté sliced onions and red bell peppers.",
                "Load pulled pork and veggies into thick iceberg lettuce cups.",
                "Top generously with shredded cheese and spicy mayo.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m20",
            name = "Cheese-Stuffed Bifteki & Choriatiki",
            calories = 1800,
            protein = 104,
            carbs = 30,
            fat = 144,
            ingredients = listOf(
                Ingredient(name = "Ground Beef (200g)", category = IngredientCategory.Meat),
                Ingredient(name = "Shredded Cheese (110g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Tomato (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Cucumber (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Red Bell Pepper (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Onion (60g)", category = IngredientCategory.Produce),
                Ingredient(name = "Olive Oil (75g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Greek Yogurt (160g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (10g)", category = IngredientCategory.Freezer),
                Ingredient(name = "Eggs (150g)", category = IngredientCategory.Dairy)
            ),
            instructions = listOf(
                "Mix ground beef with salt, pepper, and diced onion. Form 2 large patties, stuff the center of each with shredded cheese, and seal the edges.",
                "Pan-fry the Bifteki in olive oil until a crust forms and cheese is melted inside.",
                "Make Choriatiki Salad: Chop tomato, peeled cucumber, bell pepper, and onion. Toss heavily in 3 tbsp olive oil.",
                "Serve burgers with salad and 2 fried eggs on the side.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m21",
            name = "Keto Chicken Gyros Plate",
            calories = 1796,
            protein = 93,
            carbs = 30,
            fat = 149,
            ingredients = listOf(
                Ingredient(name = "Chicken Thighs (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Iceberg Lettuce (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Tomato (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Onion (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Cucumber (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Greek Yogurt (170g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Olive Oil (90g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Frozen Blueberries (20g)", category = IngredientCategory.Freezer),
                Ingredient(name = "Eggs (160g)", category = IngredientCategory.Dairy)
            ),
            instructions = listOf(
                "Slice chicken thighs thinly. Marinate briefly in olive oil, lemon (if available), salt, and oregano.",
                "Pan-fry chicken over high heat until edges are crispy and caramelized.",
                "Make Tzatziki: Yogurt, grated peeled cucumber, olive oil, and garlic powder.",
                "Serve chicken over lettuce with sliced tomatoes and onions. Smother in Tzatziki.",
                "Fry 2 eggs on the side.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m22",
            name = "Keto Gemista (Stuffed Peppers)",
            calories = 1800,
            protein = 108,
            carbs = 30,
            fat = 141,
            ingredients = listOf(
                Ingredient(name = "Ground Pork (280g)", category = IngredientCategory.Meat),
                Ingredient(name = "Red Bell Pepper (90g)", category = IngredientCategory.Produce),
                Ingredient(name = "Tomato (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Onion (70g)", category = IngredientCategory.Produce),
                Ingredient(name = "Shredded Cheese (70g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Olive Oil (45g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Greek Yogurt (170g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (40g)", category = IngredientCategory.Freezer),
                Ingredient(name = "Eggs (140g)", category = IngredientCategory.Dairy)
            ),
            instructions = listOf(
                "Cut the tops off 2 Red Bell Peppers and remove seeds. (Microwave empty peppers for 3 mins to soften).",
                "Brown ground pork with diced onion and half a diced tomato in olive oil.",
                "Stuff the meat mixture into the soft bell peppers.",
                "Top heavily with shredded cheese and bake or cover in a hot pan until cheese melts.",
                "Serve with 2 fried eggs.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m23",
            name = "The \"Big Fat Greek\" Salad",
            calories = 1792,
            protein = 109,
            carbs = 24,
            fat = 141,
            ingredients = listOf(
                Ingredient(name = "Chicken Breast (300g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (120g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Tomato (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Cucumber (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Onion (40g)", category = IngredientCategory.Produce),
                Ingredient(name = "Iceberg Lettuce (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Olive Oil (105g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Shredded Cheese (50g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Greek Yogurt (120g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (30g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Boil 3 eggs.",
                "Pan-sear chicken breast in olive oil, then slice.",
                "In a massive bowl, combine lettuce, chopped tomato, peeled cucumber, and sliced onion.",
                "Pour 4 tbsp of Extra Virgin Olive Oil directly over the veggies (this is your healthy fat fuel).",
                "Top the salad with the sliced chicken, boiled eggs, and a large handful of shredded cheese.",
                "Dessert: Yogurt with blueberries."
            )
        ),
        Recipe(
            id = "m24",
            name = "Rice Cooker Soy Sauce Chicken Drums",
            calories = 1797,
            protein = 91,
            carbs = 30,
            fat = 148,
            ingredients = listOf(
                Ingredient(name = "Chicken Drumsticks (180g)", category = IngredientCategory.Meat),
                Ingredient(name = "Eggs (240g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Bok Choy (100g)", category = IngredientCategory.Produce),
                Ingredient(name = "Ginger (15g)", category = IngredientCategory.Produce),
                Ingredient(name = "Green Onions (90g)", category = IngredientCategory.Produce),
                Ingredient(name = "Soy Sauce (20g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Sesame Oil (30g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Olive Oil (65g)", category = IngredientCategory.Pantry),
                Ingredient(name = "Greek Yogurt (250g)", category = IngredientCategory.Dairy),
                Ingredient(name = "Frozen Blueberries (50g)", category = IngredientCategory.Freezer)
            ),
            instructions = listOf(
                "Place chicken drums in the rice cooker with soy sauce, minced ginger, chopped green onions, and a splash of water. Run a standard white rice cycle.",
                "Stir-fry bok choy in olive oil and sesame oil.",
                "Fry 2 eggs on the side.",
                "Serve the fall-off-the-bone chicken over the greens, drizzled with the reduced soy juices from the cooker.",
                "Dessert: Yogurt with blueberries."
            )
        )
    )

    val byId: Map<String, Recipe> = recipes.associateBy { it.id }
}


