package Entite;

public class Hotel {
    private int    idHotel;
    private String nom;
    private String ville;
    private String adresse;
    private int    stars;           // Nombre d'étoiles
    private int    capacite;        // Nombre de chambres
    private String typeChambre;     // Simple, Double, Suite...
    private double prixParNuit;
    private boolean disponibilite;
    private int    idDestination;   // FK vers la table destination
    private String  typeReservation;

    public Hotel() {}

    public Hotel(int idHotel, String nom, String ville, String adresse,
                 int stars, int capacite, String typeChambre,
                 double prixParNuit, boolean disponibilite, int idDestination) {
        this.idHotel       = idHotel;
        this.nom           = nom;
        this.ville         = ville;
        this.adresse       = adresse;
        this.stars         = stars;
        this.capacite      = capacite;
        this.typeChambre   = typeChambre;
        this.prixParNuit   = prixParNuit;
        this.disponibilite = disponibilite;
        this.idDestination = idDestination;
    }

    /** Constructeur sans idDestination (rétro-compatibilité) */
    public Hotel(int idHotel, String nom, String ville, String adresse,
                 int stars, int capacite, String typeChambre,
                 double prixParNuit, boolean disponibilite) {
        this(idHotel, nom, ville, adresse, stars, capacite,
                typeChambre, prixParNuit, disponibilite, 0);
    }

    public Hotel(int idHotel, String nom, String ville, String adresse, int stars, int capacite, String typeChambre, double prixParNuit, boolean disponibilite, int idDestination, String typeReservation) {
        this.idHotel = idHotel;
        this.nom = nom;
        this.ville = ville;
        this.adresse = adresse;
        this.stars = stars;
        this.capacite = capacite;
        this.typeChambre = typeChambre;
        this.prixParNuit = prixParNuit;
        this.disponibilite = disponibilite;
        this.idDestination = idDestination;
        this.typeReservation = typeReservation;
    }

    public String getTypeReservation() {
        return typeReservation;
    }

    public void setTypeReservation(String typeReservation) {
        this.typeReservation = typeReservation;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int     getIdHotel()                        { return idHotel; }
    public void    setIdHotel(int idHotel)             { this.idHotel = idHotel; }

    public String  getNom()                            { return nom; }
    public void    setNom(String nom)                  { this.nom = nom; }

    public String  getVille()                          { return ville; }
    public void    setVille(String ville)              { this.ville = ville; }

    public String  getAdresse()                        { return adresse; }
    public void    setAdresse(String adresse)          { this.adresse = adresse; }

    public int     getStars()                          { return stars; }
    public void    setStars(int stars)                 { this.stars = stars; }

    public int     getCapacite()                       { return capacite; }
    public void    setCapacite(int capacite)           { this.capacite = capacite; }

    public String  getTypeChambre()                    { return typeChambre; }
    public void    setTypeChambre(String typeChambre)  { this.typeChambre = typeChambre; }

    public double  getPrixParNuit()                    { return prixParNuit; }
    public void    setPrixParNuit(double prixParNuit)  { this.prixParNuit = prixParNuit; }

    public boolean isDisponibilite()                   { return disponibilite; }
    public void    setDisponibilite(boolean d)         { this.disponibilite = d; }

    public int     getIdDestination()                  { return idDestination; }
    public void    setIdDestination(int idDestination) { this.idDestination = idDestination; }

    @Override
    public String toString() {
        return "Hotel{" +
                "idHotel=" + idHotel +
                ", nom='" + nom + '\'' +
                ", ville='" + ville + '\'' +
                ", adresse='" + adresse + '\'' +
                ", stars=" + stars +
                ", capacite=" + capacite +
                ", typeChambre='" + typeChambre + '\'' +
                ", prixParNuit=" + prixParNuit +
                ", disponibilite=" + disponibilite +
                ", idDestination=" + idDestination +
                ", typeReservation='" + typeReservation + '\'' +
                '}';
    }
}