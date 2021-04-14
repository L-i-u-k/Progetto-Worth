package Progettoreti.server;

import java.util.concurrent.CopyOnWriteArrayList;

public class Utente {
    private String password;
    private boolean stato;
    private CopyOnWriteArrayList<String> Progetti;

    public Utente() {
    }

    public Utente(String password) {
        this.password = password;
        this.stato = false;
        Progetti = new CopyOnWriteArrayList<>();
    }

    public String getPassword() {
        return password;
    }

    public boolean getStato() {
        return stato;
    }

    public void setStato(boolean stato) {
        this.stato = stato;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CopyOnWriteArrayList<String> getProgetti() {
        return Progetti;
    }

    public void setProgetti(CopyOnWriteArrayList<String> progetti) {
        this.Progetti = progetti;
    }
}
