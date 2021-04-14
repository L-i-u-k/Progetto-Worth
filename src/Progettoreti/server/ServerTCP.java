package Progettoreti.server;

import Progettoreti.client.UDPServer;
import Progettoreti.callback.ServerNotificaImpl;
import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.*;

public class ServerTCP {
    // INSTAURAZIONE CONNESIONE TCP SONO IL SERVER
    ServerSocket serverSocket;

    BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(25);

    ThreadPoolExecutor threadpool = new ThreadPoolExecutor(20, 20, 9999, TimeUnit.HOURS, queue);
    public static ServerNotificaImpl serverCallBack;


    public ServerTCP(ServerNotificaImpl serverCallBack) {
        ServerTCP.serverCallBack = serverCallBack;
    }


    private static class EchoClientServer implements Runnable {

        final Utente utente;
        private String nickNameUser;
        final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        String inputLine;
        private boolean loggato;
        public ConcurrentMap<String, Utente> DataBaseLL;// conterrra coloro che hanno fatto il login
        public CopyOnWriteArrayList<String> listaUtentiAttuale;// lista di appoggio


        public EchoClientServer(Socket socket) {
            utente = new Utente();
            this.clientSocket = socket;
            DataBaseLL = new ConcurrentHashMap<>();
            loggato = false;
        }

