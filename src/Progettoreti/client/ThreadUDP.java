package Progettoreti.client;

import Progettoreti.client.MainClassC;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


// Qua creo il Thred che ricevera le info di ogni progetto singolo
// Quindi creo un Thread per ogni progetto che arriva
public class ThreadUDP implements Runnable {

    String indirizzoIp;

    public ThreadUDP(String indirizzoIp) {
        this.indirizzoIp = indirizzoIp;
    }

    @SuppressWarnings("deprecation")
    private void receivedMessage() throws IOException, InterruptedException {

        byte[] buffer = new byte[1024];

        MulticastSocket socket = new MulticastSocket(3345);
        InetAddress group = InetAddress.getByName(indirizzoIp);
        socket.joinGroup(group);

        while (!Thread.currentThread().isInterrupted() && MainClassC.loggato) {
            Thread.sleep(4000);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String messaggio = new String(packet.getData(), packet.getOffset(), packet.getLength());
            if (MainClassC.loggato) {
                System.out.println(messaggio);
            }
        }
        socket.leaveGroup(group);
        socket.close();

    }

    @Override
    public void run() {
        try {
            receivedMessage();
        } catch (IOException | InterruptedException e) {

        }
    }
}
