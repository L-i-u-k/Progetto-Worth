package Progettoreti.callback;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface ClientNotifica extends Remote {

    void notificaUser(Vector<String> lista)throws RemoteException;

}
