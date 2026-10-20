# DevCluster: Donanım ve Sunucu Kaynak Yönetim Simülatörü 🚀

Bu proje, **BIL 403 Mobil Uygulama Geliştirme** dersi kapsamında geliştirilmiş, Google'ın modern **Bildirimsel Arayüz (Declarative UI / Jetpack Compose)** yaklaşımını, **Tek Yönlü Veri Akışı (Unidirectional Data Flow - UDF)** prensiplerini ve gelişmiş bellek/performans optimizasyonlarını hayata geçiren simülatör uygulamasıdır.

---

## 🛠 Mimari ve Teknik Katmanlar

### 1. Veri Katmanı (Data Layer) ve Derleyici Sözleşmesi
- **Değişmezlik (Immutability):** `HardwareItem` data class'ının tüm alanları çok iş parçacıklı (multithreaded) ortamlarda Race Condition hatalarını engellemek amacıyla istisnasız `val` anahtar kelimesiyle tanımlanmıştır.
- **Kararlılık Sözleşmesi (Smart Recomposition Skipping):** Veri sınıfı `@Immutable` anotasyonu ile işaretlenerek Compose derleyicisinin gereksiz Recomposition süreçlerini atlaması garanti altına alınmıştır.

```kotlin
@Immutable
data class HardwareItem(
    val id: String,
    val name: String,
    val category: String,
    val cost: Double,
    val stockCount: Int
)
```

---

### 2. Durum Yönetimi ve Donanımsal Koruma (State Management)
- **Çalışma Zamanı Hafıza Kilidi (Slot Table):** Toplam harcanan ödenek miktarı ve tahsis sayıları Compose'un doğrusal hafıza yapısı olan Slot Table içerisine `remember` makrosuyla kilitlenmiştir.
- **Konfigürasyon Değişim Koruması (Configuration Change Protection):** Arama çubuğuna metin yazılırken cihaz yan çevrildiğinde (Portrait → Landscape), Android işletim sisteminin Activity'yi yok edip baştan kurma döngüsüne karşı arama durumu `rememberSaveable` ile RAM dışı geçici Bundle alanında muhafaza edilir. Veri kaybı yaşanmaz.
- **Turnike Mantığı (Memoization):** Arama ve kategori filtreleme algoritmaları, her Recomposition tetiklenmesinde boş yere CPU tüketmemesi için `remember(searchQuery, selectedCategory, hardwareList)` önbellek turnikesi arkasına alınmıştır. Algoritma yalnızca ilgili bağımlılıklar değiştiğinde çalışır.

---

### 3. Liste Mimarisi ve GPU Hücre Dönüşümü (List Architecture)
- **Tembel Yükleme (Lazy Loading):** Ekranda akıcı ve bellek dostu listeleme için standart döngüler yerine `LazyColumn` mimarisi kullanılmıştır.
- **Nokta Atışı Recomposition:** Listenin ortasından bir elemanın stoğu değiştiğinde tüm listenin baştan çizilmesini önlemek amacıyla `items` bloğuna benzersiz anahtar sunan `key = { it.id }` parametresi entegre edilmiştir.

---

### 4. Mimari Ayrışma Disiplini (Stateless UI)
- **Saf Fonksiyon Tasarımı (Pure Functions):** `HardwareCard`, `HardwareSearchBar` ve `CategoryFilterRow` bileşenleri tamamen **Stateless** olarak kodlanmıştır. İçlerinde hiçbir yerel `remember` hücresi barındırmazlar.
- **State Hoisting:** Durumlar merkezi `DevClusterApp` kapsayıcısından parametre olarak aşağı iletilir (**State Down**), kullanıcı etkileşimleri ise lambda callback'ler ile yukarı fırlatılır (**Events Up**).
- **Modifier Sıralaması:** Kart bileşenlerinde dokunma dalgası (ripple) ve görsel kenar boşluklarının taşmaması için önce `.clickable()`, ardından `.padding()` hiyerarşisi uygulanmıştır.

---

## 💻 Sabit Test Veri Kümesi ve Uç Durum (Edge Case)
Hocanın sağladığı veri kümesinde yer alan **#107 ID'li HHKB Professional Keyboard (stockCount = 0)** donanımı için stok sınırı koruması eklenmiştir:
- Stok `0` olduğunda kart otomatik olarak pasifize edilir (`clickable` devre dışı).
- Kart üzerinde kırmızı **TÜKENDİ** rozeti gösterilir ve donanım bütçeyi eksiye düşürmez.

---

## 📱 Proje ve Teslim Bilgileri
- **Ders:** BIL 403 Mobil Uygulama Geliştirme
- **Paket Adı:** `com.example.week2`
- **Ana Dosya:** `app/src/main/java/com/example/week2/MainActivity.kt`
- **Geliştirme Ortamı:** Android Studio, Kotlin DSL, Jetpack Compose Material 3
