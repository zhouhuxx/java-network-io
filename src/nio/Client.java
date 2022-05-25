package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class Client {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5000;
    private Selector selector;
    private SocketChannel socketChannel;

    public Client() {
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(HOST, PORT));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("客户端已开启");
        } catch (IOException e)  {
            e.printStackTrace();
        }
    }

    public void read() {
        while (true) {
            try {
                int count = selector.select();
                if (count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        if (selectionKey.isReadable()) {
                            SocketChannel channel = (SocketChannel) selectionKey.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            channel.read(byteBuffer);
                            System.out.println(new String(byteBuffer.array()));
                            byteBuffer.clear();
                        }
                        iterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send() {
        Scanner input = new Scanner(System.in);
        while (input.hasNextLine()) {
            try {
                socketChannel.write(ByteBuffer.wrap(input.nextLine().getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        System.out.println("客户端已关闭");
        try {
            selector.close();
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        new Thread(client::read).start();
        client.send();
    }
}
