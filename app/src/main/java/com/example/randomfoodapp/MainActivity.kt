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
import androidx.compose.ui.tooling.preview.Preview // 確保這行存在
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var categories by remember { mutableStateOf(listOf("住宿區", "工作區", "遠區")) }
    var restaurants by remember {
        mutableStateOf<Map<String, List<String>>>( // Explicit type and use List<String>
            mapOf(
                "住宿區" to listOf( // Use listOf for immutable lists
                    "家鄉水餃", "山洞點", "甘泉鴨肉麵", "民生炒手", "民族鍋+其他",
                    "50嵐", "麥當勞", "肯德基", "必勝客", "真功夫", "阿滿早餐", "7-11"
                ),
                "工作區" to listOf<String>(), // Explicit type for empty list
                "遠區" to listOf<String>()  // Explicit type for empty list
            )
        )
    }
    var selectedCategory by remember { mutableStateOf("住宿區") }
    var randomResult by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var newRestaurant by remember { mutableStateOf("") }

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

            // 分類選擇
            AppDropdownMenu( // Renamed to avoid conflict if there was one, or clarify custom component
                items = categories,
                selectedItem = selectedCategory,
                onItemSelected = { selectedCategory = it },
                label = "請選擇分類"
            )

            // 分類管理
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
                        restaurants = restaurants + (newCategory to listOf<String>()) // Add with empty list
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
                        restaurants = restaurants - categoryToRemove
                        selectedCategory = categories.firstOrNull() ?: ""
                    }
                }) {
                    Text("刪除分類")
                }
            }

            // 餐廳列表
            Text("餐廳列表", fontSize = 18.sp)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White)
                    .border(1.dp, Color.Gray)
            ) {
                items(restaurants[selectedCategory] ?: listOf()) { restaurant -> // Use listOf() for safety
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

            // 隨機選擇
            Button(
                onClick = {
                    val currentRestaurants = restaurants[selectedCategory] ?: listOf()
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

            // 隨機結果
            Text(randomResult, fontSize = 16.sp)

            // 新增餐廳
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
                    if (newRestaurant.isNotBlank() && selectedCategory.isNotBlank()) {
                         restaurants[selectedCategory]?.let { currentList ->
                            restaurants = restaurants + (selectedCategory to (currentList + newRestaurant))
                        } ?: run {
                             // If the category somehow has no list (e.g., new category not yet in map fully)
                             // This case should ideally be handled by ensuring selectedCategory always has a list
                             if (categories.contains(selectedCategory)){ // ensure category exists
                                restaurants = restaurants + (selectedCategory to listOf(newRestaurant))
                             }
                        }
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
fun AppDropdownMenu( // Renamed to avoid conflict
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
        androidx.compose.material3.DropdownMenu( // Explicitly use Material 3 DropdownMenu
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
    FoodGeneratorApp()
}
