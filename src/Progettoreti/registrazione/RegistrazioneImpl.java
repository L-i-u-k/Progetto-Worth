package Progettoreti.registrazione;

import Progettoreti.server.Crittografia;
import Progettoreti.server.Utente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.rmi.server.RemoteServer;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RegistrazioneImpl extends RemoteServer implements Registrazione {

    public ConcurrentMap<String, Utente> DataBaseDati;

    public RegistrazioneImpl() {
        DataBaseDati = new ConcurrentHashMap<>();
    }

    public void registrazione(String nome, String password) throws NoSuchAlgorithmException {

        if (nome.equals("") || password.equals("")) {
            System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
            return;
        } else if (nome.contains(" ") || password.contains(" ")) {
            System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
            return;
        }


        String passCrpt = Crittografia.critto(password);


        //creo l'utente
        Utente User = new Utente(passCrpt);


        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("Student.json"));
            Type type = new TypeToken<ConcurrentHashMap<String, Utente>>() {
            }.getType();
            DataBaseDati = gson.fromJson(br, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //inserisci l'utente
        try {

            if (DataBaseDati == null) {
                DataBaseDati = new ConcurrentHashMap<>();
            }

            // Controllo se nel database il tizio registrato ce gia
            if (DataBaseDati.putIfAbsent(nome, User) != null) {
                System.out.println("\u001B[31m" + "Lo UserName inserito è gia esistente" + "\u001B[0m");
            }

            //Inserisco il contenuto della hash map nel file json
            FileWriter writer = new FileWriter("Student.json");
            Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
            gson1.toJson(DataBaseDati, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

