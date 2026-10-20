package com.example.week2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
 * 1. Değişmezlik (Immutability): Çok iş parçacıklı süreçlerde Race Condition hatalarını
 *    önlemek amacıyla tüm alanlar istisnasız 'val' olarak tanımlanmıştır.
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
 * Hocanın ödevde test için sağladığı sabit veri kümesi.
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
 * Tüm durumlar burada üretilir ve yönetilir. Alt bileşenler ise tamamen Stateless'tır.
 */
@Composable
fun DevClusterApp() {
    // 1. Çalışma Zamanı Hafıza Kilidi (Slot Table):
    // Toplam harcanan bütçe Compose Slot Table içerisinde remember ile kilitlenir.
    var totalSpentBudget by remember { mutableDoubleStateOf(0.0) }
    var totalAllocatedUnits by remember { mutableIntStateOf(0) }

    // Güncel donanım listesi (tahsis edildikçe stokları güncellenir)
    var hardwareList by remember { mutableStateOf(initialHardwareData) }

    // 2. Konfigürasyon Değişim Koruması (rememberSaveable):
    // Cihaz yan çevrildiğinde (Portrait -> Landscape) Activity'nin baştan kurulması krizine karşı
    // arama metni ve seçili kategori RAM dışı geçici Bundle alanında muhafaza edilir.
    // Ekran döndüğünde arama satırında veri kaybı yaşanmaz!
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Tümü") }

    // Sabit kategori listesi ("Tümü" + veri kümesindeki benzersiz kategoriler)
    val categories = remember {
        listOf("Tümü") + initialHardwareData.map { it.category }.distinct()
    }

    // 3. Turnike Mantığı (Memoization):
    // Arama veya kategori filtreleme algoritmaları, her recomposition tetiklendiğinde
    // boş yere CPU tüketmemesi için remember önbellek filtresi arkasına alınmıştır.
    // Sadece searchQuery, selectedCategory veya hardwareList değiştiğinde yeniden hesaplanır.
    val filteredList = remember(searchQuery, selectedCategory, hardwareList) {
        hardwareList.filter { item ->
            val matchesQuery = item.name.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.id.contains(searchQuery)
            val matchesCategory = selectedCategory == "Tümü" || item.category == selectedCategory
            matchesQuery && matchesCategory
        }
    }

    // Donanım Tahsis Etme Olayı (Event Handler)
    val onAllocateHardware: (HardwareItem) -> Unit = { item ->
        if (item.stockCount > 0) {
            // İlgili donanımın stoğunu 1 azalt, bütçeyi ve tahsis sayısını güncelle
            hardwareList = hardwareList.map {
                if (it.id == item.id) it.copy(stockCount = it.stockCount - 1) else it
            }
            totalSpentBudget += item.cost
            totalAllocatedUnits += 1
        }
    }

    // Simülasyonu Sıfırlama Olayı (Hocanın kolayca test edebilmesi için)
    val onResetCluster: () -> Unit = {
        hardwareList = initialHardwareData
        totalSpentBudget = 0.0
        totalAllocatedUnits = 0
        searchQuery = ""
        selectedCategory = "Tümü"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        containerColor = Color(0xFF0B0F19)
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
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Kategori Hapları (Stateless)
            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // C. Liste Mimarisi ve GPU Hücre Dönüşümü (LazyColumn)
            HardwareListContent(
                items = filteredList,
                onItemClick = onAllocateHardware,
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
 * Standart forEach döngüleri kesinlikle kullanılmamış, LazyColumn mimarisi tercih edilmiştir.
 * Nokta atışı recomposition için key = { it.id } parametresi zorunlu kılınmıştır.
 */
@Composable
fun HardwareListContent(
    items: List<HardwareItem>,
    onItemClick: (HardwareItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Kriterlere uygun donanım bulunamadı.",
                color = Color(0xFF94A3B8),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Nokta Atışı Recomposition:
            // Bir eleman güncellendiğinde tüm ekranın baştan çizilmesini önlemek amacıyla
            // items bloğuna benzersiz kimlik sunan key = { it.id } parametresi zorunlu olarak geçilmiştir.
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
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131D31)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Üst Satır: Başlık & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DEVCLUSTER SIMULATOR",
                        color = Color(0xFF38BDF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sıfırla",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrik Göstergeleri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Toplam Harcanan Ödenek
                Column {
                    Text(
                        text = "HARCANAN ÖDENEK",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", totalSpentBudget)}",
                        color = Color(0xFFF1F5F9),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Tahsis / Kalan Stok
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TAHSİS",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$totalAllocatedUnits adet",
                            color = Color(0xFF38BDF8),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "KALAN STOK",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$remainingStockCount",
                            color = if (remainingStockCount > 5) Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 16.sp,
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
        modifier = modifier,
        placeholder = {
            Text(
                text = "Donanım adı, kategori veya ID ara...",
                color = Color(0xFF64748B),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ara",
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Temizle",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFFF8FAFC),
            unfocusedTextColor = Color(0xFFF8FAFC),
            focusedContainerColor = Color(0xFF131D31),
            unfocusedContainerColor = Color(0xFF131D31),
            focusedBorderColor = Color(0xFF38BDF8),
            unfocusedBorderColor = Color(0xFF1E293B),
            cursorColor = Color(0xFF38BDF8)
        )
    )
}

/**
 * Kategori Filtreleme Çipleri - Tamamen Stateless.
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
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF131D31),
                    labelColor = Color(0xFF94A3B8),
                    selectedContainerColor = Color(0xFF0284C7),
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color(0xFF1E293B),
                    selectedBorderColor = Color(0xFF38BDF8)
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
 * 2. State Hoisting: Veriyi yukarıdan alır (item), tıklama olayını yukarı fırlatır (onItemClick).
 * 3. Modifier Sıralaması Kuralı: Hocanın belirttiği gibi:
 *    "kart bileşenlerinde önce .clickable(), ardından .padding() hiyerarşisi işletilerek
 *    görsel margin hatalarının önüne geçilmelidir."
 * 4. Stok Sınırı Koruması: Stok 0 ise (HHKB örneğinde olduğu gibi) tıklama pasifize edilir
 *    ve kart yarı saydamlaştırılarak "TÜKENDİ" etiketi gösterilir.
 */
@Composable
fun HardwareCard(
    item: HardwareItem,
    onItemClick: (HardwareItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutOfStock = item.stockCount == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isOutOfStock) 0.55f else 1.0f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151F33)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isOutOfStock) Color(0xFF334155) else Color(0xFF1E293B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // MODIFIER SIRALAMASI KURALI:
        // Önce .clickable(), hemen ardından .padding() hiyerarşisi uygulanır.
        // Bu sayede dokunma efekti (ripple) kartın sınırlarını doğru kapsar ve görsel margin hatası oluşmaz.
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
                // Kategori ve ID Rozeti
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF0369A1).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "#${item.id}",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Donanım Adı
                Text(
                    text = item.name,
                    color = Color(0xFFF1F5F9),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Fiyat
                Text(
                    text = "$${String.format(Locale.US, "%,.2f", item.cost)}",
                    color = Color(0xFF34D399),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Sağ Taraf: Stok Durumu ve Tahsis Et Buton Görünümü
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFEF4444).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "TÜKENDİ",
                            color = Color(0xFFF87171),
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
                                    colors = listOf(Color(0xFF0284C7), Color(0xFF0EA5E9))
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Tahsis Et",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Stok: ${item.stockCount} adet",
                        color = if (item.stockCount <= 2) Color(0xFFF59E0B) else Color(0xFF94A3B8),
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

@Preview(showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
fun DevClusterAppPreview() {
    Week2_assignmentTheme {
        DevClusterApp()
    }
}