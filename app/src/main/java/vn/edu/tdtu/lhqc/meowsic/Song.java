package vn.edu.tdtu.lhqc.meowsic;

public class Song {
    private String title;
    private String artist;
    private String type; // playlist, album, artist
    private int imageRes;
    private String uriString; // optional: content uri for local audio

    public Song(String title, String artist, int imageRes) {
        this.title = title;
        this.artist = artist;
        this.imageRes = imageRes;
        this.type = "song"; // default type for songs
        this.uriString = null;
    }

    public Song(String title, String artist, String type, int imageRes) {
        this.title = title;
        this.artist = artist;
        this.type = type;
        this.imageRes = imageRes;
        this.uriString = null;
    }

    public Song(String title, String artist, int imageRes, String uriString) {
        this.title = title;
        this.artist = artist;
        this.imageRes = imageRes;
        this.type = "song";
        this.uriString = uriString;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getType() { return type; }
    public int getImageRes() { return imageRes; }
    public String getUriString() { return uriString; }
}

