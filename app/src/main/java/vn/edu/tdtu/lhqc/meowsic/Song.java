package vn.edu.tdtu.lhqc.meowsic;

public class Song {
    private String title;
    private String artist;
    private String type; // playlist, album, artist
    private int imageRes;
    private String uriString; // optional: content uri for local audio
    private long createdAt;

    public Song(String title, String artist, int imageRes) {
        this.title = title;
        this.artist = artist;
        this.imageRes = imageRes;
        this.type = "song"; // default type for songs
        this.uriString = null;
        this.createdAt = System.currentTimeMillis();
    }

    public Song(String title, String artist, String type, int imageRes) {
        this.title = title;
        this.artist = artist;
        this.type = type;
        this.imageRes = imageRes;
        this.uriString = null;
        this.createdAt = System.currentTimeMillis();
    }

    public Song(String title, String artist, int imageRes, String uriString) {
        this.title = title;
        this.artist = artist;
        this.imageRes = imageRes;
        this.type = "song";
        this.uriString = uriString;
        this.createdAt = System.currentTimeMillis();
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getType() { return type; }
    public int getImageRes() { return imageRes; }
    public String getUriString() { return uriString; }
    public long getCreatedAt() { return createdAt; }

    // Setter methods
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setType(String type) { this.type = type; }
    public void setImageRes(int imageRes) { this.imageRes = imageRes; }
    public void setUriString(String uriString) { this.uriString = uriString; }

}

