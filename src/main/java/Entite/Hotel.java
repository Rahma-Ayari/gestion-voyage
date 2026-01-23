package Entite;


public class Hotel {
    private int idHotel;
    private String nom;
    private String ville;
    private String adresse;

    public Hotel() {}

    public Hotel(int idHotel, String nom, String ville, String adresse) {
        this.idHotel = idHotel;
        this.nom = nom;
        this.ville = ville;
        this.adresse = adresse;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
