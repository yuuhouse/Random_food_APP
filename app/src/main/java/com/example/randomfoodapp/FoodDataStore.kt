package com.example.randomfoodapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

// Extension property to delegate DataStore creation to the Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "food_app_settings")

class FoodDataStore(private val context: Context) {

    companion object {
        val CATEGORIES_KEY = stringPreferencesKey("categories_data")
        val RESTAURANTS_KEY = stringPreferencesKey("restaurants_data")
    }

    // Flow to get categories
    val categoriesFlow: Flow<List<String>> = context.dataStore.data
        .map {
            val jsonString = it[CATEGORIES_KEY] ?: "[]"
            try {
                Json.decodeFromString(ListSerializer(String.serializer()), jsonString)
            } catch (_: Exception) { // Changed e to _
                // Log error or handle corrupted data - return default
                listOf("住宿區", "工作區", "遠區") // Default if error or no data
            }
        }

    // Flow to get restaurants
    val restaurantsFlow: Flow<Map<String, List<String>>> = context.dataStore.data
        .map {
            val jsonString = it[RESTAURANTS_KEY] ?: "{}"
            try {
                Json.decodeFromString(MapSerializer(String.serializer(), ListSerializer(String.serializer())), jsonString)
            } catch (_: Exception) { // Changed e to _
                // Log error or handle corrupted data - return default
                mapOf( // Default if error or no data
                    "住宿區" to listOf("家鄉水餃", "山洞點"),
                    "工作區" to emptyList(),
                    "遠區" to emptyList()
                )
            }
        }

    // Function to save categories
    suspend fun saveCategories(categories: List<String>) {
        val jsonString = Json.encodeToString(ListSerializer(String.serializer()), categories)
        context.dataStore.edit {
            it[CATEGORIES_KEY] = jsonString
        }
    }

    // Function to save restaurants
    suspend fun saveRestaurants(restaurants: Map<String, List<String>>) {
        val jsonString = Json.encodeToString(MapSerializer(String.serializer(), ListSerializer(String.serializer())), restaurants)
        context.dataStore.edit {
            it[RESTAURANTS_KEY] = jsonString
        }
    }
}
