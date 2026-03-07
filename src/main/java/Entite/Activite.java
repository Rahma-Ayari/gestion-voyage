package Entite;

public class Activite {
    private int idActivite;
    private String nom;
    private String description;
    private double prix;
    private int dureeEnHeure;
    private String categorie;
    private String horaire;
    private int idTypeActivite;
    private String libelleType;        // Chargé via JOIN depuis la BD
    private TypeActivite typeAct;       // Objet TypeActivite pour ComboBox
    private int idDestination;

    // ────── CONSTRUCTEURS ──────

    /**
     * Constructeur par défaut
     */
    public Activite() {}

    /**
     * Constructeur complet utilisé par ServiceActivite.mapActivite()
     * ⚠️ IMPORTANT : C'est le constructeur utilisé quand les données viennent de la BD
     */
    public Activite(int idActivite, String nom, String description,
                    double prix, int dureeEnHeure, String horaire,
                    int idTypeActivite, String libelleType, int idDestination) {
        this.idActivite = idActivite;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.dureeEnHeure = dureeEnHeure;
        this.horaire = horaire;
        this.idTypeActivite = idTypeActivite;
        this.libelleType = libelleType;
        this.idDestination = idDestination;
        // Créer l'objet TypeActivite automatiquement
        if (libelleType != null) {
            this.typeAct = new TypeActivite(idTypeActivite, libelleType);
        }
    }

    /**
     * Constructeur avec TypeActivite objet (pour le contrôleur)
     */
    public Activite(int idActivite, String nom, String description,
                    double prix, int dureeEnHeure, String categorie, String horaire,
                    TypeActivite typeAct, int idDestination) {
        this.idActivite = idActivite;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.dureeEnHeure = dureeEnHeure;
        this.categorie = categorie;
        this.horaire = horaire;
        this.typeAct = typeAct;
        if (typeAct != null) {
            this.idTypeActivite = typeAct.getIdType();
            this.libelleType = typeAct.getLibelle();
        }
        this.idDestination = idDestination;
    }

    /**
     * Constructeur minimal (compatibilité anciennes versions)
     */
    public Activite(int idActivite, String nom, String description,
                    double prix, int dureeEnHeure,
                    String categorie, String horaire) {
        this.idActivite = idActivite;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.dureeEnHeure = dureeEnHeure;
        this.categorie = categorie;
        this.horaire = horaire;
    }

    // ────── GETTERS ET SETTERS ──────

    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getDureeEnHeure() {
        return dureeEnHeure;
    }

    public void setDureeEnHeure(int dureeEnHeure) {
        this.dureeEnHeure = dureeEnHeure;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getHoraire() {
        return horaire;
    }

    public void setHoraire(String horaire) {
        this.horaire = horaire;
    }

    public int getIdTypeActivite() {
        return idTypeActivite;
    }

    public void setIdTypeActivite(int idTypeActivite) {
        this.idTypeActivite = idTypeActivite;
    }

    public String getLibelleType() {
        return libelleType;
    }

    public void setLibelleType(String libelleType) {
        this.libelleType = libelleType;
    }

    public TypeActivite getTypeAct() {
        return typeAct;
    }

    /**
     * Setter pour TypeActivite avec synchronisation automatique
     */
    public void setTypeAct(TypeActivite typeAct) {
        this.typeAct = typeAct;
        if (typeAct != null) {
            this.idTypeActivite = typeAct.getIdType();
            this.libelleType = typeAct.getLibelle();
        }
    }

    public int getIdDestination() {
        return idDestination;
    }

    public void setIdDestination(int idDestination) {
        this.idDestination = idDestination;
    }

    // ────── TO_STRING ──────

    @Override
    public String toString() {
        return "Activite{" +
                "idActivite=" + idActivite +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", dureeEnHeure=" + dureeEnHeure +
                ", categorie='" + categorie + '\'' +
                ", horaire='" + horaire + '\'' +
                ", type='" + (typeAct != null ? typeAct.getLibelle() : libelleType) + '\'' +
                ", destination=" + idDestination +
                '}';
    }
}