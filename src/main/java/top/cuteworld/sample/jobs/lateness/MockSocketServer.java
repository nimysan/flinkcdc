package top.cuteworld.sample.jobs.lateness;

import java.io.IOException;
import java.net.ServerSocket;

public class MockSocketServer {

    public static void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(9093);
                    serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