        public void run() {

            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataBase database = new DataBase();

                while (true) {

                    inputLine = in.readLine();

                    String[] pippo = inputLine.split(" ", 4);

                    switch (pippo[0]) {
                        case "Exit":
                            if (!loggato) {

                                out.println(" Nice ✔ ");
                                Thread.sleep(2000);

                                in.close();
                                out.close();
                                clientSocket.close();
                                return;
                            }
                            break;

                        case "Login":


                            // questo if serve per vedere se quando mi collego da un'altro pc sono collegato gia oppure no
                            if (!loggato) {
                                // assegno alla variabile nickNameUser il nome dell'utente che farà il login
                                nickNameUser = pippo[1];

                                listaUtentiAttuale = new CopyOnWriteArrayList<>();


                                // aggiorno le strutture dati appena inizio l'operazione di login
                                database.ReadDatabase("Student.json");

                                // qua popolo il database locale con il metodo che sta nella classe DataBase
                                DataBaseLL = database.getHash();
                                // guardo se non è vuoto
                                if (DataBaseLL != null && !DataBaseLL.isEmpty()) {

                                    // Controllo se ce l'username
                                    if (DataBaseLL.containsKey(pippo[1])) {

                                        // Se l'utente è registrato, prelevo il suo user, cosicchè
                                        // possa settargli le info in modo giusto

                                        Utente user = DataBaseLL.get(pippo[1]);

                                        // qua adesso cripto con l'hash la password che mi arriva per il login
                                        // e la controllo con quella che è presente nel database
                                        // se le due codifiche in hash solo uguali , allora la passowrd è quella
                                        String crptPsw = Crittografia.critto(pippo[2]);


                                        // controllo se la password è uguale
                                        if (user.getPassword().equals(crptPsw)) {
                                            utente.setPassword(pippo[2]);
                                            // true = già loggato
                                            // false = non loggato
                                            if (user.getStato()) {
                                                out.println("\u001B[31m" + "Sei gia loggato" + "\u001B[0m");
                                            } else {
                                                this.loggato = true;

                                                // Cambio lo stato dell'utente medesimo che è attivo nel thread quando logga
                                                // da false va a true

                                                // visto che l'utente ha ancora lo stato a false, lo setto a true per l'utente corrente
                                                DataBaseLL.get(pippo[1]).setStato(true);

                                                // e dopo aver aggiornato lo stato dell'utente aggiorno il file, cosichè il file conterrà
                                                // pure lo satto dell'utente aggiornato
                                                database.WriteDatabase("Student.json");
                                                // e qua ricavo la lista degli utenti con stato per il servizio RMI
                                                serverCallBack.update(database.getStatoUtente());
                                                out.println("\u001B[32m" + "Login avvenuto con successo" + "\u001B[0m");
                                            }
                                        } else {
                                            out.println("\u001B[31m" + "Password non valida" + "\u001B[0m");
                                        }
                                    } else {
                                        out.println("\u001B[31m" + "Non esiste tale nickname" + "\u001B[0m");
                                    }
                                } else {
                                    out.println("\u001B[31m" + "Devi registrarti prima di accedere" + "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Sei gia loggato con un utente su questo terminale" + "\u001B[0m");
                            }

                            break;
                        case "Logout":
                            if (loggato) {

                                if (!pippo[1].equals(nickNameUser)) {
                                    out.println("\u001B[32m" + "Non sei registrato" + "\u001B[0m");
                                    continue;
                                } else {

                                    // qua inserisco all'interno del data base il contenuto del file json
                                    database.ReadDatabase("Student.json");

                                    // qua popolo il database locale con il metodo che sta nella classe DataBase
                                    DataBaseLL = database.getHash();

                                    // guardo se è null oppure vuoto
                                    if (DataBaseLL != null && !DataBaseLL.isEmpty()) {

                                        // guardo se contiene il nome dell'utente
                                        if (DataBaseLL.containsKey(pippo[1])) {

                                            // setto loggato a false
                                            loggato = false;

                                            // e setto lo stato dell'utente a false
                                            DataBaseLL.get(pippo[1]).setStato(false);

                                            // aggiorno il file json col nuovo stato dell'utente
                                            database.WriteDatabase("Student.json");

                                            // servizio RMI CallBack
                                            serverCallBack.update(database.getStatoUtente());

                                            out.println("\u001B[32m" + "Logout avvenuto con successo" + "\u001B[0m");

                                        } else {
                                            out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                        }
                                    } else {
                                        out.println("\u001B[31m" + "Database vuoto" + "\u001B[0m");
                                    }
                                }

                            } else {
                                out.println("Devi registrati prima stronzo" + "\u001B[0m");
                            }
                            break;
                        case "Progetto":
                            // il progetto lo puoi creare solo se sei loggato
                            if (loggato) {

                                // scrivo il contenuto del file Student.json dentro alla struttura dati
                                database.ReadDatabase("Student.json");
                                // qua popolo il database locale con il metodo che sta nella classe DataBase
                                DataBaseLL = database.getHash();


                                // qua devo aggiornare la struttura ListaUtenteAttuale di nuovo perche se sloggo e riloggo
                                // essa dovrà contenere sia i progetti di prima che quelli attuali

                                // con queste due righe di codice faccio si che, quando inizialmente ho inserito nella lista dei progetti
                                // di un utente n progetti, e successivamente chiudo il programma, appena lo riattivo andrei ad sovrascrivere
                                // i progetti gia in lista con quelli che un utente crea nuovi.
                                // In questo caso prima di aggiugnere altri progetti, prima mi salvo nella struttura ListaUtenteAttuale i progetti che
                                // gia aveva,e tali progetti li prendo dal file, dopo accedo alla struttura  tramite la getProgetti,
                                // cosicché quelli nuovi si sommano a quelli gia in lista.
                                Utente utente = DataBaseLL.get(nickNameUser);

                                listaUtentiAttuale = utente.getProgetti();


                                // scrivo nella struttura dati il file, perché quando riavvio la struttura sarà vuota
                                database.ReadProjectList("Progetti.json");

                                // aggiungi il progetto alla struttura se non ce
                                if (database.AggiungiProgetto(pippo[1])) {
                                    listaUtentiAttuale.addIfAbsent(pippo[1]);


                                    // passo la lista con i progetti dell'utente medesimo ovvero alla classe utente che
                                    // sarebbe l'utente in questo momento attivo nel Thread

                                    DataBaseLL.get(nickNameUser).setProgetti(listaUtentiAttuale);

                                    // aggiorno il file
                                    database.WriteDatabase("Student.json");

                                    // scrivo nel file progetti.json il progetto appena creato
                                    database.WriteProjectList("Progetti.json");

                                    // istanza della classe che genra l'IP
                                    GeneratoreIP generatoreIP = new GeneratoreIP();

                                    // scrivo prima nella struttura dati gli IP
                                    generatoreIP.ReadIndirizziIP("Indirizzi.json");

                                    Progetti project = new Progetti(pippo[1]);

                                    // aggiungo l'IP al progetto
                                    generatoreIP.aggiungiIP(project);

                                    //Devo aggiungere il nome del progetoto e il suo ip alla lista
                                    generatoreIP.WriteIndirizziIP("Indirizzi.json");


                                    // aggiungo l'utente alla lista del progetto suddetto
                                    project.AggiungiUtente(nickNameUser);


                                    // questa è tutta la aprte relativa alla creazione del progetto
                                    //ovvero alla directory che conterrà i progetti
                                    String dir = project.CreaDirectory(pippo[1]);
                                    if (dir != null) {
                                        if (project.CreaFileProgetto(dir)) {
                                            database.WriteProjectList("Progetti.json");
                                            // aggiungo l'utente nella lista
                                            if (project.WriteUserList(dir)) {
                                                utente.setStato(true);

                                                out.println("\u001B[32m" + "Creazione fatta" + "\u001B[0m");
                                            } else {
                                                out.println("\u001B[31m" + "Creazione fallita" + "\u001B[0m");
                                            }
                                        } else {
                                            out.println("\u001B[31m" + "Errore di scrittura su file" + "\u001B[0m");
                                        }
                                    } else {
                                        out.println("\u001B[31m" + "Errore nella creazione del file" + "\u001B[0m");
                                    }
                                } else {
                                    out.println("\u001B[31m" + "Progetto gia esistente" + "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Non sei loggato e quindi niente progetto" + "\u001B[0m");
                            }
                            break;

                        case "ListaProgetti":
                            // se sei loogato allora okhei, restituisce la lista dei progetti
                            if (loggato) {

                                // struttura di appoggio
                                CopyOnWriteArrayList<String> progetti;
                                // la popolo con la lista dei progetti di un utente
                                progetti = database.getListaProgetti(nickNameUser);

                                // procedimento per passare la lista degli utenti dal Server al Client con Gson
                                Gson gson = new Gson();
                                // mi crea la stringa json
                                String lugah = gson.toJson(progetti, progetti.getClass());
                                out.println(lugah);

                            } else {
                                out.println("\u001B[31m" + "Non sei loggato quindi niente lista pupazzo" + "\u001B[0m");
                            }
                            break;

                        case "Aggiungi":

                            if (loggato) {
                                // prima di tutto guardo se l'utente che voglio aggiungere è registrato

                                if(database.registrato(pippo[1])){

                                    // scrivo il contenuto del file Student.json dentro alla struttura dati
                                    database.ReadDatabase("Student.json");
                                    // qua popolo il database locale con il metodo che sta nella classe DataBase
                                    DataBaseLL = database.getHash();


                                    // controllo se l'utente è registrato al servizio
                                    if (database.seiMembro(pippo[2], nickNameUser)) {

                                        //entro qua se l'utente è registrato


                                        if (database.aggiungiMembri(pippo[1], pippo[2])) {
                                            // utente aggiunto

                                            // aggiorno la struttura dati
                                            database.ReadProjectList("Progetti.json");

                                            out.println("\u001B[32m" + "Utente aggiunto al progetto" + "\u001B[0m");


                                        } else {
                                            out.println("\u001B[31m" + "Utente già presente nella lista di tale progetto" + "\u001B[0m");
                                        }
                                    } else {
                                        out.println("\u001B[31m" + "L'utente o il progetto sono inesistenti,ricontrolla!" + "\u001B[0m");
                                    }
                                }else{
                                    out.println("\u001B[31m" +"L'utente che vuoi inserire non è registrato"+ "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Non sei loggato quindi non puoi aggiungere nessuno" + "\u001B[0m");
                            }
                            break;
                        case "showMembers":
                            if (loggato) {

                                // scrivo il contenuto del file Student.json dentro alla struttura dati
                                database.ReadDatabase("Student.json");
                                // qua popolo il database locale con il metodo che sta nella classe DataBase
                                DataBaseLL = database.getHash();

                                // recupero la lista
                                CopyOnWriteArrayList<String> listaUtentiMembri;

                                // ricavo la lista degli utenti mebri al suddetto progetto
                                listaUtentiMembri = database.listUserMember(pippo[1], nickNameUser);

                                if (listaUtentiMembri != null) {
                                    // se la lista non è vuota , uso Gson per protare la lista dal Server al Client
                                    Gson gson = new Gson();
                                    // mi crea la stringa json
                                    String lugah = gson.toJson(listaUtentiMembri, listaUtentiMembri.getClass());
                                    out.println(lugah);
                                } else {
                                    out.println("\u001B[31m" + "Non fai parte di questo progetto" + "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Non sei loggato quindi non puoi recuperare nuessuna lista" + "\u001B[0m");
                            }
                            break;
                        case "addCard":
                            if (loggato) {

                                // scrivo il contenuto del file Student.json dentro alla struttura dati
                                database.ReadDatabase("Student.json");
                                // qua popolo il database locale con il metodo che sta nella classe DataBase
                                DataBaseLL = database.getHash();


                                // prima di tutto si controlla se la card esiste gia

                                //prima di tutto guardo se colui che sta provadno ad aggiungere una Card al progetto è membro di tale progetto
                                if (database.seiMembro(pippo[2], nickNameUser)) {
                                    // se sono qua vuol dire che sono membro

                                    // adesso controllo se la card esiste gia

                                    Progetti project = new Progetti(pippo[1]);


                                    // ricavo la lista delle card
                                    project.readAllList(pippo[2]);

                                    // ora guardo se la carta esiste
                                    if (!project.esisteCard(pippo[1])) {

                                        // se entro qua la Card non esiste e quindi la creo

                                        Card card = new Card(pippo[1]);

                                        // setto la descrizione della carta
                                        card.setDescrizioneCard(pippo[3]);

                                        // una volta creata la card devo aggiungerla alla TodoList

                                        // qua controllo se la card è gia in todolist
                                        if (project.addToDoList(card)) {
                                            // se entro qua la Card è stata aggiunta e quindi aggiorno tutti i file

                                            // riscrivo tutto all'interno del file cosi da aggiornare  le Card
                                            project.WriteToDoList("Progetti/" + pippo[2]);

                                            database.ReadProjectList("Progetti.json");

                                            out.println("\u001B[32m" + "La carta è stata aggiunta" + "\u001B[0m");

                                        } else {
                                            out.println("\u001B[31m" + "Card gia esiste in questo progetto" + "\u001B[0m");
                                        }
                                    } else {
                                        out.println("\u001B[31m" + "Card già esistente" + "\u001B[0m");
                                    }
                                } else {
                                    out.println("\u001B[31m" + "Non sei membro oppure il progetto che hai inserito non esiste, RICONTROLLA" + "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Non sei loggato quindi niente Add Card" + "\u001B[0m");
                            }
                            break;
                        case "showCards":
                            if (loggato) {


                                // scrivo il contenuto del file Student.json dentro alla struttura dati
                                database.ReadDatabase("Student.json");
                                // qua popolo il database locale con il metodo che sta nella classe DataBase
                                DataBaseLL = database.getHash();

                                // guardo se sei membro di tale progetto

                                if (database.seiMembro(pippo[1], nickNameUser)) {
                                    // se entro qua sono membro

                                    // creo l'instanza del progetto
                                    Progetti project = new Progetti(pippo[1]);

                                    // vado a popolare tutte le strutture dati che rappresentano lo stato della Card
                                    project.readAllList(pippo[1]);


                                    //Dopo aver letto tutte le liste ora posso prenderle

                                    // struttura di supporto
                                    ArrayList<Card> listaCard;
                                    ArrayList<String> nomiCard = new ArrayList<>();

                                    // ci metto dentro tutte le card
                                    listaCard = project.getCard();

                                    // scorro e prendo il nome di ogni Card
                                    for (Card c : listaCard) {
                                        nomiCard.add(c.getNomeCard());
                                    }

                                    Gson gson = new Gson();
                                    // mi crea la stringa json
                                    String lugah = gson.toJson(nomiCard, nomiCard.getClass());
                                    out.println(lugah);

                                } else {
                                    out.println("\u001B[31m" + "Non sei un membro del progetto, quindi niente lista Card" + "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Non sei loggato quindi niente lista Card del progetto" + "\u001B[0m");
                            }
                            break;
                        case "showCard":
                            if (loggato) {

                                //prima di tutto guardiamo sei sei mebro di tale progetto
                                if (database.seiMembro(pippo[1], nickNameUser)) {

                                    Progetti project = new Progetti(pippo[1]);

                                    // popolo le varie strutture dati che rappresentano gli stati delle Card
                                    project.readAllList(pippo[1]);

                                    // una volta inizializzate le varie strutture che contengono le Card, controllo se la Card esiste
                                    if (project.esisteCard(pippo[2])) {
                                      // se la Card esiste, recupero le sue info

                                        String info;
                                        info = project.returnCard(pippo[2]);

                                        out.println(info);

                                    } else {
                                        out.println("\u001B[31m" + "La Card selezionata non esiste" + "\u001B[0m");
                                    }
                                } else {
                                    out.println("\u001B[31m" + "Il nome del progetto inserito non è corretto o non esiste" + "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Non sei loggato, operazione SHOW CARD non disponibile!" + "\u001B[0m");
                            }
                            break;
                        case "cancelProject":

                            if (loggato) {

                                // prima vediamo se l'utente che vuole eliminare un progetto è membro di tale progetto

                                if (database.seiMembro(pippo[1], nickNameUser)) {
                                    // se sei membro andiamo a cancellare il progetto


                                    Progetti project = new Progetti(pippo[1]);
                                    GeneratoreIP gen = new GeneratoreIP();

                                    // popolo le liste dentro alla classse Progetti
                                    project.readAllList(pippo[1]);

                                    // controllo se il progetto può essere eliminato
                                    if (project.projectDone()) {
                                        // qua dentro vuol dire che devo eliminare il progetto


                                        //prima di tutto devo levarlo dalla lista
                                        database.deletedProject(pippo[1]);

                                        // elimino la cartella del progetto
                                        project.removeDirecotry(pippo[1]);

                                        gen.ReadIndirizziIP("Indirizzi.json");

                                        // rimuovo il progetto e il suo ind IP associato dal file indirizzi.Json
                                        gen.rimuoviIP(pippo[1]);

                                        // aggiorno tale filoe .json
                                        gen.WriteIndirizziIP("Indirizzi.json");


                                        out.println("\u001B[32m" + "Progetto eliminato" + "\u001B[0m");


                                    } else {
                                        out.println("\u001B[31m" + "Ci sono Card ancora da completare CANCEL PROJECT non dispobile!" + "\u001B[0m");
                                    }
                                } else {
                                    out.println("\u001B[31m" + "Il nome del progetto che hai inserito non è corretto,ricontrolla!" + "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Non sei loggato oppure il progetto non esiste, CANCEL PROJECT non disponibile!" + "\u001B[0m");
                            }
                            break;
                        case "moveCard":
                            if (loggato) {
                                // prima devo vedere se sono membro del progetto
                                String[] pippo1 = inputLine.split(" ", 5);

                                if (database.seiMembro(pippo[1], nickNameUser)) {
                                    // se sono membro, controllo che esista pure la Card che voglio spostare

                                    Progetti project = new Progetti(pippo[1]);

                                    project.readAllList(pippo1[1]);

                                    if (project.esisteCard(pippo1[2])) {
                                        // se la Card esiste la spostiamo


                                        String valore = project.moveCard(pippo1[2], pippo1[1], pippo1[3], pippo1[4]);

                                        out.println(valore);


                                    } else {
                                        out.println("\u001B[31m" + "La Card che vuoi spostare non esiste" + "\u001B[0m");
                                    }
                                } else {
                                    out.println("\u001B[31m" + "Non fai parte del progetto, MOVE CARD non disponibile!" + "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Non sei loggato, MOVE CARD non disponibile!" + "\u001B[0m");
                            }
                            break;

                        case "getHistory":

                            if (loggato) {
                                // guardo se sono membro del progetto
                                if (database.seiMembro(pippo[1], nickNameUser)) {
                                    // se sono membro guardo se la Card esiste


                                    Progetti project = new Progetti(pippo[1]);
                                    project.readAllList(pippo[1]);

                                    if (project.esisteCard(pippo[2])) {
                                        // se la Card esiste allora andiamo a recuperare la sua story


                                        ArrayList<String> allHistory = project.getHistory(pippo[2]);

                                        Gson gson = new Gson();
                                        // mi crea la stringa json
                                        String lugah = gson.toJson(allHistory, allHistory.getClass());
                                        out.println(lugah);

                                    } else {
                                        out.println("\u001B[31m" + "La Card specificata non esiste, GET HISTORY, non disponibile!" + "\u001B[0m");
                                    }
                                } else {
                                    out.println("\u001B[31m" + "Non sei un membro del progetto, GET HYSTORY, non disponibile" + "\u001B[0m");
                                }
                            } else {
                                out.println("\u001B[31m" + "Non sei loggato, GET HISTORY, non disponibile!" + "\u001B[0m");
                            }
                            break;
                        case "getOnline":
                            if (loggato) {


                                database.ReadDatabase("Student.json");

                                Vector<String> online = database.getUtentiOnline();


                                Gson gson = new Gson();
                                // mi crea la stringa json
                                String lugah = gson.toJson(online, online.getClass());
                                out.println(lugah);


                            } else {
                                out.println("\u001B[31m" + "Non sei loggato" + "\u001B[0m");
                            }
                            break;
                        case "readChat":
                            if (loggato) {

                                if (database.seiMembro(pippo[1], nickNameUser)) {

                                    GeneratoreIP gen = new GeneratoreIP();

                                    gen.ReadIndirizziIP("Indirizzi.json");

                                    ArrayList<Progetti> progIp = gen.getListaIndirizziIP();

                                    for (Progetti g : progIp) {
                                        if (g.getNomeProgetto().equals(pippo[1])) {
                                            String IP = g.getIndirizzoIp();
                                            out.println("Dario" + IP);
                                            break;
                                        }
                                    }


                                } else {
                                    // fare vari controllo per vedere cosa succede se metto il nome del progetto sbagliato
                                    out.println("Non sei membro del progetto, oppure il progetto inserito non esiste");
                                }
                            } else {
                                out.println("Non sei loggato");
                            }
                            break;
                        case "sendMessage":
                            if (loggato) {

                                String[] pippo1 = inputLine.split(" ", 4);


                                if (pippo[1].equals(nickNameUser)) {
                                    // se sono loggato controllo se sono membro e se il progetto a cui voglio mandare il messaggio esiste
                                    if (database.seiMembro(pippo1[2], nickNameUser)) {
                                        // se sono mebro e il progetto esiste

                                        GeneratoreIP gen = new GeneratoreIP();

                                        gen.ReadIndirizziIP("Indirizzi.json");

                                        ArrayList<Progetti> progIp = gen.getListaIndirizziIP();

                                        String IP = "";
                                        for (Progetti g : progIp) {
                                            if (g.getNomeProgetto().equals(pippo1[2])) {
                                                IP = g.getIndirizzoIp();
                                            }
                                        }
                                        out.println(IP);
                                    } else {
                                        out.println("Non sei membro del progetto, oppure il progetto inserito non esiste");
                                    }
                                } else {
                                    out.println("Credenziali per accedere alla chat errate");
                                }
                            } else {
                                out.println("Non sei loggato, Send Message non disponibile!");
                            }
                            break;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    // io creo n Thrad nel server, e ogni Thread che il server crea, lo crea ogni volta arriva un client che richiede il login
    public void start() throws IOException {
        serverSocket = new ServerSocket(20000);
        while (true) {
            // Finche è vero e arriva un client ,accetto la richiesta del client e creo un thread dedicato a lui
            threadpool.execute(new EchoClientServer(serverSocket.accept()));;
        }
    }
}

