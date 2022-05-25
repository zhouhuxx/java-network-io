package bio;

import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    static AtomicInteger num = new AtomicInteger(0);
    public static void main(String[] args) throws IOException {
        int port = 4500;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("服务器启动...");
        Message message = new Message();
        ExecutorService service = Executors.newFixedThreadPool(8);
        while (true) {
            Socket socket = serverSocket.accept();
            num.getAndIncrement();
            service.submit(new Receive(socket, message));
            service.submit(new Send(socket, message));
        }
    }
}

class Receive implements Runnable {
    Socket socket;
    Message message;
    public Receive(Socket socket, Message message) {
        this.socket = socket;
        this.message = message;
    }
    @Override
    public void run() {
        try (BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String id = socketIn.readLine();
            String mess;
            while (true) {
                mess = socketIn.readLine();
                System.out.println(id + ": " + mess);
                if (mess.equals("quit")) {
                    break;
                }
                message.setMess(id + ": " + mess);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Send implements Runnable {
    Socket socket;
    Message message;
    public Send(Socket socket, Message message) {
        this.socket = socket;
        this.message = message;
    }
    @Override
    public void run() {
        try (BufferedWriter socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            while (true) {
                socketOut.write(message.getMess());
                socketOut.newLine();
                socketOut.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Server.num.getAndDecrement();
        }
    }
}

class Message {
    private String mess;
    private boolean isRead = true;
    int count;
    public Message() {
    }
    public void setMess(String mess) {
        synchronized(this) {
            while (!isRead) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isRead = false;
            this.mess = mess;
            notifyAll();
        }
    }
    public String getMess() {
        synchronized(this) {
            while (isRead) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            count++;
            if (count == Server.num.get()) {
                isRead = true;
                count = 0;
            }
            notifyAll();
            return mess;
        }
    }
}