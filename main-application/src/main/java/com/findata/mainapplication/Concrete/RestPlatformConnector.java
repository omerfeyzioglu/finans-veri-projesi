package com.findata.mainapplication.Concrete;
import com.findata.mainapplication.Abstract.CoordinatorCallback;
import com.findata.mainapplication.Abstract.PlatformConnector;
import com.findata.mainapplication.model.Rate;
import jakarta.annotation.PostConstruct; // PostConstruct importu
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier; // Kullanılmıyorsa kaldırılabilir
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
// import org.springframework.http.HttpStatusCode; // HttpStatus yeterli olabilir
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RestPlatformConnector implements PlatformConnector {

    private static final Logger log = LoggerFactory.getLogger(RestPlatformConnector.class);

    private final WebClient.Builder webClientBuilder; // Builder'ı enjekte et
    private WebClient webClient; // WebClient'ı burada oluşturmayacağız, final olmayacak
    private CoordinatorCallback callback;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Set<String> subscribedSymbols = ConcurrentHashMap.newKeySet();

    @Value("${platform.rest.name:PF2}")
    private String platformName;

    @Value("${platform.rest.base-url:http://FAIL_IF_NOT_SET}") // Varsayılanı değiştirerek okunup okunmadığını görelim
    private String baseUrl;

    // Sadece WebClient.Builder'ı enjekte et
    @Autowired
    public RestPlatformConnector(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        // !!! WebClient burada OLUŞTURULMAYACAK !!!
    }

    // @Value ile atamalar yapıldıktan SONRA bu metot çalışacak
    @PostConstruct
    private void initializeWebClient() {
        // !!! BURAYA LOG EKLE (ÇOK ÖNEMLİ) !!!
        log.info("<<<<< [RestConnector] Initializing WebClient inside @PostConstruct. Base URL from @Value: '{}' >>>>>", baseUrl);

        if (baseUrl == null || baseUrl.equals("http://FAIL_IF_NOT_SET") || baseUrl.isBlank()) {
            log.error("<<<<< [RestConnector] CRITICAL ERROR: platform.rest.base-url property is missing or empty! Check application.properties! >>>>>");
            // Hata fırlatmak veya uygulamayı durdurmak daha iyi olabilir
            // throw new IllegalStateException("Base URL for REST platform is not configured.");
            // Şimdilik sadece loglayalım ama bu durumda çalışmayacaktır.
            this.webClient = WebClient.create(); // Geçici, hatalı bir webclient
        } else {
            // WebClient'ı @Value ile gelen baseUrl ile burada oluştur
            this.webClient = webClientBuilder.baseUrl(this.baseUrl).build();
            log.info("[{}] WebClient initialized with base URL: {}", platformName, this.baseUrl);
        }
    }


    @Override
    public void connect() {
        // @PostConstruct'da WebClient zaten oluşturulmuş olmalı
        if (this.webClient == null) {
            log.error("[{}] Cannot connect, WebClient is not initialized (check Base URL config).", platformName);
            return;
        }
        log.info("Attempting to 'connect' to REST platform [{}]. (Polling will start via @Scheduled)", platformName);
        connected.set(true);
        if (callback != null) {
            callback.onConnect(platformName);
        }
    }

    // --- disconnect, cleanup, subscribe, unsubscribe, setCallback, isConnected, getPlatformName ---
    // --- BU METOTLAR AYNI KALABİLİR ---
    @Override
    public void disconnect() {
        log.info("Disconnecting from REST platform [{}]. (Polling will stop)", platformName);
        connected.set(false);
        subscribedSymbols.clear();
        if (callback != null) {
            callback.onDisconnect(platformName);
        }
    }

    @PreDestroy // Bean yok edilirken çağrılır
    public void cleanup() {
        disconnect();
    }


    @Override
    public void subscribe(String symbol) {
        log.info("[{}] Subscribing to symbol: {}", platformName, symbol);
        subscribedSymbols.add(symbol);
    }

    @Override
    public void unsubscribe(String symbol) {
        log.info("[{}] Unsubscribing from symbol: {}", platformName, symbol);
        subscribedSymbols.remove(symbol);
    }

    @Override
    public void setCallback(CoordinatorCallback callback) {
        log.debug("[{}] Setting coordinator callback.", platformName);
        this.callback = callback;
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public String getPlatformName() {
        return platformName;
    }

    // --- pollRates ve fetchRateForSymbol AYNI KALABİLİR ---
    // --- Sadece fetchRateForSymbol içindeki logu kontrol et ---
    @Scheduled(fixedRateString = "${platform.rest.poll-interval-ms:5000}")
    public void pollRates() {
        // !!! BU LOGU EKLE/KONTROL ET !!!
        log.info("[{}] Running scheduled poll task for {} symbols...", platformName, subscribedSymbols.size());

        if (!connected.get() || subscribedSymbols.isEmpty() || this.webClient == null) {
            if(this.webClient == null) log.warn("[{}] Skipping poll task, WebClient not initialized.", platformName);
            return;
        }
        log.trace("[{}] Polling rates for symbols: {}", platformName, subscribedSymbols);

        Flux.fromIterable(subscribedSymbols)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(this::fetchRateForSymbol)
                .sequential()
                .subscribe(
                        rate -> {
                            // !!! BU LOGU EKLE/KONTROL ET !!!
                            log.debug("[{}] Successfully fetched and mapped rate, calling onRateUpdate: {}", platformName, rate);
                            if (callback != null && rate != null) {
                                callback.onRateUpdate(rate);
                            }
                        },
                        error -> {
                            log.error("[{}] Error during polling reactive chain: {}", platformName, error.getMessage());
                            if (callback != null) {
                                callback.onError(platformName,"Polling error: " + error.getMessage());
                            }
                        }
                );
    }

    private Mono<Rate> fetchRateForSymbol(String symbol) {
        if (this.webClient == null) return Mono.error(new IllegalStateException("WebClient not initialized"));

        String fullRateName = platformName + "_" + symbol;
        String relativeUrl = "/api/rates/" + fullRateName;

        // !!! BU LOGU EKLE/KONTROL ET !!!
        log.debug("<<<<< [RestConnector] Attempting GET request to BaseURL: '{}' with URI: '{}' >>>>>", baseUrl, relativeUrl);

        return this.webClient.get()
                .uri(relativeUrl)
                .retrieve()
                .bodyToMono(RateDTO.class)
                .map(dto -> new Rate(platformName, symbol, dto.bid(), dto.ask(), dto.timestamp()))
                .timeout(Duration.ofSeconds(3))
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException e) {
                        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                            log.warn("[{}] Rate not found on platform for symbol {}: {}", platformName, symbol, e.getStatusCode());
                        } else {
                            log.error("[{}] Error fetching rate for symbol {}: {} - {}", platformName, symbol, e.getStatusCode(), e.getResponseBodyAsString(), e);
                        }
                    } else {
                        log.error("[{}] Network or other error fetching rate for symbol {}: {}", platformName, symbol, error.getMessage());
                    }
                })
                .onErrorResume(error -> Mono.empty());
    }

    private static record RateDTO(String rateName, double bid, double ask, Instant timestamp) {}

}