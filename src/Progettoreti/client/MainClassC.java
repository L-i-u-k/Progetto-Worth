package Progettoreti.client;

import Progettoreti.callback.ClientNotifica;
import Progettoreti.callback.ClientNotificaImpl;
import Progettoreti.callback.ServerNotifica;
import Progettoreti.registrazione.Registrazione;
import Progettoreti.registrazione.RegistrazioneImpl;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class MainClassC {

    public static boolean loggato = false;
    static Thread chat;


    // funzione che mi permette di controllare eventuali valori di input non consentiti
    public static boolean check(String... args) {
        boolean valore = false;
        for (String s : args) {
            valore = valore || s.equals("") || s.contains(" ");
        }
        return valore;
    }

    // funzione che mi scrive all'interno del file Utenti.json
    private static String listaUtenti() throws IOException {
        File file = new File("Utenti.json");
        Scanner scanner = new Scanner(file);
        String lista = "";

        while (scanner.hasNextLine()) {
            String data = scanner.nextLine();
            lista += data + "\n";
        }
        scanner.close();
        return lista;
    }

    private static void startChat(String IP) {
        ThreadUDP ciao = new ThreadUDP(IP);
        chat = new Thread(ciao);
        chat.start();
    }

    private static void stopChat() {
        if (chat != null) {
            chat.interrupt();
        }
    }


    //MAIN DEL CLIENT
    public static void main(String[] args) {
        boolean cond = true;
        Registrazione serverObject;
        String Username = null;
        String Password;
        String Progetto;
        String Descrizione;
        String nomeCard;
        String messaggio;


        RegistrazioneImpl pippo = new RegistrazioneImpl();

        ClientTCP clientTCP = new ClientTCP();

        try {
            Registry reg = LocateRegistry.getRegistry(30000);
            serverObject = (Registrazione) reg.lookup("Registrazione");


            Registry regCB = LocateRegistry.getRegistry(40000);
            ServerNotifica server = (ServerNotifica) regCB.lookup("Server");
            ClientNotifica callBack = new ClientNotificaImpl();
            ClientNotifica stubCB = (ClientNotifica) UnicastRemoteObject.exportObject(callBack, 0);


            while (cond) {
                Scanner in = new Scanner(System.in);
                Scanner Input = new Scanner(System.in);
                System.out.println("*** BEVENUTI IN WORTH ***");
                System.out.println("Seleziona un evento:");

                System.out.println(" 0  Uscire ");
                System.out.println(" 1  Registrati");
                System.out.println(" 2  Login");

                String menu1 = in.next();
                int m1;
                boolean cond1 = true;

                try {
                    m1 = Integer.parseInt(menu1);
                } catch (NumberFormatException e) {
                    m1 = -1;
                }
                while (cond1) {

                    switch (m1) {
                        case 0 -> {
                            // Esco dal primo while quello relativo al primo menù
                            cond1 = false;
                            // Esco dal secondo while quello relativo al menù principale
                            cond = false;
                            // chiudo il thread specifico
                            clientTCP.Exit();
                            System.exit(0);
                        }
                        case 1 -> {

                            System.out.println("\u001B[32m" + "Inserisci l'username" + "\u001B[0m");
                            Username = Input.nextLine();
                            System.out.println("\u001B[32m" + "Inserisci la password" + "\u001B[0m");
                            Password = Input.nextLine();
                            pippo.registrazione(Username, Password);
                            Username = null;
                            cond1 = false;
                        }
                        // una volta fatto login devo togliere alcune informazioni da visualizzare
                        case 2 -> {
                            clientTCP.connessioneTCP();
                            System.out.println("\u001B[32m" + "Username" + "\u001B[0m");
                            Username = Input.nextLine();
                            System.out.println("\u001B[32m" + "Password" + "\u001B[0m");
                            Password = Input.nextLine();
                            if (check(Username, Password)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                Username = null;
                                cond1 = false;
                                break;
                            }

                            server.registrazioneServizio(stubCB, Username);
                            String valoreLogin = clientTCP.Login(Username, Password);
                            System.out.println(valoreLogin);


                            if (!valoreLogin.equals("\u001B[32m" + "Login avvenuto con successo" + "\u001B[0m")) {
                                server.deregistrazioneServizio(stubCB, Username);
                                Username = null;
                            } else {
                                String listaUtenti = listaUtenti();
                                System.out.println("\u001B[32m" + "Lista utenti" + "\u001B[0m" + listaUtenti);
                                loggato = true;
                            }
                            cond1 = false;
                        }
                        default -> {
                            cond1 = false;
                        }
                    }
                }


                while (loggato) {


                    System.out.println(" 1  Logout" + "             " + " 8  Mostra Card");
                    System.out.println(" 2  Crea Progetto" + "      " + " 9  Cancella progetto");
                    System.out.println(" 3  Lista Progetti " + "     " + "10 Sposta Card");
                    System.out.println(" 4  Aggiungi Membro" + "    " + " 11 Ottieni la History");
                    System.out.println(" 5  Mostra Membri" + "      " + " 12 Lista Utenti");
                    System.out.println(" 6  Aggiungi una Card" + "  " + " 13 Utenti On line");
                    System.out.println(" 7  Mostra Cards " + "       " + "14 Leggi Chat");
                    System.out.println(" " + "                       " + "15 Invia Messaggio");


                    String inputDaTastiera = in.next();
                    int val;

                    try {
                        val = Integer.parseInt(inputDaTastiera);
                    } catch (NumberFormatException e) {
                        val = -1;
                    }
                    switch (val) {
                        case 1 -> {
                            if (Username != null) {

                                String valoreLogout = clientTCP.Logout(Username);
                                server.deregistrazioneServizio(stubCB, Username);
                                stopChat();
                                Username = null;
                                System.out.println(valoreLogout);
                            } else {
                                System.out.println("\u001B[31m" + "Non sei loggato" + "\u001B[0m");
                            }
                            loggato = false;
                        }

                        case 2 -> {

                            System.out.println("Inserisci il nome del nuovo progetto");
                            Progetto = Input.nextLine();
                            if (check(Progetto)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }
                            String valoreProgetto = clientTCP.CreaProgetto(Progetto);
                            System.out.println(valoreProgetto);
                        }
                        case 3 -> {

                            String valoreListaProgetti = clientTCP.ListaProgetti();
                            String[] array;
                            int i = 1;
                            Gson gson = new Gson();
                            try {
                                array = gson.fromJson(valoreListaProgetti, String[].class);

                                for (String s : array) {
                                    System.out.println("Progetto " + i + " :" + s);
                                    i++;
                                }


                            } catch (Exception e) {
                                System.out.println("\u001B[31m" + "Non sei loggato, opzione non disponibile" + "\u001B[0m");
                            }
                        }
                        case 4 -> {

                            System.out.println("Inserisci il Nick dell'utente che vuoi aggiungere");
                            String utenteDaAggiugnere = Input.nextLine();
                            System.out.println("Inserisci il nome del progetto in cui vuoi aggiungere l'utente");
                            Progetto = Input.nextLine();
                            if (check(utenteDaAggiugnere, Progetto)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }
                            String valoreAggiungiMembro = clientTCP.aggiungiMembro(utenteDaAggiugnere, Progetto);
                            System.out.println(valoreAggiungiMembro);
                        }
                        case 5 -> {

                            System.out.println("Inserisci il nome del progetto di cui vuoi visualizzare gli utenti");
                            Progetto = Input.nextLine();
                            if (check(Progetto)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }
                            String valoreShowMembers = clientTCP.showMembers(Progetto);
                            String[] array1;
                            int i = 1;
                            Gson gson1 = new Gson();
                            try {
                                array1 = gson1.fromJson(valoreShowMembers, String[].class);
                                for (String s : array1) {
                                    System.out.println("Membro" + i + ":" + " " + s);
                                    i++;
                                }
                            } catch (Exception e) {
                                System.out.println("\u001B[31m" + "La lista dei membri è vuota oppure il nome del progetto è errato,ricontrolla!" + "\u001B[0m");
                            }
                        }
                        case 6 -> {

                            System.out.println("Inserisci il nome del progetto");
                            Progetto = Input.nextLine();
                            System.out.println("Inserisci il nome della carta");
                            nomeCard = Input.nextLine();
                            System.out.println("Inserisci una breve descerizione della Card");
                            Descrizione = Input.nextLine();
                            if (check(Progetto, nomeCard)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }
                            String valoreaddCard = clientTCP.addCard(Progetto, nomeCard, Descrizione);
                            System.out.println(valoreaddCard);
                        }
                        case 7 -> {

                            System.out.println(" Inserisci il nome del  progetto");
                            Progetto = Input.nextLine();
                            if (check(Progetto)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }
                            String valoreShowCards = clientTCP.showCards(Progetto);
                            String[] array2;
                            int i = 1;
                            Gson gson2 = new Gson();
                            try {
                                array2 = gson2.fromJson(valoreShowCards, String[].class);
                                for (String s : array2) {
                                    System.out.println("Card " + " " + i + ":" + " " + s);
                                    i++;
                                }
                            } catch (Exception e) {
                                System.out.println("\u001B[31m" + "Il progetto richiesto non esiste" + "\u001B[0m");

                            }
                        }
                        case 8 -> {

                            System.out.println("Inserisci il nome del progetto");
                            Progetto = Input.nextLine();
                            System.out.println("Inserisci il nome della Card");
                            nomeCard = Input.nextLine();
                            if (check(Progetto, nomeCard)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }
                            String valoreShowCard = clientTCP.showCard(Progetto, nomeCard);
                            System.out.println(valoreShowCard.replace("£", "\n"));
                        }
                        case 9 -> {

                            System.out.println("Inserisci il nome del progetto da cancellare");
                            Progetto = Input.nextLine();
                            if (check(Progetto)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }
                            String valoreCancellaProgetto = clientTCP.cancelProject(Progetto);
                            System.out.println(valoreCancellaProgetto);
                        }
                        case 10 -> {
                            String listaPartenza = null;
                            String listaDestinazione = null;

                            System.out.println("Inserisci il nome del progetto");
                            Progetto = Input.nextLine();
                            System.out.println("Inserisci il nome della Card");
                            nomeCard = Input.nextLine();

                            if (check(Progetto, nomeCard)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }


                            System.out.println("Scegli la lista di partenza");

                            System.out.println(" 0 Esci");
                            System.out.println(" 1  todolist");
                            System.out.println(" 2  inprogress");
                            System.out.println(" 3  reseived");
                            System.out.println(" 4  done");

                            String val1 = in.next();
                            int valore;
                            boolean condizione = true;

                            try {
                                valore = Integer.parseInt(val1);
                            } catch (NumberFormatException e) {
                                valore = 0;
                            }


                            while (condizione) {


                                switch (valore) {
                                    case 0 -> condizione = false;
                                    case 1 -> {
                                        listaPartenza = "todolist";
                                        condizione = false;
                                    }
                                    case 2 -> {
                                        listaPartenza = "inprogress";
                                        condizione = false;
                                    }
                                    case 3 -> {
                                        listaPartenza = "reseived";
                                        condizione = false;
                                    }
                                    case 4 -> {
                                        listaPartenza = "done";
                                        condizione = false;
                                    }
                                    default -> {
                                        condizione = false;
                                    }

                                }

                            }

                            // con questo non entro nel secondo switch
                            if (valore == 0 || valore > 4) {
                                break;
                            }

                            System.out.println("Inseriscila lista di destinazione");

                            System.out.println(" 0 Esci");
                            System.out.println(" 1  todolist");
                            System.out.println(" 2  inprogress");
                            System.out.println(" 3  reseived");
                            System.out.println(" 4  done");

                            boolean condizione2 = true;

                            String val2 = in.next();
                            int valore2;

                            try {
                                valore2 = Integer.parseInt(val2);
                            } catch (NumberFormatException e) {
                                valore2 = 0;
                            }


                            while (condizione2) {

                                switch (valore2) {
                                    case 0 -> condizione2 = false;
                                    case 1 -> {
                                        listaDestinazione = "todolist";
                                        condizione2 = false;
                                    }
                                    case 2 -> {
                                        listaDestinazione = "inprogress";
                                        condizione2 = false;
                                    }
                                    case 3 -> {
                                        listaDestinazione = "reseived";
                                        condizione2 = false;
                                    }
                                    case 4 -> {
                                        listaDestinazione = "done";
                                        condizione2 = false;
                                    }

                                }

                            }

                            if (valore2 == 0) {
                                break;
                            }
                            String valoreMoveCard = clientTCP.moveCard(Progetto, nomeCard, listaPartenza, listaDestinazione);
                            System.out.println(valoreMoveCard);
                        }


                        case 11 -> {
                            System.out.println("Inserisci il nome del progetto");
                            Progetto = Input.nextLine();
                            System.out.println("Inserisci il nome della Card");
                            nomeCard = Input.nextLine();
                            if (check(Progetto, nomeCard)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }

                            String valoreGetHistory = clientTCP.getHistory(Progetto, nomeCard);
                            String[] array1;
                            Gson gson1 = new Gson();
                            int i = 0;
                            try {
                                array1 = gson1.fromJson(valoreGetHistory, String[].class);
                                for (String s : array1) {
                                    System.out.println("Storia" + " " + i + ":" + " " + s);
                                    i++;
                                }
                            } catch (Exception e) {
                                System.out.println("\u001B[31m" + "Il progetto inserito o la Card non esistono" + "\u001B[0m");
                            }
                        }

                        case 12 -> {
                            String listaUtenti = listaUtenti();
                            System.out.println("\u001B[32m" + "Lista utenti registrati con stato" + "\u001B[0m" + listaUtenti);
                        }
                        case 13 -> {

                            System.out.println("\u001B[32m" + "Lista utenti On line" + "\u001B[0m");
                            String userOnline = clientTCP.getOnline();
                            String[] array1;
                            Gson gson1 = new Gson();
                            int i = 1;
                            try {
                                array1 = gson1.fromJson(userOnline, String[].class);
                                for (String s : array1) {
                                    System.out.println("Utente" + " " + i + ":" + " " + s);
                                    i++;
                                }
                            } catch (Exception e) {
                                System.out.println("\u001B[31m" + "Il progetto inserito o la Card non esistono" + "\u001B[0m");
                            }
                        }
                        case 14 -> {
                            System.out.println("Inserisci il nome del progetto");
                            Progetto = Input.nextLine();
                            if (check(Progetto)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }
                            String valoreReadChat = clientTCP.readChat(Progetto);
                            if (valoreReadChat.contains("Dario")) {
                                startChat(valoreReadChat.substring(5));
                                System.out.println("\u001B[32m" + "Rimani in attesa dei messaggi" + "\u001B[0m");
                            } else {
                                System.out.println("\u001B[31m" + "Il progetto inserito non esiste,riprova! " + "\u001B[0m");
                            }
                        }
                        case 15 -> {
                            System.out.println("Inserisci il tuo username");
                            String nomeUtente = Input.nextLine();
                            System.out.println("Inserisci il nome del progetto");
                            Progetto = Input.nextLine();
                            System.out.println("Inserisci il messaggio che vuoi inviare");
                            Scanner scanner = new Scanner(System.in);
                            messaggio = scanner.nextLine();
                            if (check(Progetto, nomeUtente)) {
                                System.out.println("\u001B[31m" + "Credenziali non valide" + "\u001B[0m");
                                break;
                            }
                            // valore conterrà l'in IP che mi ritorna dal server
                            String valoreSendMessage = clientTCP.sendMessage(Progetto, messaggio, nomeUtente);

                            UDPServer.sendMessage(messaggio, valoreSendMessage, 3345, nomeUtente);

                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
