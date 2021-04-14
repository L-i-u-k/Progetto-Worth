package Progettoreti.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


// QUESTA CLASSE CONTIENE L'INSTAURAZIONE DELLA CONNESSIONE TCP DA PARTE DEL CLIENT,
// E TUTTI I METODI CHE VENGONO RICHIAMATI DENTRO A MainClassC dentro allo switch case
public class ClientTCP {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientTCP() {
    }

    // instaurazione della connesione
    public void connessioneTCP() throws IOException {
        if (clientSocket == null)
            clientSocket = new Socket("127.0.0.1", 20000);
        //if(out == null)
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        //if(in == null)
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    }

    public void Exit() throws IOException {
        if (out == null) {
            System.out.println("Errore 💔");
        } else {
            out.println("Exit");
            System.out.println(in.readLine());
        }
    }

    public String Login(String username, String password) throws IOException {
        out.println("Login " + username + " " + password);
        return in.readLine();
    }

    public String Logout(String username) throws IOException {
        out.println("Logout " + username);
        return in.readLine();
    }

    public String CreaProgetto(String Progetto) throws IOException {
        out.println("Progetto " + Progetto);
        return in.readLine();
    }

    public String ListaProgetti() throws IOException {
        out.println("ListaProgetti");
        return in.readLine();
    }

    public String aggiungiMembro(String username, String progetto) throws IOException {
        out.println("Aggiungi " + username + " " + progetto);
        return in.readLine();
    }

    public String showMembers(String progetto) throws IOException {
        out.println("showMembers " + progetto);
        return in.readLine();
    }

    public String addCard(String nomeCard, String progetto, String desrizione) throws IOException {
        out.println("addCard " + progetto + " " + nomeCard + " " + desrizione);
        return in.readLine();
    }

    public String showCards(String progetto) throws IOException {
        out.println("showCards " + progetto);
        return in.readLine();
    }

    public String showCard(String progetto, String nomecard) throws IOException {
        out.println("showCard " + progetto + " " + nomecard);
        return in.readLine();
    }

    public String cancelProject(String progetto) throws IOException {
        out.println("cancelProject " + progetto);
        return in.readLine();
    }

    public String moveCard(String progetto, String card, String partenza, String destinazione) throws IOException {
        out.println("moveCard " + progetto + " " + card + " " + partenza + " " + destinazione);
        return in.readLine();
    }

    public String getHistory(String progetto, String nomecard) throws IOException {
        out.println("getHistory " + progetto + " " + nomecard);
        return in.readLine();
    }

    public String getOnline() throws IOException {
        out.println("getOnline");
        return in.readLine();
    }

    public String readChat(String nomeprogetto) throws IOException {
        out.println("readChat " + nomeprogetto);
        return in.readLine();
    }

    public String sendMessage(String nomeprogetto, String messaggio, String nome) throws IOException {
        out.println("sendMessage " + nome + " " + nomeprogetto + " " + messaggio);
        return in.readLine();
    }


}
