package com.example.week2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Layers
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

// =======================================================================================
// A. VERİ KATMANI (DATA LAYER) VE DERLEYİCİ SÖZLEŞMESİ (COMPILER CONTRACT)
// =======================================================================================

/**
 * Her bir donanım kaynağını temsil eden veri sınıfı.
 *
 * 1. Değişmezlik (Immutability): Çok iş parçacıklı asenkron süreçlerde Race Condition
 *    hatalarını engellemek amacıyla sınıfın tüm alanları istisnasız 'val' olarak tanımlanmıştır.
 * 2. Kararlılık Sözleşmesi: Jetpack Compose derleyicisinin akıllı atlama
 *    (Smart Recomposition Skipping) motorunu tetiklemek için @Immutable ile işaretlenmiştir.
 */
@Immutable
data class HardwareItem(
    val id: String,
    val name: String,
    val category: String,
    val cost: Double,
    val stockCount: Int
)

/**
 * Hocanın ödev şartnamesinde test için sağladığı sabit veri kümesi.
 */
val initialHardwareData = listOf(
    HardwareItem(id = "101", name = "MacBook Pro M3 Max", category = "Geliştirici Bilgisayarı", cost = 3499.0, stockCount = 5),
    HardwareItem(id = "102", name = "NVIDIA RTX 4090 Sunucu", category = "Yapay Zeka / GPU", cost = 1599.0, stockCount = 2),
    HardwareItem(id = "103", name = "Google Pixel 8 Pro", category = "Mobil Test Cihazı", cost = 999.0, stockCount = 8),
    HardwareItem(id = "104", name = "Dell UltraSharp 49\"", category = "Monitör", cost = 1199.0, stockCount = 4),
    HardwareItem(id = "105", name = "Sony WH-1000XM5 Kulaklık", category = "Odaklanma Ekipmanı", cost = 399.0, stockCount = 12),
    HardwareItem(id = "106", name = "Raspberry Pi 5 8GB", category = "IoT Geliştirme Kiti", cost = 85.0, stockCount = 25),
    HardwareItem(id = "107", name = "HHKB Professional Keyboard", category = "Ergonomik Klavye", cost = 299.0, stockCount = 0), // Stok sınırı test verisi
    HardwareItem(id = "108", name = "AMD Ryzen Threadripper", category = "Yapay Zeka / GPU", cost = 4999.0, stockCount = 1)
)

// =======================================================================================
// DESIGN SYSTEM TOKENS (Apple HIG + Studio DevOps Pro Aesthetic)
// =======================================================================================

private object ClusterTheme {
    val Background = Color(0xFF090D16)
    val SurfaceCard = Color(0xFF131A29)
    val SurfaceCardBorder = Color(0xFF1F2B42)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    // Semantic Accents
    val AccentCyan = Color(0xFF38BDF8)
    val AccentEmerald = Color(0xFF10B981)
    val AccentAmber = Color(0xFFF59E0B)
    val AccentRose = Color(0xFFEF4444)
    val AccentViolet = Color(0xFFA855F7)

    fun categoryColor(category: String): Color {
        return when (category) {
            "Yapay Zeka / GPU" -> AccentViolet
            "Geliştirici Bilgisayarı" -> AccentCyan
            "Mobil Test Cihazı" -> Color(0xFF60A5FA)
            "IoT Geliştirme Kiti" -> AccentEmerald
            "Monitör" -> Color(0xFF38BDF8)
            "Odaklanma Ekipmanı" -> Color(0xFFF472B6)
            "Ergonomik Klavye" -> AccentAmber
            else -> TextSecondary
        }
    }
}

// =======================================================================================
// MAIN ACTIVITY
// =======================================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Week2_assignmentTheme {
                DevClusterApp()
            }
        }
    }
}

// =======================================================================================
// B. DURUM YÖNETİMİ VE STATEFUL CONTAINER (STATE HOISTING ROOT)
// =======================================================================================

/**
 * Tek Yönlü Veri Akışını (UDF) ve State Hoisting mimarisini yöneten merkezi Stateful kapsayıcı.
 * Bütün dinamik durumlar burada tutulur; alt bileşenler ise tamamen Stateless tasarlanmıştır.
 */
