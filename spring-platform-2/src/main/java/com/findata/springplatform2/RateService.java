package com.findata.springplatform2;


import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public interface RateService {

    /**
     * Retrieves the current rate for the given full rate name (e.g., "PF2_USDTRY").
     *
     * @param fullRateName The platform-prefixed rate name.
     * @return The current Rate object, or null if not found.
     */
    Rate getRate(String fullRateName);

    // İleride başka servis metodları eklenirse buraya tanımlanabilir.
    // Örneğin, desteklenen tüm kurları listeleme gibi.
    // Map<String, Rate> getAllRates();
}