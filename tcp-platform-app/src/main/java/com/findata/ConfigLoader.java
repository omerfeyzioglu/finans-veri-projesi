package com.findata;

import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigLoader {

    private static final String CONFIG_FILE = "/rates-config.json"; // resources kök dizininde

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