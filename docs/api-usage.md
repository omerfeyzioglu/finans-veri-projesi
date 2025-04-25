# API ve Veri Kaynakları Kullanım Kılavuzu

Bu belge, Finans Veri Projesi'nin REST API'lerine ve TCP veri kaynaklarına nasıl erişileceğini detaylı olarak açıklamaktadır.

## REST API Kullanımı

REST API Simulator (`spring-platform-2`) aşağıdaki endpoint'leri sunmaktadır:

### Döviz Kurları (Exchange Rates)

#### Tüm Kurları Alma

```bash
curl -X GET http://localhost:8080/api/rates
```

Örnek Yanıt:
```json
[
  {
    "symbol": "USDTRY",
    "bid": 32.4532,
    "ask": 32.4732,
    "timestamp": "2023-10-25T13:45:23.123Z",
    "platform": "REST"
  },
  {
    "symbol": "EURTRY",
    "bid": 35.1234,
    "ask": 35.1534,
    "timestamp": "2023-10-25T13:45:23.123Z",
    "platform": "REST"
  }
]
```

#### Belirli Bir Kur İçin Fiyat Alma

```bash
curl -X GET http://localhost:8080/api/rates/USDTRY
```

Örnek Yanıt:
```json
{
  "symbol": "USDTRY",
  "bid": 32.4532,
  "ask": 32.4732,
  "timestamp": "2023-10-25T13:45:23.123Z",
  "platform": "REST"
}
```

#### Fiyat Geçmişi

```bash
curl -X GET http://localhost:8080/api/rates/USDTRY/history?from=2023-10-20T00:00:00Z&to=2023-10-25T23:59:59Z
```

Örnek Yanıt:
```json
[
  {
    "symbol": "USDTRY",
    "bid": 32.4532,
    "ask": 32.4732,
    "timestamp": "2023-10-25T13:45:23.123Z",
    "platform": "REST"
  },
  {
    "symbol": "USDTRY",
    "bid": 32.4432,
    "ask": 32.4632,
    "timestamp": "2023-10-24T14:30:45.456Z",
    "platform": "REST"
  }
]
```

### Yeni Kur Ekleme (Simülasyon)

```bash
curl -X POST http://localhost:8080/api/rates \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "GBPTRY",
    "bid": 41.5632,
    "ask": 41.5932,
    "platform": "REST"
  }'
```

## TCP Bağlantısı ile Veri Alma

TCP Simulator (`tcp-platform-app`) 8081 portundan veri akışı sağlamaktadır. Telnet veya netcat (nc) gibi araçlarla bu veri akışına bağlanabilirsiniz.

### Telnet ile Bağlanma

```bash
telnet localhost 8081
```

Bağlantı başarılı olduktan sonra, sistem otomatik olarak veri akışı göndermeye başlayacaktır.

Örnek Çıktı:
```
Connected to localhost.
Escape character is '^]'.
USDTRY|32.4531|32.4731|2023-10-25T14:10:23.123Z
EURTRY|35.1235|35.1535|2023-10-25T14:10:23.223Z
GBPTRY|41.5634|41.5934|2023-10-25T14:10:23.323Z
...
```

### Netcat (nc) ile Bağlanma

```bash
nc localhost 8081
```

Örnek Çıktı (Aynı format):
```
USDTRY|32.4531|32.4731|2023-10-25T14:10:23.123Z
EURTRY|35.1235|35.1535|2023-10-25T14:10:23.223Z
GBPTRY|41.5634|41.5934|2023-10-25T14:10:23.323Z
...
```

### TCP Veri Formatı

TCP üzerinden gelen veriler aşağıdaki formattadır:

```
SEMBOL|BID|ASK|TIMESTAMP
```

Örneğin:
```
USDTRY|32.4531|32.4731|2023-10-25T14:10:23.123Z
```

Bu formatın açıklaması:
- `SEMBOL`: Döviz kur sembolü (örn: USDTRY)
- `BID`: Alış fiyatı (5 ondalık basamaklı)
- `ASK`: Satış fiyatı (5 ondalık basamaklı)
- `TIMESTAMP`: ISO-8601 formatında tarih-saat bilgisi

## Programatik Erişim Örnekleri

### Python ile REST API'ye Erişim

```python
import requests
import json

# Tüm kurları alma
response = requests.get('http://localhost:8080/api/rates')
rates = response.json()
print(json.dumps(rates, indent=2))

# Belirli bir kuru alma
symbol = 'USDTRY'
response = requests.get(f'http://localhost:8080/api/rates/{symbol}')
rate = response.json()
print(json.dumps(rate, indent=2))
```

### Java ile TCP Bağlantısı

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class TcpClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8081;
        
        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                
                // Gelen veriyi ayrıştırma örneği
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String symbol = parts[0];
                    double bid = Double.parseDouble(parts[1]);
                    double ask = Double.parseDouble(parts[2]);
                    String timestamp = parts[3];
                    
                    System.out.println("Sembol: " + symbol);
                    System.out.println("Alış: " + bid);
                    System.out.println("Satış: " + ask);
                    System.out.println("Zaman: " + timestamp);
                    System.out.println("-----------------");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Node.js ile TCP Bağlantısı

```javascript
const net = require('net');

const client = new net.Socket();
const HOST = 'localhost';
const PORT = 8081;

client.connect(PORT, HOST, () => {
    console.log('TCP sunucusuna bağlandı');
});

client.on('data', (data) => {
    const lines = data.toString().trim().split('\n');
    
    lines.forEach(line => {
        console.log('Alınan veri:', line);
        
        // Gelen veriyi ayrıştırma
        const [symbol, bid, ask, timestamp] = line.split('|');
        console.log({
            symbol,
            bid: parseFloat(bid),
            ask: parseFloat(ask),
            timestamp
        });
    });
});

client.on('close', () => {
    console.log('Bağlantı kapatıldı');
});

client.on('error', (err) => {
    console.error('Bağlantı hatası:', err);
});
``` 