package advisor;

public class Playlist {
    private String name;
    private String link;

    public Playlist() {
    }

    public Playlist(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return name + "\n" +
                link + "\n";
    }

}
