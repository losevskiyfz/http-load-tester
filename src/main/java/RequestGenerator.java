import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RequestGenerator {

    public static final String rootUri = "http://localhost:8080/api/v1/book";
    private static final Random random = new Random();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> ids = ConcurrentHashMap.newKeySet();

    static {
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
    }

    public static HttpRequest generate() throws IOException, InterruptedException {
        HttpRequest request = null;
        while(request == null){
            switch (random.nextInt(4)) {
                case 1:
                    request = generateGet();
                    break;
                case 2:
                    if(!ids.isEmpty()) request = generatePut();
                    break;
                case 3:
                    if(!ids.isEmpty()) request = generateDelete();
                    break;
                default:
                    request = generatePost();
                    break;
            }
        }
        return request;
    }

    public static HttpRequest generateGet() {
        int size = random.nextInt(100) + 1;
        int page = random.nextInt(ids.size() / size + 1) + 1;
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
        String id = getRandomId();
        return HttpRequest.newBuilder()
                .uri(URI.create(rootUri + "/"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(MockBookGenerator.generateFakeBook().withId(id)), StandardCharsets.UTF_8))
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
        return idsList.get(random.nextInt(idsList.size()));
    }

    public static void addId(String id) {
        ids.add(id);
    }

    public static void removeId(String id) {
        ids.remove(id);
    }
}

