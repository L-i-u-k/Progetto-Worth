package Progettoreti.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GeneratoreIP {

    // indirizzi.json
    private ArrayList<Progetti> listaIndirizziIP;
    // struttura che contiene solo indirizzi IP
    private ArrayList<String> IP;

    public GeneratoreIP() {
        listaIndirizziIP = new ArrayList<>();
        IP = new ArrayList<>();
    }


    public synchronized void ReadIndirizziIP(String path) {
        // qua scrivo dal file nella struttura dati tutti gli indirizzi IP che sono scritti sul file indirizzi.json
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path));
            Type type = new TypeToken<ArrayList<Progetti>>() {
            }.getType();
            listaIndirizziIP = gson.fromJson(br, type);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void WriteIndirizziIP(String path) {
        // scrivo dalla struttura dati nel file
        try {
            FileWriter writer = new FileWriter(path);
            Gson gson1 = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
            gson1.toJson(listaIndirizziIP, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // creo randomicamente un indirizzo IP, lo metto static sync cosi se due progetti vengono creati nello steso momento non avranno lo stesso IP
    // perché accendono un Thread alla volta
    public static synchronized String GeneraIP() {
        return (ThreadLocalRandom.current().nextInt(224, 239 + 1) + "." + ThreadLocalRandom.current().nextInt(0, 256) + "." + ThreadLocalRandom.current().nextInt(0, 255 + 1) + "." + ThreadLocalRandom.current().nextInt(0, 255 + 1));
    }

    // aggiungo un indrizzo IP alla lista
    public synchronized void aggiungiIP(Progetti progetto) {
        if (listaIndirizziIP == null) {
            listaIndirizziIP = new ArrayList<>();
            IP = new ArrayList<>();
        }
        // qua genero un inidirizzo IP
        String indIP = GeneraIP();

        // qua scandisco la lista che contiene nome del progetto e indirizzo IP associato
        // prendo solo gli indirizzi IP e li inserisco nella struttura dati IP
        for (Progetti p : listaIndirizziIP) {
            IP.add(p.getIndirizzoIp());
        }


        // questo ciclo mi serve per far si che non ci siano indirizzi IP uguali, infatti ciclo finché l'indirizzo che genero gia esiste
        // se genero un indirizzi IP che non eiste esco dal while e lo vado ad assegnare
        while (IP.contains(indIP)) {
            indIP = GeneraIP();
        }

        // assegno il progetto
        listaIndirizziIP.add(progetto);
        // e assegno l'indirizzo IP al progetto
        progetto.setIndirizzoIp(indIP);
    }

    public ArrayList<Progetti> getListaIndirizziIP() {
        return listaIndirizziIP;
    }

    //funzione che mi rimuoive dal file Indrizzi.json il progetto e l'indirizzo IP associato
    public synchronized void rimuoviIP(String nomeprogetto) {

        Progetti project = new Progetti(nomeprogetto);


        project = listaIndirizziIP.remove(listaIndirizziIP.indexOf(project));
        IP.remove(project.getIndirizzoIp());

        if (listaIndirizziIP.isEmpty()) {
            System.out.println("ok");
        } else {
            System.out.println("non ok");
        }

    }
}
