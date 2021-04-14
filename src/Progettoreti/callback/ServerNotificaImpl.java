package Progettoreti.callback;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Vector;

public class ServerNotificaImpl extends RemoteObject implements ServerNotifica {

    ArrayList<String> listaUtenti;
    ArrayList<ClientNotifica> client;

    public ServerNotificaImpl() {
        listaUtenti = new ArrayList<>();
        client = new ArrayList<>();
    }

    // servizio per registrare e quindi aggiungere un utente al servizio CallBack
    public synchronized void registrazioneServizio(ClientNotifica notifica, String user){
        if (!listaUtenti.contains(user)) {
            listaUtenti.add(user);
            client.add(notifica);
        }
    }

    // servizio per deregistare e quindi rimuore un utente dal servizio CallBack
    public synchronized void deregistrazioneServizio(ClientNotifica notifica, String user){
        if (listaUtenti.contains(user)) {
            int indiceUtente = listaUtenti.indexOf(user);
            listaUtenti.remove(user);
            client.remove(indiceUtente);
        } else {
            System.out.println("Utente non presente");
        }
    }

    // metodo che riceve dal server la lista degli utenti con il loro stato
    public void update(Vector<String> usrs) throws RemoteException {

        finalUpdate(usrs);
    }

    // metodo per che prende la lista degli utenti con satto e la passa al metodo notificaUser
    private synchronized void finalUpdate(Vector<String> usrs) throws RemoteException {
        for (ClientNotifica c : client) {
            c.notificaUser(usrs);
        }

    }

}
