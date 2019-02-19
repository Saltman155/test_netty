package com.superywd.nio;

import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


public class NioEchoServer {

    private static final Logger logger = LoggerFactory.getLogger(NioEchoServer.class);
    // 通道管理器
    private Selector selector;
    /**
     * 获得一个ServerSocket通道，并对该通道做一些初始化的工作
     * @throws IOException
     */
    public void initServer(int port) throws IOException {
        // 获得一个ServerSocket通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // 设置通道为非阻塞
        serverChannel.configureBlocking(false);
        // 将该通道对应的ServerSocket绑定到port端口
        serverChannel.socket().bind(new InetSocketAddress(port));
        // 获得一个通道管理器
        this.selector = Selector.open();
        // 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
        // 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
     *
     * @throws IOException
     */
    public void listen() throws IOException {
        System.out.println("服务端启动成功！");
        // 轮询访问selector
        while (true) {
            // 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
            selector.select();
            // 获得selector中选中的项的迭代器，选中的项为注册的事件
            Iterator<?> ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                // 删除已选的key,以防重复处理
                ite.remove();
                handler(key);
            }
        }
    }

    /**
     * 处理请求
     *
     * @param key
     * @throws IOException
     */
    public void handler(SelectionKey key) throws IOException {
        // 客户端请求连接事件
        if (key.isAcceptable()) {
            handlerAccept(key);
            // 获得了可读的事件
        } else if (key.isReadable()) {
            handlerRead(key);
        }
    }

    /**
     * 处理连接请求
     *
     * @param key         产生待连接事件的channel的key值
     * @throws IOException
     */
    public void handlerAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        // 获得和客户端连接的通道
        SocketChannel channel = server.accept();
        // 设置成非阻塞
        channel.configureBlocking(false);
        // 在这里可以给客户端发送信息
        logger.info("接受了一个新的连接！"+channel.toString());
        channel.write(ByteBuffer.wrap("这是一个求和服务，只要输入两个整数，就可以求和...".getBytes()));
        // 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    /**
     * 处理读的事件
     *
     * @param key           产生可读事件的channel的key值
     * @throws IOException
     */
    public void handlerRead(SelectionKey key) throws IOException {
        // 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        // 创建读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = channel.read(buffer);
        if(read > 0){
            byte[] data = buffer.array();
            String msg = new String(data, CharsetUtil.UTF_8).trim();
            String[] numArray = msg.split(" +");
            BigInteger num1 = new BigInteger(numArray[0]);
            BigInteger num2 = new BigInteger(numArray[1]);
            logger.info("服务端接收到的计算请求为："+num1+" + "+num2);
            //回写数据
            ByteBuffer outBuffer = ByteBuffer.wrap(("您的计算结果为:"+num1.add(num2)).getBytes());
            channel.write(outBuffer);// 将消息回送给客户端
        }else{
            logger.info("客户端关闭...");
            key.cancel();
        }
    }

    /**
     * 启动服务端测试
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        NioEchoServer server = new NioEchoServer();
        server.initServer(10086);
        server.listen();
    }

}
