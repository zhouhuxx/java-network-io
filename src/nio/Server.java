package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class Server {
    private static final String HOST = "127.0.0.1";
    private static final int POST = 5000;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public Server() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(HOST, POST));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器已开启");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        while (true) {
            try {
                int count = selector.select(1000);
                if (count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        if (selectionKey.isAcceptable()) {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            System.out.println(socketChannel.getRemoteAddress() + "已上线");
                        }
                        if (selectionKey.isReadable()) {
                            read(selectionKey);
                        }
                        iterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void read(SelectionKey selectionKey) {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try {
            int count = socketChannel.read(byteBuffer);
            if (count > 0) {
                String msg = new String(byteBuffer.array());
                System.out.println(socketChannel.getRemoteAddress() + ": " + msg);
                send(socketChannel, msg);
            }
        } catch (IOException e) {
            try {
                System.out.println(socketChannel.getRemoteAddress() + "已离线");
                selectionKey.cancel();
                socketChannel.close();
            } catch (IOException ex) {
                e.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void send(SocketChannel socketChannel, String msg) {
        try {
            byte[] bytes = (socketChannel.getRemoteAddress() + ": " + msg).getBytes();
            for (SelectionKey selectionKey : selector.keys()) {
                Channel channel = selectionKey.channel();
                if (channel instanceof SocketChannel && channel != socketChannel) {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                    ((SocketChannel) channel).write(byteBuffer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        System.out.println("服务器已关闭");
        try {
            selector.close();
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.listen();
    }
}
