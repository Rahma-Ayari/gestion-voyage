package Entite;

public class VoyageHotel {
    private int idVoyage;
    private int idHotel;

    public VoyageHotel() {}

    public VoyageHotel(int idVoyage, int idHotel) {
        this.idVoyage = idVoyage;
        this.idHotel = idHotel;
    }

    public int getIdVoyage() {
        return idVoyage;
    }

    public void setIdVoyage(int idVoyage) {
        this.idVoyage = idVoyage;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

}
