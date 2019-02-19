package com.superywd.bio;

import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BioEchoServer
{

    private static final Logger logger = LoggerFactory.getLogger(BioEchoServer.class);

    private static ExecutorService newCachedThreadPool = null;


    public static void main(String[] args)throws Exception{
        //创建线程池
        newCachedThreadPool = Executors.newCachedThreadPool();
        start(10086);
    }

    public static void start(int port) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        try {
            while(true) {
                final Socket clientSocket = serverSocket.accept();
                logger.info("接受了一个新的连接！" + clientSocket);
                newCachedThreadPool.execute(() -> handler(clientSocket));
            }
        }
        catch (IOException e)   {
            logger.error(e.getMessage(),e);
        }
    }

    public static void handler(Socket socket) {
        OutputStream out;
        InputStream in;
        try {
            int len = 0;
            byte[] tmp = new byte[1024];
            out = socket.getOutputStream();
            in = socket.getInputStream();
            out.write("这是一个求和服务，只要输入两个整数，就可以求和...\n".getBytes(Charset.forName("UTF-8")));
            out.flush();
            while(true){
                BigInteger num1,num2;
                ByteArrayOutputStream array = new ByteArrayOutputStream();
                //读取数据（阻塞）
                while(true) {
                    len=in.read(tmp);
                    array.write(tmp,0,len);
                    // 从socket中获取的输入流不像文件输入流，无法通过len=-1的方
                    // 式来判断是否读取完毕了，我定义了一个\0放在字节流尾部来判断是否读取完毕
                    if('\0' == tmp[len-1] || array.size()>1024*1024){ break; }
                }
                try {
                    String str = array.toString("UTF-8").trim();
                    logger.info(str);
                    String[] numArray = str.split(" +");
                    num1 = new BigInteger(numArray[0]);
                    num2 = new BigInteger(numArray[1]);
                    logger.info("服务端接收到的计算请求为："+num1+" + "+num2);
                } catch (Exception e) {
                    out.write("您的输入有误，请重新输入...".getBytes(CharsetUtil.UTF_8));
                    out.flush();
                    continue;
                }
                out.write(("您的计算结果为:"+num1.add(num2)).getBytes(CharsetUtil.UTF_8));
                out.flush();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }finally {
            logger.info("socket关闭");
            try {
                socket.close();
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }
}

