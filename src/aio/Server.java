package aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class Server {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5500;
    private AsynchronousServerSocketChannel serverSocketChannel;
    private final CopyOnWriteArrayList<AsynchronousSocketChannel> list = new CopyOnWriteArrayList<>();

    public Server() {
        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(HOST, PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        serverSocketChannel.accept(null, new AcceptHandler());
        System.out.println("服务器已开启");
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.listen();
    }

    class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

        @Override
        public void completed(AsynchronousSocketChannel result, Void attachment) {
            serverSocketChannel.accept(null, this);
            list.add(result);
            try {
                System.out.println(result.getRemoteAddress() + "已上线");
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                result.read(byteBuffer, byteBuffer, new ReadHandler(result));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            exc.printStackTrace();
        }
    }

    class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {
        private AsynchronousSocketChannel socketChannel;

        public ReadHandler(AsynchronousSocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            socketChannel.read(byteBuffer, byteBuffer, this);
            if (result > 0) {
                String msg = new String(attachment.array());
                try {
                    System.out.println(socketChannel.getRemoteAddress() + ": " + msg);
                    send(socketChannel, msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            list.remove(socketChannel);
            try {
                System.out.println(socketChannel.getRemoteAddress() + "已离线");
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            exc.printStackTrace();
        }

        public void send(AsynchronousSocketChannel socketChannel, String msg) {
            try {
                byte[] bytes = (socketChannel.getRemoteAddress() + ": " + msg).getBytes();
                for (AsynchronousSocketChannel socketChannel1 : list) {
                    if (socketChannel1 != socketChannel) {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                        socketChannel1.write(byteBuffer, null, new CompletionHandler<Integer, Void>() {
                            @Override
                            public void completed(Integer result, Void attachment) {

                            }

                            @Override
                            public void failed(Throwable exc, Void attachment) {

                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}