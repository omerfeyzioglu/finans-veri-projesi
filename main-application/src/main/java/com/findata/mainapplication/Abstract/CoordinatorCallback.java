package com.findata.mainapplication.Abstract;


import com.findata.mainapplication.model.Rate;

/**
 * PlatformConnector'ların Coordinator'a olayları bildirmek için kullanacağı arayüz.
 * <p>
 * Bu arayüz, veri platformlarından (PlatformConnector) gelen bağlantı durumları,
 * kur güncelleme bildirimleri ve hata durumlarının ana koordinatöre (Coordinator)
 * iletilmesini sağlayan callback metodlarını tanımlar.
 * </p>
 * <p>
 * Observer tasarım desenini temel alan bu yapı, veri kaynaklarının birbirinden
 * bağımsız çalışmasını ve olayların merkezi bir noktadan yönetilmesini sağlar.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public interface CoordinatorCallback {

    /**
     * Bir platformun bağlantı kurduğunu bildirir.
     * <p>
     * Bu metod, bir veri platformunun başarıyla bağlantı kurduğunda çağrılır.
     * </p>
     * 
     * @param platformName Bağlantı kuran platformun adı
     */
    void onConnect(String platformName);

    /**
     * Bir platformun bağlantısının kesildiğini bildirir.
     * <p>
     * Bu metod, bir veri platformunun bağlantısı sonlandığında veya
     * beklenmedik şekilde koptuğunda çağrılır.
     * </p>
     * 
     * @param platformName Bağlantısı kesilen platformun adı
     */
    void onDisconnect(String platformName);

    /**
     * Bir platformdan yeni kur verisi geldiğini bildirir.
     * <p>
     * Bu metod, veri platformları tarafından yeni bir kur verisi alındığında
     * çağrılır. İlgili veri, önbelleğe alınma ve işleme için koordinatöre iletilir.
     * </p>
     * 
     * @param rate Güncellenen kur bilgisi
     */
    void onRateUpdate(Rate rate);

    /**
     * Bir platformda oluşan hatayı bildirir.
     * <p>
     * Bu metod, veri platformlarında gerçekleşen hata durumlarını
     * koordinatöre iletmek için kullanılır. Koordinatör, hatanın ciddiyetine
     * göre uygun işlemleri (loglama, yeniden bağlanma girişimi vb.) yapabilir.
     * </p>
     * 
     * @param platformName Hatanın oluştuğu platformun adı
     * @param error Hata açıklaması
     */
    void onError(String platformName, String error);
}