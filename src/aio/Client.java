package aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Client {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5500;
    private AsynchronousSocketChannel socketChannel;

    public Client() {
        try {
            socketChannel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        socketChannel.connect(new InetSocketAddress(HOST, PORT), null, new ConnectHandler());
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.connect();
    }

    class ConnectHandler implements CompletionHandler<Void, Void> {

        @Override
        public void completed(Void result, Void attachment) {
            System.out.println("客户端已开启");
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            socketChannel.read(byteBuffer, byteBuffer, new ReadHandler());
            Scanner input = new Scanner(System.in);
            socketChannel.write(ByteBuffer.wrap(input.nextLine().getBytes()), input, new WriteHandler());
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            exc.printStackTrace();
        }
    }

    class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            socketChannel.read(byteBuffer, byteBuffer, this);
            if (result > 0) {
                System.out.println(new String(attachment.array()));
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            exc.printStackTrace();
        }
    }

    class WriteHandler implements CompletionHandler<Integer, Scanner> {

        @Override
        public void completed(Integer result, Scanner attachment) {
            socketChannel.write(ByteBuffer.wrap(attachment.nextLine().getBytes()), attachment, this);
        }

        @Override
        public void failed(Throwable exc, Scanner attachment) {
            exc.printStackTrace();
        }
    }
}
