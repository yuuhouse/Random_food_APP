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
            val jsonString = it[CATEGORIES_KEY] ?: "[]" // Default to empty JSON array string if null
            try {
                val decodedList = Json.decodeFromString(ListSerializer(String.serializer()), jsonString)
                if (jsonString == "[]" && decodedList.isEmpty()) {
                    listOf("住宿區", "工作區", "遠區") // Provide default categories
                } else {
                    decodedList
                }
            } catch (_: Exception) { // Catch any deserialization errors
                listOf("住宿區", "工作區", "遠區")
            }
        }

    // Flow to get restaurants
    val restaurantsFlow: Flow<Map<String, List<String>>> = context.dataStore.data
        .map {
            val jsonString = it[RESTAURANTS_KEY] ?: "{}" // Default to empty JSON object string if null
            try {
                val decodedMap = Json.decodeFromString(MapSerializer(String.serializer(), ListSerializer(String.serializer())), jsonString)
                if (jsonString == "{}" && decodedMap.isEmpty()) {
                    mapOf( // Provide default restaurants
                        "住宿區" to listOf("家鄉水餃", "山洞點", "麥當勞", "肯德基", "巷口炒飯", "阿嬤的滷肉飯", "深夜食堂", "Android Studio123"),
                        "工作區" to listOf("公司餐廳", "附近便當店", "能量補給站", "活力輕食沙拉", "咖啡與簡餐"),
                        "遠區" to listOf("景觀餐廳A", "特色小吃B", "秘境風味餐館", "山頂咖啡屋", "海邊燒烤BBQ")
                    )
                } else {
                    decodedMap
                }
            } catch (_: Exception) { // Catch any deserialization errors
                mapOf(
                    "住宿區" to listOf("家鄉水餃", "山洞點", "麥當勞", "肯德基", "巷口炒飯", "阿嬤的滷肉飯", "深夜食堂", "Android Studio123"),
                    "工作區" to listOf("公司餐廳", "附近便當店", "能量補給站", "活力輕食沙拉", "咖啡與簡餐"),
                    "遠區" to listOf("景觀餐廳A", "特色小吃B", "秘境風味餐館", "山頂咖啡屋", "海邊燒烤BBQ")
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
