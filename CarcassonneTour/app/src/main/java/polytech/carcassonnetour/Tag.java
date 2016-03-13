package polytech.carcassonnetour;

public enum Tag {
    restaurant("Restaurant"),
    panorama("Panorama"),
    historique("Point historique"),
    commerce("Commerce"),
    souvenir("Boutique de souvenirs"),
    office("Office de tourisme"),
    pittoresque("Lieu pittoresque");

    private final String text;
    private Tag(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
