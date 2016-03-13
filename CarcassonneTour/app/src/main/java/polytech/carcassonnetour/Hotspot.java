package polytech.carcassonnetour;

public class Hotspot {

    private String name;
    private String text;
    private Tag tag;
    private double latitude;
    private double longitude;
    private int radius;
    private boolean alreadySee;

    public Hotspot(String name, String text, Tag tag, double latitude, double longitude, int radius) {
        this.name = name;
        this.text = text;
        this.tag = tag;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.alreadySee = false;
    }

    public String getName() {
        return this.name;
    }

    public String getText() {
        return this.text;
    }

    public Tag getTag() {return this.tag;}

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public int getRadius() {
        return this.radius;
    }

    public boolean isAlreadySee() {
        return this.alreadySee;
    }

    public void setAlreadySee(boolean alreadySee) {
        this.alreadySee = alreadySee;
    }

}
