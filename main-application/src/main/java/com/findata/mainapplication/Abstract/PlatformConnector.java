package com.findata.mainapplication.Abstract;

/**
 * Farklı veri platformlarına bağlanmak için genel arayüz.
 * Her platform (TCP, REST, FIX vb.) bu arayüzü implemente etmelidir.
 * <p>
 * Bu arayüz, veri kaynaklarına bağlanma, sembol aboneliği yönetimi ve
 * bağlantı durumu kontrolü için gerekli metodları tanımlar. Farklı protokoller
 * ve veri kaynakları için tutarlı bir API sağlayarak, ana uygulamanın
 * altta yatan iletişim detaylarından soyutlanmasını sağlar.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public interface PlatformConnector {

    /**
     * Veri platformuna bağlantı başlatır.
     * <p>
     * Bu metod, bağlantı kurulumu yapar ve gerekli handshake protokollerini
     * uygular. Başarısız olması durumunda uygun bir hata fırlatmalıdır.
     * </p>
     */
    void connect();

    /**
     * Veri platformu ile olan bağlantıyı sonlandırır.
     * <p>
     * Bu metod, açık kaynakları serbest bırakır ve socket, HTTP client gibi
     * bağlantıları düzgün şekilde kapatır.
     * </p>
     */
    void disconnect();

    /**
     * Belirtilen sembol için veri aboneliği başlatır.
     * <p>
     * Bu metod, platformdan belirtilen sembole ait veri akışı almak için
     * gerekli abonelik işlemlerini gerçekleştirir.
     * </p>
     * 
     * @param symbol Abone olunacak sembol (örn: "USDTRY")
     */
    void subscribe(String symbol);

    /**
     * Belirtilen sembol için veri aboneliğini sonlandırır.
     * <p>
     * Bu metod, daha önce abone olunan bir sembol için veri alımını durdurur.
     * </p>
     * 
     * @param symbol Aboneliği sonlandırılacak sembol (örn: "USDTRY")
     */
    void unsubscribe(String symbol);

    /**
     * Bu connector'dan gelen bildirimleri alacak olan Coordinator örneğini ayarlar.
     * <p>
     * Bu metod, connector'dan gelen verilerin iletileceği callback nesnesini tanımlar.
     * </p>
     * 
     * @param callback Bildirimleri alacak olan Coordinator callback nesnesi
     */
    void setCallback(CoordinatorCallback callback);

    /**
     * Bağlantı durumunu kontrol eder.
     * <p>
     * Bu metod, veri platformuna olan bağlantının aktif olup olmadığını döndürür.
     * </p>
     * 
     * @return Bağlantı aktifse true, değilse false
     */
    boolean isConnected();

    /**
     * Bu connector'ın temsil ettiği platformun adını döndürür (Konfigürasyondan alınabilir).
     * <p>
     * Bu metod, connector'ın hangi veri kaynağını temsil ettiğini belirten
     * benzersiz bir tanımlayıcı döndürür.
     * </p>
     * 
     * @return Platform adı (örn: "PF1", "REST-API", "TCP-SOURCE")
     */
    String getPlatformName();
}