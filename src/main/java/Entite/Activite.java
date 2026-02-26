package Entite;

public class Activite {
    private int    idActivite;
    private String nom;
    private String description;
    private double prix;
    private int    dureeEnHeure;
    private String horaire;
    private int    idTypeActivite;
    private String libelleType;     // chargé par JOIN, pas de colonne BD
    private int    idDestination;

    public Activite() {}

    public Activite(int idActivite, String nom, String description,
                    double prix, int dureeEnHeure, String horaire,
                    int idTypeActivite, String libelleType, int idDestination) {
        this.idActivite     = idActivite;
        this.nom            = nom;
        this.description    = description;
        this.prix           = prix;
        this.dureeEnHeure   = dureeEnHeure;
        this.horaire        = horaire;
        this.idTypeActivite = idTypeActivite;
        this.libelleType    = libelleType;
        this.idDestination  = idDestination;
    }

    public int    getIdActivite()                          { return idActivite; }
    public void   setIdActivite(int idActivite)            { this.idActivite = idActivite; }
    public String getNom()                                 { return nom; }
    public void   setNom(String nom)                       { this.nom = nom; }
    public String getDescription()                         { return description; }
    public void   setDescription(String description)       { this.description = description; }
    public double getPrix()                                { return prix; }
    public void   setPrix(double prix)                     { this.prix = prix; }
    public int    getDureeEnHeure()                        { return dureeEnHeure; }
    public void   setDureeEnHeure(int dureeEnHeure)        { this.dureeEnHeure = dureeEnHeure; }
    public String getHoraire()                             { return horaire; }
    public void   setHoraire(String horaire)               { this.horaire = horaire; }
    public int    getIdTypeActivite()                      { return idTypeActivite; }
    public void   setIdTypeActivite(int idTypeActivite)    { this.idTypeActivite = idTypeActivite; }
    public String getLibelleType()                         { return libelleType; }
    public void   setLibelleType(String libelleType)       { this.libelleType = libelleType; }
    public int    getIdDestination()                       { return idDestination; }
    public void   setIdDestination(int idDestination)      { this.idDestination = idDestination; }

    @Override
    public String toString() {
        return "Activite{id=" + idActivite + ", nom='" + nom + "', type='" + libelleType
                + "', prix=" + prix + ", duree=" + dureeEnHeure + "h, horaire='" + horaire + "'}";
    }
}