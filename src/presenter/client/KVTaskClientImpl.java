package presenter.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class KVTaskClientImpl implements KVTaskClient {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final String url;
    private final HttpClient httpClient;
    private String token;

    public KVTaskClientImpl(String url) {
        this.url = url;
        httpClient = HttpClient.newHttpClient();
        token = register();
    }

    @Override
    public void put(String key, String json) {
        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create("/save/" + key + "?API_TOKEN=" + token))
                .POST(HttpRequest.BodyPublishers.ofString(json, CHARSET))
                .build();
        try {
            HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            if (httpResponse.statusCode() != 201)
                throw new RuntimeException("Не удалось сохранить значение");
        } catch (IOException | InterruptedException | RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String load(String key) {
        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create("/load/" + key + "?API_TOKEN=" + token))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET)
            );
            if (response.statusCode() == 200)
                return response.body();
            else
                throw new RuntimeException("Не удалось получить значение");
        } catch (IOException | InterruptedException | RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String register() {
        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(url + "/register"))
                .GET()
                .build();
        try {
            HttpResponse<String> tokenResponse = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET)
            );
            if (tokenResponse.statusCode() == 200)
                return tokenResponse.body();
            else
                throw new RuntimeException("Не удалось получить токен доступа");
        } catch (IOException | InterruptedException | RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
