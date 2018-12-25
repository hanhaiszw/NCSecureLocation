package connect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import utils.MyThreadPool;

public class MyServerSocket {
    private ServerSocket serverSocket;
    private List<MySocket> mySocketList = new ArrayList<>();


    public MyServerSocket(){

    }

    public void openServer(int port){
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("开启服务器失败");
        }
        System.out.println("开启服务器成功，等待客户端连接");
        MyThreadPool.execute(() -> {
            while(true){
                try {
                    Socket socket = serverSocket.accept();
                    MySocket mySocket = new MySocket(socket);

                    mySocketList.add(mySocket);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("服务器退出");
                    break;
                }
            }
        });
    }


    public void closeServer() {
        try {
            for (MySocket mySocket : mySocketList) {
                mySocket.close();
            }
            mySocketList.clear();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("serverSocket关闭");
    }
}
