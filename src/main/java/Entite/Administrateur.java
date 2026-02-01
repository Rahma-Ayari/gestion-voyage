package Entite;

import java.util.Date;

public class Administrateur extends Utilisateur {

    public Administrateur() {
        super();
    }

    public Administrateur(int id, String email, String motDePasse, Date dateInscription) {
        super(id, email, motDePasse, dateInscription);
    }

    @Override
    public String toString() {
        return "Administrateur [ID=" + idUtilisateur + ", Email=" + email + "]";
    }
}