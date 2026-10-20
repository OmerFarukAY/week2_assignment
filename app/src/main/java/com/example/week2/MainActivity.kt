package com.example.week2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.week2.ui.theme.Week2_assignmentTheme
import java.util.Locale

// Donanım kaynak modelimiz (Immutability ve Smart Recomposition Skipping için @Immutable ve val alanlar)
@Immutable
data class HardwareItem(
    val id: String,
    val name: String,
    val category: String,
    val cost: Double,
    val stockCount: Int
)

// Başlangıç test donanım listesi
val sampleHardwareList = listOf(
    HardwareItem(id = "101", name = "MacBook Pro M3 Max", category = "Geliştirici Bilgisayarı", cost = 3499.0, stockCount = 5),
    HardwareItem(id = "102", name = "NVIDIA RTX 4090 Sunucu", category = "Yapay Zeka / GPU", cost = 1599.0, stockCount = 2),
    HardwareItem(id = "103", name = "Google Pixel 8 Pro", category = "Mobil Test Cihazı", cost = 999.0, stockCount = 8),
    HardwareItem(id = "104", name = "Dell UltraSharp 49\"", category = "Monitör", cost = 1199.0, stockCount = 4),
    HardwareItem(id = "105", name = "Sony WH-1000XM5 Kulaklık", category = "Odaklanma Ekipmanı", cost = 399.0, stockCount = 12),
    HardwareItem(id = "106", name = "Raspberry Pi 5 8GB", category = "IoT Geliştirme Kiti", cost = 85.0, stockCount = 25),
    HardwareItem(id = "107", name = "HHKB Professional Keyboard", category = "Ergonomik Klavye", cost = 299.0, stockCount = 0), // Stok sınırı testi (stok 0)
    HardwareItem(id = "108", name = "AMD Ryzen Threadripper", category = "Yapay Zeka / GPU", cost = 4999.0, stockCount = 1)
)

// Arayüz renkleri ve tema paleti
private object AppColors {
    val Background = Color(0xFF090D16)
    val CardSurface = Color(0xFF131A29)
    val CardBorder = Color(0xFF1F2B42)
    val TextMain = Color(0xFFF8FAFC)
    val TextSub = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    val Cyan = Color(0xFF38BDF8)
    val Emerald = Color(0xFF10B981)
    val Amber = Color(0xFFF59E0B)
    val Rose = Color(0xFFEF4444)
    val Violet = Color(0xFFA855F7)

    fun forCategory(cat: String): Color {
        return when (cat) {
            "Yapay Zeka / GPU" -> Violet
            "Geliştirici Bilgisayarı" -> Cyan
            "Mobil Test Cihazı" -> Color(0xFF60A5FA)
            "IoT Geliştirme Kiti" -> Emerald
            "Monitör" -> Color(0xFF38BDF8)
            "Odaklanma Ekipmanı" -> Color(0xFFF472B6)
            "Ergonomik Klavye" -> Amber
            else -> TextSub
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Week2_assignmentTheme {
                DevClusterScreen()
            }
        }
    }
}

