package Progettoreti.callback;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerNotifica  extends Remote{

     void registrazioneServizio(ClientNotifica notifica, String user)throws RemoteException;

     void deregistrazioneServizio(ClientNotifica notica,String user) throws RemoteException;

}

