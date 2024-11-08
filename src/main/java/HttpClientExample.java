
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpClientExample {
    public static String tokenX;
    public static String external_id;
    public static String id;
    public static HashMap<String,String> comment = new HashMap<>();

    public final static Map<String, Object> picPoints = new HashMap<>();
    static HashMap<String, String> mapStars = new HashMap<>();
    static ArrayList<String> list = new ArrayList<>();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String getToken(String phoneNumber) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"device_uuid\": \"888116b697f94e47afafd747291dc0d5\", \"phone\": \""+phoneNumber+"\", \"resend\": false}"))
                .uri(URI.create("https://r-point.wb.ru/api/v1/login"))
                .header("Content-Type", "application/json, text/plain, */*")
                .header("Accept","application/json, text/plain, */*")
                .header("Accept-Encoding", "gzip, deflate, br, zstd")
                .build();

        CompletableFuture<HttpResponse<String>> response =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> makeMap = objectMapper.readValue(result, new TypeReference<>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        String token = (String) makeMap.get("data");
        return token;
    }

    public static String validate (String code, String token) throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"code\":\""+code+"\",\"token\":\""+token+"\",\"device_type\":\"DEVICE_WEB\",\"app_version\":\"v9.7.247\",\"device_name\":\"Windows 10.0.0\"}"))
                .uri(URI.create("https://r-point.wb.ru/api/v1/validate"))
                .header("Content-Type", "application/json, text/plain, */*")
                .header("Accept","application/json, text/plain, */*")
                .header("Accept-Encoding", "gzip, deflate, br, zstd")
                .build();

        CompletableFuture<HttpResponse<String>> response =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        String result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> makeMap = objectMapper.readValue(result, new TypeReference<>() {///где то тут ошибка!!!!! при парсинге полученых значений
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        LinkedHashMap<String, Object> RES= new LinkedHashMap<>((Map) makeMap.get("access"));
        String tokenValidate = (String) RES.get("token");
        tokenX=tokenValidate;
        return tokenValidate;
    }

    public static String getPicpoints(String xToken, int idPvz) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://core-point.wb.ru/pickpoints/v1/list"))
                .setHeader("X-Token", xToken) // add request header
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpHeaders headers = response.headers();

        headers = response.headers();
        //headers.map().forEach((k, v) -> System.out.println(k + ":" + v));

        // print status code
        //System.out.println(response.statusCode());

        // print response body
        //System.out.println(response.body());
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> makeMap = objectMapper.readValue(response.body(), new TypeReference<>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        String result = "";
        String json = objectMapper.writeValueAsString(makeMap.get("data"));
        JsonNode jsonArrayNode = objectMapper.readTree(json);
        for (JsonNode node : jsonArrayNode) {
            int test = node.path("external_id").asInt();
            if (test==idPvz){
                result =node.path("city")+" "+node.path("street")+" д. "+ node.path("house");
                external_id = String.valueOf(node.path("external_id"));
                picPoints.put("name", String.valueOf(node.path("name")));
                picPoints.put("id", node.path("id").asInt());
                id = String.valueOf(node.path("id"));
                picPoints.put("external_id", node.path("external_id").asInt());
            }
        }
        return result;
    }
//    public static String getStars() throws IOException, InterruptedException {
//        // File f = new File("example.txt");
//        HttpRequest request = HttpRequest.newBuilder()
//                .GET()
//                .uri(URI.create("https://point-rating.wb.ru/external/api/v1/feedbacks/pickpoint?filter.stars=1"))
//                .setHeader("X-Pickpoint-External-Id", external_id)
//                .setHeader("X-Pickpoint-Id", id)
//                .setHeader("X-Token", tokenX) // add request header
//
//                .build();
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//        HttpHeaders headers = response.headers();
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> makeMap = objectMapper.readValue(response.body(), new TypeReference<>() {
//            @Override
//            public Type getType() {
//                return super.getType();
//            }
//        });
//        HashMap<String, String> mapStars = new HashMap<>();
//
//        String json = objectMapper.writeValueAsString(makeMap.get("data"));
//        JsonNode jsonArrayNode = objectMapper.readTree(json);
//        String result = null;
//        for (JsonNode node : jsonArrayNode) {
//            mapStars.put("\"comment\"", String.valueOf(node.path("comment")));
//            mapStars.put("goods", String.valueOf(node.path("goods")));
//            String good = mapStars.get("goods").toString();
//            JsonNode jsonArrayNodes = objectMapper.readTree(good);
//            for (JsonNode node1 : jsonArrayNodes) {
//                result = " коментарий: " + mapStars.get("\"comment\"") +" "+String.valueOf(node1.path("shk_id"))+"\n";
//                //res.add(result);
//            }
//        }
//        //return res.toString();
//    }

    public static String test() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://point-rating.wb.ru/external/api/v1/feedbacks/pickpoint?filter.stars=1&filter.stars=2&filter.stars=3&filter.stars=4&filter.stars=5"))
                .setHeader("X-Pickpoint-External-Id", "159524")
                .setHeader("X-Pickpoint-Id", "28135")
                .setHeader("X-Token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzaWQiOiI2MTAxMDo5NjUyMWI1YWRkNjM4ZTMzIiwidWlkIjo2MTAxMCwid3VpZCI6MTY1NTg0OTMsInBpZCI6MjgxMzUsInhwaWQiOjE1OTUyNCwicHR5cGUiOjgsInMiOjE0LCJyIjpbMl0sImFkbWluIjpmYWxzZSwiYmwiOmZhbHNlLCJhZG1pbl9yb2xlcyI6bnVsbCwiaXNfcmVmcmVzaCI6ZmFsc2UsInJlZnJlc2hfa2V5IjoiIiwiY291bnRyeSI6IlJVIiwiYXBpZHMiOlsyODEzNSw0MTg3MCwxNjk4NiwyMTk5Niw1MzQ3MV0sImVhcGlkcyI6WzE1OTUyNCwyMTU3ODAsNTAwMDQ5NDJdLCJ1c2hzIjpbMTQsMSw5XSwidmVyc2lvbiI6MSwiZGV2aWNlX3V1aWQiOiIxMGFkZjJjZjE1Y2M0NTE0OWE5NmQ3MmY3OWY5YzE5MCIsImV4cCI6MTczMDQ4Njc1OCwiaWF0IjoxNzI5NjIyNzU4fQ.Lp6rib0gGEk6mcWH4C0OQcY7iljBSgzKbNDwc_YU7lc77gbpN_puNx9xZ1MYHcOrOQmsuOOSAs5yTtRBBHZxC08SusaNiL59QwJVLkkRYHM2Y2LpVA3QKRcctZfSPINWdf62viuvdvWuYqcVyP31wSbpF-ehM3F_ZWDNRxqKBBbQuYU9xp1xgmsgASmHP6d1CWxI6kSsWGIGAAPxtLOZftb7bKc3XIkIu8q8LoFEJTzkfLXbsdsZNbr8t8khJ8ORajOskzXO6MrGF9ipBkX8NpdiwDFJjeaMrH1yDtVpqTEG9dUHRL4pH20JB23-BnvOLYfvhaqaaxKAdW_SMPfVOg") // add request header
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpHeaders headers = response.headers();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> makeMap = objectMapper.readValue(response.body(), new TypeReference<>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });


        String json = objectMapper.writeValueAsString(makeMap.get("data"));
        JsonNode jsonArrayNode = objectMapper.readTree(json);
        String result = null;
        for (JsonNode node : jsonArrayNode) {
           if (String.valueOf(node.path("comment")).length()>=1){
               comment.put(String.valueOf(node.path("comment")),String.valueOf(node.path("stars")));
               JsonNode jsonArrayNodes = objectMapper.readTree(String.valueOf(node.path("goods")));
               for (JsonNode node1 : jsonArrayNodes) {
                   list.add("Количество звезд "+String.valueOf(node.path("stars"))+". Коментарий "+String.valueOf(node.path("comment"))+" "+ String.valueOf(node1.path("shk_id"))+"\n");
               }
           }
//            mapStars.put("goods", String.valueOf(node.path("goods")));
//            String comment = String.valueOf(node.path("comment"));
//            String good = mapStars.get("goods").toString();
//            JsonNode jsonArrayNodes = objectMapper.readTree(good);
//            for (JsonNode node1 : jsonArrayNodes) {
//                result = " коментарий: " + comment +" "+String.valueOf(node1.path("shk_id"))+"\n";
//                res.add(result);
            }
        return list.toString();
        }




    public static void main(String[] args) throws Exception {
        System.out.println(test());
       // getPicpoints();
}
}