// Ana stateful ekran (State Hoisting merkezi)
@Composable
fun DevClusterScreen() {
    // Harcanan toplam bütçe ve tahsis adedi (Slot Table üzerinde remember ile saklanır)
    var totalSpentBudget by remember { mutableDoubleStateOf(0.0) }
    var totalAllocatedUnits by remember { mutableIntStateOf(0) }

    // Donanım listesi state'i
    var hardwareList by remember { mutableStateOf(sampleHardwareList) }

    // Ekran döndüğünde (Activity recreation) metin ve filtre kaybolmasın diye rememberSaveable kullandım
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Tümü") }

    // Kategorileri tekilleştirip listeye çeviriyoruz
    val categories = remember {
        listOf("Tümü") + sampleHardwareList.map { it.category }.distinct()
    }

    // Turnike mantığı: Filtreleme algoritması sadece query veya liste değiştiğinde yeniden hesaplansın
    val filteredList = remember(searchQuery, selectedCategory, hardwareList) {
        hardwareList.filter { item ->
            val matchesQuery = item.name.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.id.contains(searchQuery)
            val matchesCategory = selectedCategory == "Tümü" || item.category == selectedCategory
            matchesQuery && matchesCategory
        }
    }

    // Donanım tahsis edildiğinde çalışan fonksiyon
    val handleAllocate: (HardwareItem) -> Unit = { item ->
        if (item.stockCount > 0) {
            hardwareList = hardwareList.map {
                if (it.id == item.id) it.copy(stockCount = it.stockCount - 1) else it
            }
            totalSpentBudget += item.cost
            totalAllocatedUnits += 1
        }
    }

    // Testleri kolay sıfırlayabilmek için reset fonksiyonu
    val handleReset = {
        hardwareList = sampleHardwareList
        totalSpentBudget = 0.0
        totalAllocatedUnits = 0
        searchQuery = ""
        selectedCategory = "Tümü"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Üst metrik paneli (Stateless)
            ClusterHeader(
                totalSpent = totalSpentBudget,
                allocatedCount = totalAllocatedUnits,
                remainingStock = hardwareList.sumOf { it.stockCount },
                onReset = handleReset
            )

            // Arama çubuğu (Stateless)
            HardwareSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Kategori filtreleme çipleri (Stateless)
            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // LazyColumn ile donanım listesi
            HardwareListContent(
                items = filteredList,
                onItemClick = handleAllocate,
                onClearSearch = {
                    searchQuery = ""
                    selectedCategory = "Tümü"
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// LazyColumn liste bileşeni (Stateless)
@Composable
fun HardwareListContent(
    items: List<HardwareItem>,
    onItemClick: (HardwareItem) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AppColors.CardSurface)
                        .border(1.dp, AppColors.CardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Sonuç bulunamadı",
                    color = AppColors.TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Arama kriterlerinizi değiştirmeyi deneyebilirsiniz.",
                    color = AppColors.TextMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.CardSurface)
                        .border(1.dp, AppColors.CardBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onClearSearch)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Filtreleri Sıfırla",
                        color = AppColors.Cyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nokta atışı recomposition için key = { it.id } zorunlu
            items(
                items = items,
                key = { it.id }
            ) { item ->
                HardwareCard(
                    item = item,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

// Üst bilgi ve bütçe kartı (Stateless)
@Composable
fun ClusterHeader(
    totalSpent: Double,
    allocatedCount: Int,
    remainingStock: Int,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardSurface),
        border = BorderStroke(1.dp, AppColors.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(AppColors.Emerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DEVCLUSTER MONITOR",
                        color = AppColors.Cyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.3.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E283D))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sıfırla",
                        tint = AppColors.TextSub,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "HARCANAN ÖDENEK",
                        color = AppColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", totalSpent)}",
                        color = AppColors.TextMain,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TAHSİS",
                            color = AppColors.TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$allocatedCount adet",
                            color = AppColors.Cyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "KALAN STOK",
                            color = AppColors.TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val stockColor = if (remainingStock > 10) AppColors.Emerald else AppColors.Amber
                        Text(
                            text = "$remainingStock",
                            color = stockColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Arama kutusu bileşeni (Stateless)
@Composable
fun HardwareSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.height(54.dp),
        placeholder = {
            Text(
                text = "Donanım adı, kategori veya #ID...",
                color = AppColors.TextMuted,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ara",
                tint = AppColors.Cyan,
                modifier = Modifier.size(19.dp)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Temizle",
                        tint = AppColors.TextSub,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.TextMain,
            unfocusedTextColor = AppColors.TextMain,
            focusedContainerColor = AppColors.CardSurface,
            unfocusedContainerColor = AppColors.CardSurface,
            focusedBorderColor = AppColors.Cyan,
            unfocusedBorderColor = AppColors.CardBorder,
            cursorColor = AppColors.Cyan
        )
    )
}

// Kategori hapları (Stateless)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = AppColors.CardSurface,
                    labelColor = AppColors.TextSub,
                    selectedContainerColor = Color(0xFF0369A1),
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = AppColors.CardBorder,
                    selectedBorderColor = AppColors.Cyan
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

// Donanım Kartı (Stateless)
@Composable
fun HardwareCard(
    item: HardwareItem,
    onItemClick: (HardwareItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutOfStock = item.stockCount == 0
    val categoryColor = AppColors.forCategory(item.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isOutOfStock) 0.52f else 1.0f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardSurface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isOutOfStock) Color(0xFF283447) else AppColors.CardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Modifier sıralaması: ripple sınırları için önce clickable, ardından padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !isOutOfStock,
                    onClick = { onItemClick(item) }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = categoryColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = 0.8.dp,
                                color = categoryColor.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            color = categoryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "#${item.id}",
                        color = AppColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = item.name,
                    color = AppColors.TextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$${String.format(Locale.US, "%,.2f", item.cost)}",
                    color = AppColors.Emerald,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = AppColors.Rose.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = AppColors.Rose.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "TÜKENDİ",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF0284C7), Color(0xFF0284C7).copy(alpha = 0.85f))
                                ),
                                shape = RoundedCornerShape(9.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "+ Tahsis Et",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Stok: ${item.stockCount} adet",
                        color = if (item.stockCount <= 2) AppColors.Amber else AppColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF090D16)
@Composable
fun DevClusterScreenPreview() {
    Week2_assignmentTheme {
        DevClusterScreen()
    }
}