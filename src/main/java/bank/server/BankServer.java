package bank.server;

import bank.local.Driver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BankServer {

    private final Driver.Bank bank = new Driver.Bank();

    public static void main(String[] args) throws IOException {
        BankServer bank = new BankServer();
        bank.start();
    }

    public void start() throws IOException {
        try (ServerSocket s = new ServerSocket(12345)) {
            while (true) {
                Socket sock = s.accept();
                System.out.println("New connection, starting thread!");
                Thread t = new Thread(new BankHandler(sock, bank));
                t.start();
            }
        }
    }

    private static class BankHandler implements Runnable {

        private final DataOutputStream os;
        private final DataInputStream is;
        private final Driver.Bank bank;

        public BankHandler(Socket s, Driver.Bank bank) throws IOException {
            this.bank = bank;
            this.os = new DataOutputStream(s.getOutputStream());
            this.is = new DataInputStream(s.getInputStream());
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String[] arr = is.readUTF().split("\\|");
//                    System.out.println("Called: " + String.join("|", arr));
                    synchronized (bank) {
                        os.writeUTF(BankCommand.fromString(arr[0]).action(arr, bank));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

}
