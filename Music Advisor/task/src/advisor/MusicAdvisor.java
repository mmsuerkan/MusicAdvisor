package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class MusicAdvisor {
    public static String RESOURCE = MusicApi.API_LINK;
    private static String accessToken = "";
    Scanner scanner = new Scanner(System.in);
    private static boolean authorized = false;

    String apiPathUrl = "";
    private HttpRequest apiRequest;
    List<Album> newAlbums = new ArrayList<>();
    Map<String,Category> categories =  new HashMap<String,Category>();

    public void start() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        do {
            String command = scanner.nextLine();
            String[] commands = command.toLowerCase().split(" ");
            switch (commands[0]) {
                case "auth":
                    authorizeUser();
                    break;
                case "new":
                    if (!authorized) {
                        System.out.println("Please, provide access for application.");
                        break;
                    } else {
                        apiPathUrl = RESOURCE + "/v1/browse/new-releases";
                        apiRequest = createApiPath(accessToken, apiPathUrl);
                        HttpResponse<String> res = client.send(apiRequest, HttpResponse.BodyHandlers.ofString());

                        JsonObject responseObject = JsonParser.parseString(res.body()).getAsJsonObject();
                        responseObject.getAsJsonObject("albums").getAsJsonArray("items").forEach(item -> {
                            Album album = new Album();
                            JsonObject albumJson = item.getAsJsonObject();
                            String name = albumJson.get("name").getAsString();
                            album.setName(name);
                            ArrayList<String> artistsNameList = new ArrayList<>();
                            albumJson.get("artists").getAsJsonArray().forEach(artist -> {

                                String artistName = artist.getAsJsonObject().get("name").getAsString();
                                artistsNameList.add(artistName);
                            });
                            album.setArtists(artistsNameList);
                            String link = albumJson.get("external_urls").getAsJsonObject().get("spotify").getAsString();
                            album.setLink(link);
                            newAlbums.add(album);
                        });
                        newAlbums.forEach(album -> System.out.println(album));
                    }
                    break;
                case "featured":

                    if (!authorized) {
                        System.out.println("Please, provide access for application.");
                        break;
                    } else {
                        apiPathUrl = RESOURCE + "/v1/browse/featured-playlists";
                        apiRequest = createApiPath(accessToken, apiPathUrl);
                        HttpResponse<String> res = client.send(apiRequest, HttpResponse.BodyHandlers.ofString());

                         JsonParser.parseString(res.body()).getAsJsonObject().getAsJsonObject("playlists").getAsJsonArray("items").forEach(item -> {
                          Playlist playlist = new Playlist();
                            JsonObject playlistJson = item.getAsJsonObject();
                            String name = playlistJson.get("name").getAsString();
                            playlist.setName(name);
                            String link = playlistJson.get("external_urls").getAsJsonObject().get("spotify").getAsString();
                            playlist.setLink(link);
                            System.out.println(playlist);

                         });
                    }

                    break;
                case "categories":

                    if (!authorized) {
                        System.out.println("Please, provide access for application.");
                        break;
                    } else {
                        apiPathUrl = RESOURCE + "/v1/browse/categories";
                        apiRequest = createApiPath(accessToken, apiPathUrl);
                        HttpResponse<String> res = client.send(apiRequest, HttpResponse.BodyHandlers.ofString());
                        JsonObject responseObject = JsonParser.parseString(res.body()).getAsJsonObject();
                        responseObject.getAsJsonObject("categories").getAsJsonArray("items").forEach(item -> {
                            JsonObject category = item.getAsJsonObject();
                            String name = category.get("name").getAsString();
                            String id = category.get("id").getAsString();
                            categories.put(name.toLowerCase(), new Category(name, id));
                            System.out.println(name);
                        });
                    }
                    break;
                case "playlists":
                    if (!authorized) {
                        System.out.println("Please, provide access for application.");
                        break;
                    } else {
                        String playListName = commands[1];
                        if (categories.containsKey(playListName.toLowerCase())) {
                            apiPathUrl = RESOURCE + "/v1/browse/categories/" + categories.get(playListName).getId() + "/playlists";
                            apiRequest = createApiPath(accessToken, apiPathUrl);
                            HttpResponse<String> res = client.send(apiRequest, HttpResponse.BodyHandlers.ofString());
                            JsonObject responseObject = JsonParser.parseString(res.body()).getAsJsonObject();
                            responseObject.getAsJsonObject("playlists").getAsJsonArray("items").forEach(item -> {
                                JsonObject playlist = item.getAsJsonObject();
                                String name = playlist.get("name").getAsString();
                                System.out.println(name);
                                String url = playlist.get("external_urls").getAsJsonObject().get("spotify").getAsString();
                                System.out.println(url);
                                System.out.println();
                            });
                        }else{
                            System.out.println("Unknown category name.");
                        }
                    }
                    break;
                case "exit":
                    System.out.println("---GOODBYE!---");
                    return;
            }
        } while (scanner.hasNext());
    }

    private static HttpRequest createApiPath(String accessToken,String apiPath){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(apiPath))
                .GET()
                .build();
        return httpRequest;
    }

    private static void authorizeUser() throws IOException {
        Authentication authentication = new Authentication();
        authentication.getAccessCode();
        accessToken = authentication.getAccessToken();
        authorized = true;
    }

}
