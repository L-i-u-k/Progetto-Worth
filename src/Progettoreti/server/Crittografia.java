package Progettoreti.server;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crittografia {

    public static String critto(String password) throws NoSuchAlgorithmException {

        // Qua creo l'hash della password che gli passo in input
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        //In questo caso vado a convertire l'array di byte
        BigInteger valore = new BigInteger(1, hash.digest(password.getBytes(StandardCharsets.UTF_8)));
        // vado a convertire valore in un numero esadecimale
        StringBuilder valore1 = new StringBuilder(valore.toString(16));
        // qua devo far si che le stringhe siano lunghe tutte la stessa lunghezza cosi da non avere cifrature diverse
        while (valore1.length() < 32) {
            valore1.insert(0, '0');
        }
        return valore1.toString();
    }

}