@Composable
fun DevClusterApp() {
    // 1. Çalışma Zamanı Hafıza Kilidi (Slot Table):
    // Toplam harcanan ödenek miktarı ve tahsis sayısı Compose Slot Table içerisine remember ile kilitlenir.
    var totalSpentBudget by remember { mutableDoubleStateOf(0.0) }
    var totalAllocatedUnits by remember { mutableIntStateOf(0) }

    // Güncel donanım kümesi (tahsis edildikçe stokları güvenle güncellenir)
    var hardwareList by remember { mutableStateOf(initialHardwareData) }

    // 2. Konfigürasyon Değişim Koruması (rememberSaveable):
    // Kullanıcının arama çubuğuna (TextField) harf yazdığı esnada cihazı yan çevirmesi (Portrait -> Landscape)
    // durumunda, Activity'nin baştan kurulma döngüsüne karşı arama metni ve kategori rememberSaveable ile
    // RAM dışı geçici Bundle alanında muhafaza edilir. Ekran döndüğünde veri kaybı yaşanmaz!
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Tümü") }

    // Sabit kategori listesi ("Tümü" + veri kümesindeki benzersiz kategoriler)
    val categories = remember {
        listOf("Tümü") + initialHardwareData.map { it.category }.distinct()
    }

    // 3. Turnike Mantığı (Memoization):
    // Arama veya kategori filtreleme algoritmaları, her Recomposition tetiklenmesinde
    // boş yere CPU tüketmemesi için remember(searchQuery, selectedCategory, hardwareList) turnike
    // filtresi arkasına alınmıştır. Algoritma yalnızca ilgili anahtarlar değiştiğinde çalışır.
    val filteredList = remember(searchQuery, selectedCategory, hardwareList) {
        hardwareList.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.id.contains(searchQuery)
            val matchesCategory = selectedCategory == "Tümü" || item.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    // Donanım Tahsis Etme Olayı (Event Handler)
    val onAllocateHardware: (HardwareItem) -> Unit = { item ->
        if (item.stockCount > 0) {
            hardwareList = hardwareList.map {
                if (it.id == item.id) it.copy(stockCount = it.stockCount - 1) else it
            }
            totalSpentBudget += item.cost
            totalAllocatedUnits += 1
        }
    }

    // Küme Kaynaklarını Başa Sarma Olayı (Hocanın testlerini kolaylaştıran sıfırlama)
    val onResetCluster: () -> Unit = {
        hardwareList = initialHardwareData
        totalSpentBudget = 0.0
        totalAllocatedUnits = 0
        searchQuery = ""
        selectedCategory = "Tümü"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ClusterTheme.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dashboard Metrik Başlığı (Stateless)
            ClusterDashboardHeader(
                totalSpentBudget = totalSpentBudget,
                totalAllocatedUnits = totalAllocatedUnits,
                remainingStockCount = hardwareList.sumOf { it.stockCount },
                onResetClick = onResetCluster
            )

            // Arama Çubuğu (Stateless)
            HardwareSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Kategori Hapları (Scope Bar - Stateless)
            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // C. Liste Mimarisi ve GPU Hücre Dönüşümü (LazyColumn)
            HardwareListContent(
                items = filteredList,
                onItemClick = onAllocateHardware,
                onClearFilterClick = {
                    searchQuery = ""
                    selectedCategory = "Tümü"
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// =======================================================================================
// C. LİSTE MİMARİSİ (LAZYCOLUMN VE NOKTA ATIŞI RECOMPOSITION)
// =======================================================================================

/**
 * Donanım listesini GPU dostu Tembel Yükleme (Lazy Loading) ile sunan bileşen.
 * Standart forEach döngüleri kesinlikle kullanılmamış, LazyColumn mimarisi entegre edilmiştir.
 * Nokta atışı recomposition için key = { it.id } parametresi zorunlu olarak geçilmiştir.
 */
@Composable
fun HardwareListContent(
    items: List<HardwareItem>,
    onItemClick: (HardwareItem) -> Unit,
    onClearFilterClick: () -> Unit,
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
                        .background(ClusterTheme.SurfaceCard)
                        .border(1.dp, ClusterTheme.SurfaceCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ClusterTheme.TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Aramaya uygun donanım bulunamadı",
                    color = ClusterTheme.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Farklı bir arama terimi veya kategori deneyin.",
                    color = ClusterTheme.TextMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ClusterTheme.SurfaceCard)
                        .border(1.dp, ClusterTheme.SurfaceCardBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onClearFilterClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Filtreleri Temizle",
                        color = ClusterTheme.AccentCyan,
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
            // Nokta Atışı Recomposition:
            // Listenin ortasından bir donanımın stoğu değiştiğinde tüm listenin baştan
            // çizilmesini önlemek amacıyla items bloğuna benzersiz anahtar key = { it.id } geçilmiştir.
            items(
                items = items,
                key = { it.id }
            ) { hardwareItem ->
                HardwareCard(
                    item = hardwareItem,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

// =======================================================================================
// D. MİMARİ AYRIŞMA DİSİPLİNİ (STATELESS UI BILEŞENLERİ)
// =======================================================================================

/**
 * DevCluster Dashboard Metrik Başlığı - Tamamen Stateless.
 * Apple HIG'deki System Monitor ve Control Center göstergelerinden esinlenilmiştir.
 */
@Composable
fun ClusterDashboardHeader(
    totalSpentBudget: Double,
    totalAllocatedUnits: Int,
    remainingStockCount: Int,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClusterTheme.SurfaceCard
        ),
        border = BorderStroke(1.dp, ClusterTheme.SurfaceCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Üst Satır: Sistem Başlığı, Durum Işığı ve Sıfırlama Butonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing / Active Node Indicator
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(ClusterTheme.AccentEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DEVCLUSTER MONITOR",
                        color = ClusterTheme.AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.3.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E283D))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Simülasyonu Sıfırla",
                        tint = ClusterTheme.TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrik Tablosu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Toplam Harcanan Ödenek (Slot Table State)
                Column {
                    Text(
                        text = "HARCANAN ÖDENEK",
                        color = ClusterTheme.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", totalSpentBudget)}",
                        color = ClusterTheme.TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Tahsis & Stok Rozetleri
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Tahsis Edilen
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TAHSİS",
                            color = ClusterTheme.TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalAllocatedUnits adet",
                            color = ClusterTheme.AccentCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Kalan Stok
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "KALAN STOK",
                            color = ClusterTheme.TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val stockColor = if (remainingStockCount > 10) ClusterTheme.AccentEmerald else ClusterTheme.AccentAmber
                        Text(
                            text = "$remainingStockCount",
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

/**
 * Arama Çubuğu - Tamamen Stateless.
 * İçinde hiçbir yerel remember hücresi barındırmaz (State Down, Events Up).
 */
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
                color = ClusterTheme.TextMuted,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ara",
                tint = ClusterTheme.AccentCyan,
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
                        contentDescription = "Aramayı Temizle",
                        tint = ClusterTheme.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ClusterTheme.TextPrimary,
            unfocusedTextColor = ClusterTheme.TextPrimary,
            focusedContainerColor = ClusterTheme.SurfaceCard,
            unfocusedContainerColor = ClusterTheme.SurfaceCard,
            focusedBorderColor = ClusterTheme.AccentCyan,
            unfocusedBorderColor = ClusterTheme.SurfaceCardBorder,
            cursorColor = ClusterTheme.AccentCyan
        )
    )
}

/**
 * Kategori Filtreleme Çipleri (Scope Bar) - Tamamen Stateless.
 */
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
                    containerColor = ClusterTheme.SurfaceCard,
                    labelColor = ClusterTheme.TextSecondary,
                    selectedContainerColor = Color(0xFF0369A1),
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = ClusterTheme.SurfaceCardBorder,
                    selectedBorderColor = ClusterTheme.AccentCyan
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

/**
 * Donanım Kartı - Tamamen Stateless.
 *
 * 1. Saf Fonksiyon Tasarımı: İçerisinde hiçbir yerel remember hücresi barındırmaz.
 * 2. State Hoisting: Veriyi yukarıdan parametre olarak alır (item), tıklama olayını
 *    lambda callback ile ana container'a fırlatır (onItemClick).
 * 3. Modifier Sıralaması Kuralı: Hocanın açıkça belirttiği gibi:
 *    "kart bileşenlerinde önce .clickable(), ardından .padding() hiyerarşisi işletilerek
 *    görsel margin hatalarının önüne geçilmelidir."
 * 4. Stok Sınırı Koruması: Stok 0 ise (HHKB örneğindeki test verisi) tıklama pasifize edilir,
 *    kart opaklığı 0.55f'e düşürülür ve kırmızı "TÜKENDİ" rozeti gösterilir.
 */
@Composable
fun HardwareCard(
    item: HardwareItem,
    onItemClick: (HardwareItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutOfStock = item.stockCount == 0
    val categoryAccent = ClusterTheme.categoryColor(item.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isOutOfStock) 0.52f else 1.0f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClusterTheme.SurfaceCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isOutOfStock) Color(0xFF283447) else ClusterTheme.SurfaceCardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // MODIFIER SIRALAMASI KURALI:
        // Önce .clickable(), hemen ardından .padding() hiyerarşisi işletilmiştir.
        // Bu sayede dokunma dalgası (ripple) kartın köşelerini tam kapsar ve margin taşma hatası oluşmaz.
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
            // Sol Taraf: Donanım Bilgileri
            Column(modifier = Modifier.weight(1f)) {
                // Kategori Rozeti ve ID Etiketi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = categoryAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = 0.8.dp,
                                color = categoryAccent.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            color = categoryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "#${item.id}",
                        color = ClusterTheme.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                // Donanım Adı
                Text(
                    text = item.name,
                    color = ClusterTheme.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Fiyat
                Text(
                    text = "$${String.format(Locale.US, "%,.2f", item.cost)}",
                    color = ClusterTheme.AccentEmerald,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Sağ Taraf: Stok Durumu ve Tahsis Et Eylemi
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = ClusterTheme.AccentRose.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = ClusterTheme.AccentRose.copy(alpha = 0.45f),
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
                        color = if (item.stockCount <= 2) ClusterTheme.AccentAmber else ClusterTheme.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// =======================================================================================
// PREVIEW
// =======================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF090D16)
@Composable
fun DevClusterAppPreview() {
    Week2_assignmentTheme {
        DevClusterApp()
    }
}