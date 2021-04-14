package Progettoreti.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

// questa classe contiene tutti i metodi d'appoggio che mi servono e che richiamo all'interno di Server TCP
public class DataBase {

    // student.json
    private ConcurrentMap<String, Utente> hash;
    // Progetti.json
    private CopyOnWriteArrayList<String> listaProgetti;


    public DataBase() {
        hash = new ConcurrentHashMap<>();
        listaProgetti = new CopyOnWriteArrayList<>();
    }

    public synchronized void ReadDatabase(String path) {

        // qua scrivo dal file nella struttura dati visto che passandola qua è vuota
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path));
            //converto il file a caso nella struttura nella quale voglio inserire i dati in questo caso ConcurrentHashMap
            Type type = new TypeToken<ConcurrentHashMap<String, Utente>>() {
            }.getType();
            hash = gson.fromJson(br, type);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void WriteDatabase(String path) {
        // scrivo dalla struttura dati nel file
        try {
            FileWriter writer = new FileWriter(path);
            Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
            gson1.toJson(hash, writer);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void ReadProjectList(String path) {
        // qua scrivo dal file nella struttura dati tutti i progetti che sono scritti sul file Progetti.json
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path));
            Type type = new TypeToken<CopyOnWriteArrayList<String>>() {
            }.getType();
            listaProgetti = gson.fromJson(br, type);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void WriteProjectList(String path) {
        // scrivo dalla struttura dati nel file
        try {
            FileWriter writer = new FileWriter(path);
            Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
            gson1.toJson(listaProgetti, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CopyOnWriteArrayList<String> getListaProgetti(String nick) {
        // restituisce la lista dei progetti di un utente

        // quindi prima popolo tale struttura con qeusta funzione
        ReadDatabase("Student.json");
        //ricavo l'utente medesimo
        Utente pippo = hash.get(nick);
        // e di tale utente ricavo la sua lista dei progetti
        return pippo.getProgetti();
    }

    public synchronized ConcurrentMap<String, Utente> getHash() {
        // funzione che mi ritorna la stuttura dati che ho in quetsa classe
        return this.hash;
    }

    public synchronized boolean aggiungiMembri(String nome, String progetto) {

        // aggiungo un utente come membro di un progetto

        // mi creo una lista che conterrà gli utenti relativi al progetto
        CopyOnWriteArrayList<String> utentiProgettoX;

        // mi creo un nuovo progetto, che avrà come nome quello passato come parametro alla funzione aggiungiMembri
        // mi creo una istanza di progetto per poterlo usare successivamente
        Progetti progetti = new Progetti(progetto);

        // qua vado a scrivere nella struttura dati i nomi degli utenti che gia fanno parte del progetto dato in input,
        // e sta cosa la faccio perchè se acceso da zero tali strutture sono vuote, quindi prendo il file .json e popolo tale struttura
        progetti.ReadUserList(progetto);

        // e una volta scritto nella struttura Utenti coloro che fanno gia parte al progetto, mi prendo tali utenti che sono affiliati a quel progetto,
        // quindi utentiProgettoX conterrà coloro che fanno gia parte al progetto che io ho inserito all'inizio

        // modificando utentiProgettoX aggiorno pure Utenti
        utentiProgettoX = progetti.getUtenti();

        boolean risultato;

        //inserisco tale utente all'interno della struttura dati
        risultato = utentiProgettoX.addIfAbsent(nome);


        //adesso aggiorno il file con gli utenti nuovi che fanno parte di quel progetto
        progetti.WriteUserList("Progetti/" + progetto);

        // scrivo nella struttura dati il contenuto del file Student.json aggiornato ad ora
        ReadDatabase("Student.json");


        // se risultato è true allora vuol dire che ho aggiunto l'utente e quindi devo aggiornare il suo profilo utente generale

        if (risultato) {

            // mi prendo l'utente grazie al nome
            Utente utente = hash.get(nome);

            // inserisco in listaProgetti la sua lista dei progetti attuale
            listaProgetti = utente.getProgetti();


            // e qua aggiungo a tale lista quello nuovo se gia non è presente
            listaProgetti.addIfAbsent(progetto);


            // adesso aggiorno lo status dell'utente andando a eliminare il vecchio utente con le info non aggiornate
            hash.remove(nome);
            // e ci aggiungo lo stesso utente con le info aggiornate
            hash.put(nome, utente);
            // e infine aggiorno il file Student.json che contiene le info degli utenti
            WriteDatabase("Student.json");
        }
        return risultato;
    }


    public CopyOnWriteArrayList<String> listUserMember(String progetto, String user) {
        // funzione che guarda se un utente è membro di un determinato progetto e restituisco tale lista

        // creo una lista di appoggio
        CopyOnWriteArrayList<String> utentiMembri;

        // creo l'istanza dei progetti
        Progetti project = new Progetti(progetto);

        // ora qua scrivo la lista degli utenti che sta dentro la struttura UTENTI la quale si trova nella classe "Progetti"
        // cosiché quando la vado a prendere con getUtenti() è inizalizzata
        project.ReadUserList(progetto);

        // ricavo tutti gli utenti presenti al progetto suddetto
        utentiMembri = project.getUtenti();

        // controllo se l'utente loggato al momento è membro di tale progetto
        if (utentiMembri.contains(user)) {
            return utentiMembri;
        } else {
            return null;
        }
    }

    public boolean seiMembro(String progetto, String user) {


        // controllo se un utente è membro di un progetto

        // creo una lista di appoggio
        CopyOnWriteArrayList<String> utentiMembri;

        // creo l'istanza dei progetti
        Progetti project = new Progetti(progetto);

        // ora qua scrivo la lista degli utenti dentro la struttura UTENTI che si trova nella classe "Progetti"
        // cosiché quando la vado a prendere con getUtenti() è inizalizzata
        project.ReadUserList(progetto);

        // ricavo tutti gli utenti presenti al progetto suddetto
        utentiMembri = project.getUtenti();

        // controllo se l'utente loggato al momento è membro di tale progetto
        return utentiMembri.contains(user);
    }

    public boolean registrato(String nome){

        // popolo la lista con gli utenti registrati
        ReadDatabase("Student.json");

        return hash.containsKey(nome);
    }

    public synchronized void deletedProject(String nomeprogetto) {

        // aggiorno la struttura dati hash con il contenuto aggiornato di Student.json
        ReadDatabase("Student.json");


        // scorro  la struttura hash e elimino il progetto da tutti gli utenti

        hash.forEach((k, v) -> v.getProgetti().remove(nomeprogetto));


        // aggiorno il file scrivendo il contenuto della struttura hash nel file Student.json, la quale contiene i progetti aggiornati
        WriteDatabase("Student.json");


        //adesso devo popolare la strtuttura dati che contiene i progetti
        ReadProjectList("Progetti.json");

        // qua elimino il progetto nella lista dei progetti Progetti.json
        listaProgetti.remove(nomeprogetto);

        // aggiorno il file Progetti.json con i progetti aggiornati ad ora
        WriteProjectList("Progetti.json");
    }

    public synchronized boolean AggiungiProgetto(String progetto) {
        if (listaProgetti == null) {
            listaProgetti = new CopyOnWriteArrayList<>();
        }

        return listaProgetti.addIfAbsent(progetto);
    }

    public Vector<String> getStatoUtente() {
        // recupero la lista degli stati degli utenti


        // creo un Vector di Stringhe, nel quale memorizzo l'utente e il suo stato
        // e per fare cio uso un foreachche mi scorre la struttura dove ci sono tutte le info degli utenti
        Vector<String> stati = new Vector<>();
        hash.forEach((k, v) -> stati.add(k + ":" + v.getStato()));
        return stati;
    }

    public Vector<String> getUtentiOnline() {
        Vector<String> online = new Vector<>();

        hash.forEach((k, v) ->
                {
                    // se lo stato è true allora entro nell'if
                    if (v.getStato()) {
                        // mi salvo il nome dell'utente  on line (k,v)->(nome,statoOnline), e lo aggiungo al Vector online
                        online.add(k + ":" + v.getStato());
                    }
                }
        );
        return online;
    }

}
