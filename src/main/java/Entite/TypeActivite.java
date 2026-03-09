package Entite;

public class TypeActivite {
    private int idType;
    private String libelle;

    public TypeActivite() {}

    public TypeActivite(int idType, String libelle) {
        this.idType = idType;
        this.libelle = libelle;
    }

    public int getIdType() {
        return idType;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }

}
