package top.cuteworld.sample.jobs.lateness;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 产生数据
 */
public class MockEventGen implements Runnable {

    private static ServerSocket serverSocket;

    public static void startServer() {
        try {
            serverSocket = new ServerSocket(9093);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Socket socket = null;
        try {
            socket = serverSocket.accept();
            System.out.println("it's ok.....");

            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

            Long current = System.currentTimeMillis();

            writeData(dOut, new MockEvent("t1", current));
            pause(1000);
            writeData(dOut, new MockEvent("t1", current + 999));
            pause(2000);
            writeData(dOut, new MockEvent("t1", current + 1999));
            pause(3000);
            writeData(dOut, new MockEvent("t1", current + 2999));

            pause(5000);//pause 5 sec and event time is 4 second , the lateness is 1second
            writeData(dOut, new MockEvent("t1", current + 3999));
            pause(15000);//pause 5 sec and event time is 4 second , the lateness is 1second
            writeData(dOut, new MockEvent("t1", current + 10 * 1000));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //close it
        }

    }

    private void pause(long pause) {
        try {
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeData(DataOutputStream dOut, MockEvent data) throws IOException {
        System.out.println("-----emit----> " + data);
        dOut.writeBytes(data.line() + "\n");
        dOut.flush();
    }
}
