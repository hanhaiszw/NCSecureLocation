package connect;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import utils.MyThreadPool;

public class MySocket {
    private Socket socket;

    public MySocket(Socket socket){
        this.socket=socket;
    }


    private void init() {
        try {
            //无论数据多少 都立即发送
            socket.setTcpNoDelay(true);
            //设置read的超时值
            //socket.setSoTimeout(3 * 1000);
            // 当超时15秒没有读到数据  就断开吧
            //socket.setSoTimeout(15 * 1000);
            // 开启接收线程
            MyThreadPool.execute(receiveRunnable);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private Runnable receiveRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                byte[] data = new byte[4096];
                while (true) {
                    if (socketIsActive()) {
                        // 处理接收到的消息

                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("client端socket关闭");
            } finally {
                //关闭socket操作
                close();
            }
        }
    };



    // 检查socket是否还可用
    public boolean socketIsActive(){
        return socket.isConnected() && !socket.isClosed();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
