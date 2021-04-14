package Progettoreti.server;

import Progettoreti.callback.ServerNotifica;
import Progettoreti.callback.ServerNotificaImpl;
import Progettoreti.registrazione.Registrazione;
import Progettoreti.registrazione.RegistrazioneImpl;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MainClass {

    //MAIN DEL SERVER
    public static void main(String[] args) {

        try {
            // Creo l'istanza della classe che mi implementa il metodo Registrazione
            RegistrazioneImpl state = new RegistrazioneImpl();


            // Creo lo Stub
            Registrazione stub = (Registrazione) UnicastRemoteObject.exportObject(state, 0);
            LocateRegistry.createRegistry(30000);
            // Registry è un registor che contiene tutti i servizi che usano le RMI
            Registry reg = LocateRegistry.getRegistry(30000);
            //Reg cotiene molti metodo RMI in questo caso io vado ad associare un nuovo nome in questo caso "Progettoreti.registrazione" al mio stub
            reg.bind("Registrazione", stub);


            // creo l'istanza di Progettoreti.callback.ServerNotificaImpl
            ServerNotificaImpl serverCallBack = new ServerNotificaImpl();
            //creo lo stub
            ServerNotifica stubCallBack = (ServerNotifica) UnicastRemoteObject.exportObject(serverCallBack, 4576);
            LocateRegistry.createRegistry(40000);
            Registry regCallBack = LocateRegistry.getRegistry(40000);
            regCallBack.bind("Server", stubCallBack);

            ServerTCP server = new ServerTCP(serverCallBack);

            System.out.println("Serve is Ready");

            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
