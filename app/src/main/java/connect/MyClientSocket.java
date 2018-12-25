package connect;

import java.io.IOException;
import java.net.Socket;

public class MyClientSocket {
    private MySocket mySocket;

    public MyClientSocket() {

    }


    public boolean connect(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            mySocket = new MySocket(socket);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("MyClientSocket连接失败");
            return false;
        }
        System.out.println("MyClientSocket连接成功");
        return true;
    }

    public void disconnect() {
        if (mySocket != null) {
            mySocket.close();
        }
    }
}
