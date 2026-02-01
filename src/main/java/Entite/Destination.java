package Entite;

public class Destination {
    private int idDestination;
    private String pays;
    private String ville;
    private String description;

    public Destination() {}

    public Destination(int idDestination, String pays, String ville, String description) {
        this.idDestination = idDestination;
        this.pays = pays;
        this.ville = ville;
        this.description = description;
    }

    public int getIdDestination() { return idDestination; }
    public void setIdDestination(int idDestination) { this.idDestination = idDestination; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Destination{" +
                "idDestination=" + idDestination +
                ", pays='" + pays + '\'' +
                ", ville='" + ville + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

