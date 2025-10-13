package vn.edu.tdtu.lhqc.meowsic;

public class Song {
    private String title;
    private String type; //playlist, album, artist

    private int imageRes;

    public Song(String title, String type, int imageRes) {
        this.title = title;
        this.type = type;
        this.imageRes = imageRes;
    }

    public String getTitle() { return title; }
    public String getType() { return type; }

    public int getImageRes() { return imageRes; }
}

