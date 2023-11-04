package caso3;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

public class HashMiner {
    private static final AtomicBoolean found = new AtomicBoolean(false);
    private static String winningHash = "";
    private static String winningNonce = "";

    public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException {
        //parametros de entrada
        String algorithm = "SHA-256"; // tambien se puede usar "SHA-512"
        String C = "ejemploCadena123"; // Cadena entre 16 y 20
        int zeroBits = 24; // ceros buscado: 20, 24, 28, 32 o 36
        int threadsNum = 2; // threads

        long startTime = System.currentTimeMillis();
        Thread[] miners = new Thread[threadsNum];
        for (int i = 0; i < threadsNum; i++) {
            miners[i] = new MiningThread(algorithm, C, zeroBits, i, threadsNum);
            miners[i].start();
        }
        for (Thread miner : miners) {
            miner.join();
        }
        long endTime = System.currentTimeMillis();
        if (found.get()) {
            System.out.println("Cadena de entrada (C): " + C);
            System.out.println("Valor v que permite cumplir la condici�n: " + winningNonce);
            System.out.println("C�digo hash generado (ch): " + winningHash);
        } else {
            System.out.println("No se encontr� una soluci�n en el espacio de b�squeda dado.");
        }
        System.out.println("Tiempo de b�squeda: " + (endTime - startTime) + " ms");
    }

    public static class MiningThread extends Thread {
        private final MessageDigest digest;
        private final String C;
        private final int zeroBits;
        private final int threadId;
        private final int threadsNum;

        public MiningThread(String algorithm, String C, int zeroBits, int threadId, int threadsNum) throws NoSuchAlgorithmException {
            this.digest = MessageDigest.getInstance(algorithm);
            this.C = C;
            this.zeroBits = zeroBits;
            this.threadId = threadId;
            this.threadsNum = threadsNum;
        }

        @Override
        public void run() {
            String candidate;
            for (int i = threadId; !found.get(); i += threadsNum) {
                candidate = C + i; 
                byte[] hash = digest.digest(candidate.getBytes());
                if (checkZeros(hash, zeroBits)) {
                    found.set(true);
                    winningHash = bytesToHex(hash);
                    winningNonce = Integer.toString(i);
                    System.out.println("Thread " + threadId + " encontr� la soluci�n: " + winningNonce);
                    return;
                }
            }
        }
        private boolean checkZeros(byte[] hash, int zeroBits) {
            int fullZeroBytes = zeroBits / 8;
            int extraZeroBits = zeroBits % 8;

            for (int i = 0; i < fullZeroBytes; i++) {
                if (hash[i] != 0) return false;
            }

            if (extraZeroBits == 0) return true;
            int mask = 0xFF >>> extraZeroBits;
            return (hash[fullZeroBytes] & mask) == 0;
        }

        private String bytesToHex(byte[] hash) {
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
    }
}