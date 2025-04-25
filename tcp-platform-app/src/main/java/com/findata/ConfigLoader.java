package com.findata;

import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Yapılandırma dosyasını yükleyen ve işleyen yardımcı sınıf.
 * <p>
 * Bu sınıf, classpath üzerinden rates-config.json dosyasını okur,
 * JSON verilerini {@link Config} nesnesine dönüştürür ve
 * yapılandırılmış {@link Rate} nesnelerini bir Map içinde toplar.
 * </p>
 *
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public class ConfigLoader {

    /** Yapılandırma dosyasının classpath üzerindeki konumu */
    private static final String CONFIG_FILE = "/rates-config.json";

    /**
     * Yapılandırma dosyasını yükler ve verilen haritayı {@link Rate} nesneleriyle doldurur.
     * <p>
     * Bu metod, resources klasöründe bulunan JSON yapılandırma dosyasını okur,
     * okunan yapılandırmayı bir {@link Config} nesnesine dönüştürür ve
     * yapılandırma içindeki kur bilgilerini kullanarak {@link Rate} nesneleri oluşturup
     * verilen haritaya ekler.
     * </p>
     *
     * @param rateMap Yapılandırmadan oluşturulan Rate nesnelerinin ekleneceği harita
     * @return Yüklenen ve işlenen yapılandırma nesnesi
     * @throws IOException Dosya bulunamadığında veya okuma hatası oluştuğunda
     */
    public static Config loadConfig(Map<String, Rate> rateMap) throws IOException {
        Gson gson = new Gson();
        InputStream inputStream = ConfigLoader.class.getResourceAsStream(CONFIG_FILE);

        if (inputStream == null) {
            throw new FileNotFoundException("Configuration file '" + CONFIG_FILE + "' not found in resources!");
        }

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Config config = gson.fromJson(reader, Config.class);

            // rateMap'i doldur
            rateMap.clear(); // Önceki verileri temizle (varsa)
            for (Map<String, Object> rateConfig : config.getRates()) {
                String name = (String) rateConfig.get("name");
                double initialBid = ((Number) rateConfig.get("initialBid")).doubleValue();
                double initialAsk = ((Number) rateConfig.get("initialAsk")).doubleValue();
                double volatility = ((Number) rateConfig.getOrDefault("volatility", 0.01)).doubleValue(); // Volatilite ekledik
                rateMap.put(name, new Rate(name, initialBid, initialAsk, volatility));
            }
            // config.rates artık gerekli değil, temizlenebilir veya Config sınıfı düzenlenebilir.
            config.setRates(null);
            return config;
        }
    }
}