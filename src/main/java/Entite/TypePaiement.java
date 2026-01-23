package Entite;

public class TypePaiement {
    private int idTypePaiement;
    private String libelle;
    private String description;


    public int getIdTypePaiement() { return idTypePaiement; }
    public void setIdTypePaiement(int idTypePaiement) { this.idTypePaiement = idTypePaiement; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public String afficherInfo() {
        return libelle + " : " + description;
    }


    @Override
    public String toString() {
        return "TypePaiement{" +
                "idTypePaiement=" + idTypePaiement +
                ", libelle='" + libelle + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
