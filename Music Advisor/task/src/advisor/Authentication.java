package advisor;

import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Authentication {

    public static String SERVER_PATH = "https://accounts.spotify.com";
    public static String REDIRECT_URI = "http://localhost:8080";
    public static String CLIENT_ID = "0d64730c85344a6d8b370dbfbb46ca04";
    public static String CLIENT_SECRET = "9ff103ebb584495ea1fadb18ac0570ac";
    public static String ACCESS_TOKEN = "";
    public static String ACCESS_CODE = "";


    final String[] accessToken = {null};

    public void getAccessCode() throws IOException {




        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String query = exchange.getRequestURI().getQuery();
                String request;
                if(query != null && query.contains("access_token")) {
                    accessToken[0] = query.substring(query.indexOf("=") + 1);
                }
                if (query != null && query.contains("code=")) {
                    accessToken[0] = query.substring(5);
                    System.out.println("code received");
                    request = "Got the code. Return back to your program.";
                }else{
                    request = "Authorization code not found. Try again.";
                }
                exchange.sendResponseHeaders(200, request.length());
                exchange.getResponseBody().write(request.getBytes());
                exchange.getResponseBody().close();
            }
        });

        server.start();

        String authRequestUri = SERVER_PATH +
                "/authorize?client_id=" + CLIENT_ID +
                "&response_type=code" +
                "&redirect_uri=" + REDIRECT_URI;

        System.out.println("use this link to request the access code:");
        System.out.println(authRequestUri);

        System.out.println("waiting for code...");
        while (accessToken[0] == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        server.stop(1);


    }

    public String getAccessToken() throws IOException {
        System.out.println("making http request for access_token...");
        HttpRequest request1 = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(SERVER_PATH +"/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=authorization_code&"
                                + "code=" + accessToken[0] + "&"
                                + "redirect_uri="+REDIRECT_URI+"&"
                                + "client_id="+CLIENT_ID+"&"
                                + "client_secret="+CLIENT_SECRET))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> res = client.send(request1, HttpResponse.BodyHandlers.ofString());
            //System.out.println("response:");
            //System.out.println(res.body());
            String access_token = JsonParser.parseString(res.body()).getAsJsonObject().get("access_token").getAsString();
            //System.out.println("access_token: " + access_token);
            System.out.println("Success!");
            return access_token;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
