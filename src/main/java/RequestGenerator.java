import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestGenerator {

    private static MockBookGenerator mockBookGenerator;
    public static final String rootUri = "localhost:8080/api/v1/book";
    private static final Random random = new Random();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> ids = new HashSet<>();

    public static HttpRequest generate() throws IOException, InterruptedException {
        switch (random.nextInt(4)) {
            case 0:
                return generateGet();
            case 1:
                return generatePost();
            case 2:
                return generatePut();
            case 3:
                return generateDelete();
        }
    }

    public static HttpRequest generateGet() throws IOException, InterruptedException {
        int size = random.nextInt(100) + 1;
        int page = random.nextInt(ids.size() / size) + 1;
        String uri = rootUri + "?page=" + page + "&size=" + size;

        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
    }

    public static HttpRequest generatePost() throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .uri(URI.create(rootUri))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(MockBookGenerator.generateFakeBook()), StandardCharsets.UTF_8))
                .build();
    }

    public static HttpRequest generatePut() throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .uri(URI.create(rootUri + "/" + getRandomId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(MockBookGenerator.generateFakeBook()), StandardCharsets.UTF_8))
                .build();
    }

    public static HttpRequest generateDelete() {
        String id = getRandomId();
        removeId(id);
        return HttpRequest.newBuilder()
                .uri(URI.create(rootUri + "/" + id))
                .DELETE()
                .build();
    }

    public static String getRandomId() {
        List<String> idsList = new ArrayList<>(ids);
        String randomElement = idsList.get(random.nextInt(idsList.size()));
        ids.remove(randomElement);
        return randomElement;
    }

    public static void addId(String id) {
        ids.add(id);
    }

    public static void removeId(String id) {
        ids.remove(id);
    }

}
