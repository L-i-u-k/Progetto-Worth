package Progettoreti.callback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.server.RemoteObject;
import java.util.Vector;


public class ClientNotificaImpl extends RemoteObject implements ClientNotifica {


    // vado a scrivere sul file "Utenti.json" la lista degli utenti col loro stato attuale

    @Override
    public void notificaUser(Vector<String> lista) {
        try {
            FileWriter writer = new FileWriter("Utenti.json");
            Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
            gson1.toJson(lista, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
