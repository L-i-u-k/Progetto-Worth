package Progettoreti.registrazione;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;


public interface Registrazione extends Remote {

    void registrazione(String nome, String password) throws RemoteException, NoSuchAlgorithmException;



}



