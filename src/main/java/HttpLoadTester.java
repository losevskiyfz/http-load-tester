import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpLoadTester {

    private static final int REQUESTS_PER_MINUTE = 60;

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = RequestGenerator.generate();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long delay = 60_000L / REQUESTS_PER_MINUTE; // Интервал между запросами в миллисекундах

        scheduler.scheduleAtFixedRate(() -> {
            Instant start = Instant.now();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                Instant end = Instant.now();
                long responseTime = Duration.between(start, end).toMillis();

                Optional<String> locationHeader = response.headers().firstValue("Location");

                locationHeader.ifPresent(string -> RequestGenerator.addId(string.replace(RequestGenerator.rootUri, "")));

                System.out.println("HTTP статус: " + response.statusCode());
                System.out.println("Время ответа: " + responseTime + " мс");
            } catch (Exception ignored) {
            }
        }, 0, delay, TimeUnit.MILLISECONDS);

        scheduler.schedule(() -> {
            scheduler.shutdown();
            System.out.println("Тестирование завершено.");
        }, 1, TimeUnit.MINUTES);
    }

}