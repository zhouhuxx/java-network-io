package bio;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable {
    Socket socket;
    public Client(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try (BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (true) {
                    System.out.println(socketIn.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException{
        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("please enter your id: ");
        String id = systemIn.readLine();
        String address = "127.0.0.1";
        int port = 4500;
        Socket socket = new Socket(address, port);
        new Thread(new Client(socket)).start();
        try (BufferedWriter socketOut= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            socketOut.write(id);
            socketOut.newLine();
            socketOut.flush();
            String messSen;
            do {
                messSen = systemIn.readLine();
                socketOut.write(messSen);
                socketOut.newLine();
                socketOut.flush();
            } while (!messSen.equals("quit"));
        }
        socket.close();
        systemIn.close();
    }
}
