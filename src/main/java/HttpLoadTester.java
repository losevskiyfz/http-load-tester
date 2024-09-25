import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import java.util.Optional;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpLoadTester {

    private static final int REQUESTS_PER_MINUTE = 1000;
    private static final Queue<Long> responseTimes = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failureCount = new AtomicInteger(0);

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(200);
        long delay = 60_000L / REQUESTS_PER_MINUTE;

        Runnable task = () -> {
	        HttpRequest request = null;
	        try {
		        request = RequestGenerator.generate();
	        } catch (IOException e) {
		        throw new RuntimeException(e);
	        } catch (InterruptedException e) {
		        throw new RuntimeException(e);
	        }
	        Instant start = Instant.now();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Instant end = Instant.now();
                        long responseTime = Duration.between(start, end).toMillis();
                        responseTimes.add(responseTime);
                        successCount.incrementAndGet();

                        Optional<String> locationHeader = response.headers().firstValue("Location");
                        locationHeader.ifPresent(string -> RequestGenerator.addId(string.replace(RequestGenerator.rootUri + "/", "")));

                        System.out.println("HTTP статус: " + response.statusCode());
                        System.out.println("Время ответа: " + responseTime + " мс");
                    })
                    .exceptionally(e -> {
                        failureCount.incrementAndGet();
                        e.printStackTrace();
                        return null;
                    });
        };

        scheduler.scheduleAtFixedRate(task, 0, delay, TimeUnit.MILLISECONDS);

        scheduler.schedule(() -> {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }

            double averageResponseTime = calculateAverageResponseTime();
            System.out.println("Среднее время ответа: " + averageResponseTime + " мс");
            System.out.println("Успешных запросов: " + successCount.get());
            System.out.println("Неудачных запросов: " + failureCount.get());
            System.out.println("Тестирование завершено.");
        }, 1, TimeUnit.MINUTES);
    }

    private static double calculateAverageResponseTime() {
        return responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }
}
