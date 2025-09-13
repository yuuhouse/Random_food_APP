package com.example.randomfoodapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodGeneratorApp()
        }
    }
}

@Composable
fun FoodGeneratorApp() {
    val context = LocalContext.current
    val foodDataStore = remember { FoodDataStore(context) }
    val scope = rememberCoroutineScope()

    var categories by remember { mutableStateOf(emptyList<String>()) }
    var restaurants by remember { mutableStateOf(emptyMap<String, List<String>>()) }
    var selectedCategory by remember { mutableStateOf("") }
    var randomResult by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var newRestaurant by remember { mutableStateOf("") }

    var categoriesInitialLoadDone by remember { mutableStateOf(false) }
    var restaurantsInitialLoadDone by remember { mutableStateOf(false) }

    // Load categories from DataStore
    LaunchedEffect(foodDataStore) {
        foodDataStore.categoriesFlow.collect { loadedCategories ->
            categories = loadedCategories
            categoriesInitialLoadDone = true
        }
    }

    // Load restaurants from DataStore
    LaunchedEffect(foodDataStore) {
        foodDataStore.restaurantsFlow.collect { loadedRestaurants ->
            restaurants = loadedRestaurants
            restaurantsInitialLoadDone = true
        }
    }

    // Effect to manage selectedCategory based on loaded/updated categories
    LaunchedEffect(categories, categoriesInitialLoadDone) {
        if (categoriesInitialLoadDone) {
            if (selectedCategory.isBlank() || !categories.contains(selectedCategory)) {
                selectedCategory = categories.firstOrNull() ?: ""
            }
        }
    }

    // Save categories when they change after initial load
    LaunchedEffect(categories) {
        if (categoriesInitialLoadDone) {
            scope.launch {
                foodDataStore.saveCategories(categories)
            }
        }
    }

    // Save restaurants when they change after initial load
    LaunchedEffect(restaurants) {
        if (restaurantsInitialLoadDone) {
            scope.launch {
                foodDataStore.saveRestaurants(restaurants)
            }
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color(0xFFF0F0F0)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "隨機吃啥產生器",
                fontSize = 24.sp,
                color = Color(0xFF2C3E50)
            )

            AppDropdownMenu(
                items = categories,
                selectedItem = selectedCategory,
                onItemSelected = { selectedCategory = it },
                label = "請選擇分類"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newCategory,
                    onValueChange = { newCategory = it },
                    label = { Text("新分類") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    if (newCategory.isNotBlank() && newCategory !in categories) {
                        categories = categories + newCategory
                        // Ensure new category has an entry in restaurants map
                        if (!restaurants.containsKey(newCategory)){
                            restaurants = restaurants + (newCategory to emptyList())
                        }
                        selectedCategory = newCategory
                        newCategory = ""
                    }
                }) {
                    Text("添加分類")
                }
                Button(onClick = {
                    if (categories.size > 1 && categories.contains(selectedCategory)) {
                        val categoryToRemove = selectedCategory
                        categories = categories - categoryToRemove
                        restaurants = restaurants - categoryToRemove // This also removes the restaurants for that category
                        // selectedCategory will be updated by LaunchedEffect(categories, ...)
                    }
                }) {
                    Text("刪除分類")
                }
            }

            Text("餐廳列表", fontSize = 18.sp)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White)
                    .border(1.dp, Color.Gray)
            ) {
                items(restaurants[selectedCategory] ?: emptyList()) { restaurant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(restaurant)
                        IconButton(onClick = {
                            restaurants[selectedCategory]?.let { currentList ->
                                restaurants = restaurants + (selectedCategory to (currentList - restaurant))
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "刪除")
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val currentRestaurants = restaurants[selectedCategory] ?: emptyList()
                    randomResult = if (currentRestaurants.isNotEmpty()) {
                        "你可以去: ${currentRestaurants.random()}"
                    } else {
                        "沒有可選擇的餐廳"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50))
            ) {
                Text("吃什麼？", color = Color.White)
            }

            Text(randomResult, fontSize = 16.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newRestaurant,
                    onValueChange = { newRestaurant = it },
                    label = { Text("新餐廳") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    if (newRestaurant.isNotBlank() && selectedCategory.isNotBlank() && categories.contains(selectedCategory)) {
                        val currentList = restaurants[selectedCategory] ?: emptyList()
                        restaurants = restaurants + (selectedCategory to (currentList + newRestaurant))
                        newRestaurant = ""
                    }
                }) {
                    Text("添加餐廳")
                }
            }
        }
    }
}

@Composable
fun AppDropdownMenu(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = if (selectedItem.isNotBlank() && items.contains(selectedItem)) selectedItem else label,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun FoodGeneratorAppPreview() {
    // Preview might not work correctly with DataStore context.
    // For a working preview, you might need to pass mock data or a fake DataStore.
    FoodGeneratorApp()
}
