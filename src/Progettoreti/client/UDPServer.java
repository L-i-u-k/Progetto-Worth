package Progettoreti.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPServer {

    public static void sendMessage(String messaggio, String indirizzoIP, int porta,String nome) throws IOException {
try{
    messaggio = nome + ":" + messaggio;
    DatagramSocket socket = new DatagramSocket();
    InetAddress group = InetAddress.getByName(indirizzoIP);
    byte[] msg = messaggio.getBytes();
    DatagramPacket packet = new DatagramPacket(msg, msg.length, group, porta);
    socket.send(packet);
    socket.close();

}catch (UnknownHostException e){
    System.out.println("\u001B[31m" +"Una delle informazioni inserite non è corretta, ricontrolla!" + "\u001B[0m");
}

    }
}
