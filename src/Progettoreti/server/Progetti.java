package Progettoreti.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Progetti {
    @Expose
    final String nomeProgetto;
    @Expose
    private String indirizzoIp;
    private CopyOnWriteArrayList<String> utenti; // Utenti relativi al progetto
    private CopyOnWriteArrayList<Card> listaDelleCoseDaFare;
    private CopyOnWriteArrayList<Card> inSviluppo;
    private CopyOnWriteArrayList<Card> daRivedere;
    private CopyOnWriteArrayList<Card> conclusi;


    public Progetti(String nome) {
        this.nomeProgetto = nome;
        this.utenti = new CopyOnWriteArrayList<>();
        this.listaDelleCoseDaFare = new CopyOnWriteArrayList<>();
        this.inSviluppo = new CopyOnWriteArrayList<>();
        this.daRivedere = new CopyOnWriteArrayList<>();
        this.conclusi = new CopyOnWriteArrayList<>();
    }

    public synchronized boolean AggiungiUtente(String user) {
        return this.utenti.addIfAbsent(user);
    }

    public synchronized String CreaDirectory(String NomeProgetto) {
        try {
            Path path = Paths.get("Progetti/" + NomeProgetto);
            Files.createDirectories(path);
            return path.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized boolean CreaFileProgetto(String nome) {

        try {
            File CoseDaFare = new File(nome + "/CoseDaFare.json");
            File Sviluppo = new File(nome + "/InSviluppo.json");
            File Rivedere = new File(nome + "/DaRivedere.json");
            File Conclusi = new File(nome + "/Conclusi.json");
            File Utenti = new File(nome + "/Utenti.json");
            return (CoseDaFare.createNewFile() && Utenti.createNewFile() && Sviluppo.createNewFile() && Rivedere.createNewFile() && Conclusi.createNewFile());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean WriteUserList(String nome) {
        // funzione che mi scrive gli utenti di un progetto sul file medesimo
        try {
            FileWriter writer = new FileWriter(nome + "/Utenti.json");
            Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
            gson1.toJson(utenti, writer);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized void ReadUserList(String path) {
        // qua scrivo dal file tutti gli utenti che fanno parte a questo progetto nella struttura dati visto che passandola qua è vuota
        Gson gson = new Gson();

        try (BufferedReader br = new BufferedReader(new FileReader("Progetti/" + path + "/Utenti.json"))) {
            //converto il file a caso nella struttura nella quale voglio inserire i dati in questo caso ConcurrentHashMap
            Type type = new TypeToken<CopyOnWriteArrayList<String>>() {
            }.getType();
            utenti = gson.fromJson(br, type);
        } catch (IOException e) {
        }
    }

    public void setIndirizzoIp(String indirizzoIp) {
        this.indirizzoIp = indirizzoIp;
    }

    public String getIndirizzoIp() {
        return indirizzoIp;
    }

    public String getNomeProgetto() {
        return nomeProgetto;
    }

    // funzione che mi restituisce tutti gli utenti che fanno parte di un progetto
    public CopyOnWriteArrayList<String> getUtenti() {
        return this.utenti;
    }

    public synchronized void WriteToDoList(String path) {
        // questa mi scrive dalla struttura dati nel file
        try {
            FileWriter writer = new FileWriter(path + "/CoseDaFare.json");
            Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
            gson1.toJson(listaDelleCoseDaFare, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //funzione che mi popola tutte le strutture dati con gli elementi rispettivi a ogni file.json
    public synchronized void readAllList(String path) {
        Gson gson = new Gson();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("Progetti/" + path + "/CoseDaFare.json"));
            //converto il file a caso nella struttura nella quale voglio inserire i dati in questo caso CopyOnWriteArrayList
            Type type = new TypeToken<CopyOnWriteArrayList<Card>>() {
            }.getType();
            listaDelleCoseDaFare = gson.fromJson(br, type);
            br.close();

            br = new BufferedReader(new FileReader("Progetti/" + path + "/InSviluppo.json"));
            inSviluppo = gson.fromJson(br, type);
            br.close();

            br = new BufferedReader(new FileReader("Progetti/" + path + "/DaRivedere.json"));
            daRivedere = gson.fromJson(br, type);
            br.close();

            br = new BufferedReader(new FileReader("Progetti/" + path + "/Conclusi.json"));
            conclusi = gson.fromJson(br, type);
            br.close();
        } catch (IOException e) {
        }
    }

    // funzione che mi restituisce l'array delle Card
    public ArrayList<Card> getCard() {
        ArrayList<Card> allCard = new ArrayList<>();

        // controllo se ogni lista non è vuota allora prendo le Cadr in quella lista e le inserisco
        // tutte insieme in allCard che poi restituisco

        if (listaDelleCoseDaFare != null) {
            allCard.addAll(listaDelleCoseDaFare);
        }
        if (inSviluppo != null) {
            allCard.addAll(inSviluppo);
        }
        if (daRivedere != null) {
            allCard.addAll(daRivedere);
        }
        if (conclusi != null) {
            allCard.addAll(conclusi);
        }
        return allCard;
    }


    public boolean esisteCard(String nomeCard) {
        Card card = new Card(nomeCard);

        ArrayList<Card> listaCard;

        listaCard = getCard();


        if (listaCard.contains(card)) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean addToDoList(Card card) {
        if (listaDelleCoseDaFare == null) {
            // se la todolist è vuota la inizializziamo
            this.listaDelleCoseDaFare = new CopyOnWriteArrayList<>();
        }
        return this.listaDelleCoseDaFare.addIfAbsent(card);
    }

    public String returnCard(String nomeCard) {
        // ricavo le info della Card
        Card C = new Card(nomeCard);


        ArrayList<Card> listaCard;

        listaCard = getCard();

        // guardo se la carta è contenuta nella lista delle Card
        if (listaCard.contains(C)) {

            // dall'indice ricavo la Card che mi interessa
            C = listaCard.get(listaCard.indexOf(C));
        }
        // e di tale Card mi ricavo le sue info
        return C.getInfo();
    }

    public boolean projectDone() {
        // controllo che i progetti siano tutti all'interno dello stato Done
        boolean valore;


        // se le 3 liste sono vuote questo vuol dire che:
        // 1) Conclusi è vuoto oppure
        // 2) Conclusi è piena, e in entrambi i casi posso cancellarlo
        // 3) altrimenti ce almeno una Card in quelle tre liste e quindi non posso cancellarlo

        if (inSviluppo == null && daRivedere == null && listaDelleCoseDaFare == null) {
            valore = true;
        } else {
            valore = false;
        }

        return valore;
    }

    public void removeDirecotry(String pathh) throws IOException {
        Path rootPath = Paths.get("Progetti/" + pathh);
        final List<Path> pathsToDelete = Files.walk(rootPath).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        for (Path path : pathsToDelete) {
            Files.deleteIfExists(path);
        }
    }


    // questa funzione mi scrive tutto cio che ce all'interno delle strutture dati in ogni file .json
    public synchronized void writeAllList(String path) {
        // questa mi scrive dal file nella struttura dati
        try {

            FileWriter writer = new FileWriter(path + "/CoseDaFare.json");
            Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
            gson1.toJson(listaDelleCoseDaFare, writer);
            writer.flush();
            writer.close();

            writer = new FileWriter(path + "/Conclusi.json");
            gson1.toJson(conclusi, writer);
            writer.flush();
            writer.close();

            writer = new FileWriter(path + "/InSviluppo.json");
            gson1.toJson(inSviluppo, writer);
            writer.flush();
            writer.close();

            writer = new FileWriter(path + "/DaRivedere.json");
            gson1.toJson(daRivedere, writer);
            writer.flush();
            writer.close();


        } catch (IOException e) {
        }

    }

    public synchronized String moveCard(String nomecard, String nomeprogetto, String partenza, String destinazione) {

        // metodo che mi permette di spostare una Card da una lista all'altra

        String valore = "";

        Card c = new Card(nomecard);

        if (partenza.equals("todolist") && !destinazione.equals("inprogress")) {
            return "\u001B[31m" + "Spostamento non permesso, unico spostamento permesso : todolist -> inprogress" + "\u001B[0m";
        }

        if (partenza.equals("inprogress") && !(destinazione.equals("done") || destinazione.equals("reseived"))) {
            return "\u001B[31m" + "Spostamento non permesso, unici spostamenti permessi: inprogres-> done oppure inprogres-> reseived" + "\u001B[0m";
        }

        if (partenza.equals("reseived") && !(destinazione.equals("done") || destinazione.equals("inprogress"))) {
            return "\u001B[31m" + "Spostamento non permesso, unici spostamenti permessi: reseived->done oppure reseived->inprogres" + "\u001B[0m";
        }

        if (partenza.equals("done")) {
            return "\u001B[31m" + "Spostamento non permesso, da done non si va da nessuna parte " + "\u001B[0m";
        }


        // in base alla stringa di partenza, popolo la lista giusta
        try {
            switch (partenza) {
                case "todolist" -> c = listaDelleCoseDaFare.remove(listaDelleCoseDaFare.indexOf(c));
                case "inprogress" -> c = inSviluppo.remove(inSviluppo.indexOf(c));
                case "reseived" -> c = daRivedere.remove(daRivedere.indexOf(c));
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            //vado qua se la indexOf tira una eccezione
            c = null;
        }


        if (c != null) {

            // se non sono vuote vuol dire che qualche Card al loro interno ce,
            // però ce da vedere se la lista di partenza contiene la Card specifica


            switch (destinazione) {

                case "todolist" -> {
                    if (listaDelleCoseDaFare == null) {
                        listaDelleCoseDaFare = new CopyOnWriteArrayList<>();
                    }
                    listaDelleCoseDaFare.add(c);
                    valore = "\u001B[32m" + "La Card è stata aggiunta alla ToDo List" + "\u001B[0m";
                }
                case "inprogress" -> {
                    if (inSviluppo == null) {
                        inSviluppo = new CopyOnWriteArrayList<>();
                    }
                    inSviluppo.add(c);
                    valore = "\u001B[32m" + "La Card è stata aggiunta alla InProgress List" + "\u001B[0m";
                }
                case "reseived" -> {
                    if (daRivedere == null) {
                        daRivedere = new CopyOnWriteArrayList<>();
                    }
                    daRivedere.add(c);
                    valore = "\u001B[32m" + "La Card è stata aggiunta alla Reseived List" + "\u001B[0m";
                }
                case "done" -> {
                    if (conclusi == null) {
                        conclusi = new CopyOnWriteArrayList<>();
                    }
                    conclusi.add(c);
                    valore = "\u001B[32m" + "La Card è stata aggiunta alla Done List" + "\u001B[0m";
                }
            }
            // setto tutte i file vuoti a null, cosi posso eliminarle nella funzione deletedProject

            if (listaDelleCoseDaFare == null || listaDelleCoseDaFare.isEmpty()) {
                listaDelleCoseDaFare = null;
            }

            if (inSviluppo == null || inSviluppo.isEmpty()) {
                inSviluppo = null;
            }

            if (daRivedere == null || daRivedere.isEmpty()) {
                daRivedere = null;
            }

            if (conclusi == null || conclusi.isEmpty()) {
                conclusi = null;
            }


            Card card = new Card(nomecard);

            if (destinazione.equals("todolist")) {
                card = listaDelleCoseDaFare.get(listaDelleCoseDaFare.indexOf(card));
                listaDelleCoseDaFare.remove(card);
                card.history.add(destinazione);
                listaDelleCoseDaFare.add(card);
            }
            if (destinazione.equals("inprogress")) {
                card = inSviluppo.get(inSviluppo.indexOf(card));
                inSviluppo.remove(card);
                card.history.add(destinazione);
                inSviluppo.add(card);
            }
            if (destinazione.equals("reseived")) {
                card = daRivedere.get(daRivedere.indexOf(card));
                daRivedere.remove(card);
                card.history.add(destinazione);
                daRivedere.add(card);
            }
            if (destinazione.equals("done")) {
                card = conclusi.get(conclusi.indexOf(card));
                conclusi.remove(card);
                card.history.add(destinazione);
                conclusi.add(card);
            }


            writeAllList("Progetti/" + nomeprogetto);


        } else {
            valore = "\u001B[31m" + "La Card non è contenuta nella lista di partenza" + "\u001B[0m";
        }
        return valore;
    }

    public synchronized ArrayList<String> getHistory(String nomecard) {

        // prima creo una istanza alla classe Card
        Card card = new Card(nomecard);

        // metto qua dentro tutte le Card presenti in tutte le liste
        ArrayList<Card> allCard = getCard();
        ArrayList<String> history = new ArrayList<>();


        // controllo se la Card specificata esiste non servirebbe perché lo faccio già nel server
        // ma non si è mai troppo sicuri

        if (allCard.contains(card)) {
            // se la lista ddi tutte le Card contiene le nostra allora

            // ricavo l'indice della Card
            card = allCard.get(allCard.indexOf(card));

            // e di tale Card ricavo la storia
            history = card.getHistory();
        }

        return history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Progetti progetti = (Progetti) o;
        return nomeProgetto.equals(progetti.nomeProgetto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomeProgetto);
    }
}
