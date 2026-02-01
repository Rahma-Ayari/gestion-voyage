package Entite;

import java.time.LocalDateTime;

public class Notification {
    private int idNotification;
    private String message;
    private LocalDateTime dateNotification;
    private boolean lu;
    private String typeNotification;
    private int idReservation;


    public int getIdNotification() { return idNotification; }
    public void setIdNotification(int idNotification) { this.idNotification = idNotification; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDateNotification() { return dateNotification; }
    public void setDateNotification(LocalDateTime dateNotification) { this.dateNotification = dateNotification; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }

    public String getTypeNotification() { return typeNotification; }
    public void setTypeNotification(String typeNotification) { this.typeNotification = typeNotification; }

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }


    public void marquerCommeLue() { this.lu = true; }

    @Override
    public String toString() {
        return "Notification{" +
                "idNotification=" + idNotification +
                ", message='" + message + '\'' +
                ", dateNotification=" + dateNotification +
                ", lu=" + lu +
                ", typeNotification='" + typeNotification + '\'' +
                ", idReservation=" + idReservation +
                '}';
    }
}
