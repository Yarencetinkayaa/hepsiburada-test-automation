Hepsiburada Test Automation Project

Bu proje, Hepsiburada web sitesi için geliştirilmiş bir test otomasyon projesidir.  
Testler Gauge framework kullanılarak BDD yaklaşımıyla yazılmış, Java ve Selenium WebDriver ile otomatikleştirilmiştir.

Kullanılan Teknolojiler

Java 11  
Selenium WebDriver  
Gauge  
Maven  
IntelliJ IDEA  

Proje Yaklaşımı

Test senaryoları Gauge `.spec` dosyalarında, okunabilir ve sade bir dille yazılmıştır.

Element locator bilgileri ve test verileri koddan ayrılarak JSON dosyalarında tutulmuştur.
- Element locator'ları: `allpage.json`
- Test verileri: `values.json`

Page Object Model mantığına uygun, modüler ve sürdürülebilir bir yapı kullanılmıştır.

Sabit beklemeler yerine element bazlı dinamik bekleme mekanizmaları tercih edilmiştir.

Her test temiz ve izole bir tarayıcı oturumunda (Incognito / InPrivate) başlatılır.

Desteklenen Tarayıcılar

Chrome (varsayılan)  
Edge  

Tarayıcı seçimi ortam değişkeni üzerinden yapılabilir.

Kurulum

Gereksinimler:
- Java JDK 11 veya üzeri
- Maven
- Gauge CLI

Bağımlılıkları yüklemek için proje dizininde aşağıdaki komut çalıştırılır:
'''bash
mvn clean install

Tüm testleri çalıştırmak için:
mvn gauge:execute

Otomasyon Kapsamı:
Ürün arama
Ürün listeleme kontrolleri
Ürün detay sayfası
Sepete ürün ekleme
Liste ve grid görünüm kontrolleri
Smoke test senaryoları

PROJE YAPISI:

├─ specs/                     → Gauge senaryoları (.spec)
├─ src/test/java/             → Step ve core sınıflar
├─ src/test/resources/
│  ├─ data/                   → Test verileri (values.json)
│  └─ element-infos/          → Locator dosyaları (allpage.json)
├─ env/                       → Ortam konfigürasyonları
├─ pom.xml
└─ README.md
