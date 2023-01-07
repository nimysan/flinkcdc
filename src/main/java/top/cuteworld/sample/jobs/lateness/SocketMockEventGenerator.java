package top.cuteworld.sample.jobs.lateness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * env.socketTextStream()
 * <p>
 * 事件对象生成器
 */
public class SocketMockEventGenerator implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SocketMockEventGenerator.class);

    private static ServerSocket serverSocket;

    /**
     * 默认间隔多久产生一个数据
     */
    private long internal = 1000l;

    /**
     * 随机暂停一段时间以产生差值
     */
    private long randomPause = 100l;

    /**
     * 产生多少个即停止， 默认100个
     */
    private long count = 10l;

    /**
     * 初始化的时候创建一个Socket服务
     */
    public SocketMockEventGenerator() {
        try {
            serverSocket = new ServerSocket(9093);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SocketMockEventGenerator(long internal, long randomPause, long count) {
        this();
        this.internal = internal;
        this.randomPause = randomPause;
        this.count = count;
    }

    public void run() {
        Socket socket = null;
        try {
            socket = serverSocket.accept();
            long generatedCount = 0;
            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
            while (generatedCount <= count) {
                writeData(dOut, new MockEvent("t1", System.currentTimeMillis()));
                generatedCount++;
                pause(internal);
            }
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
        if (randomPause >= 0) {
            long emitDelay = Math.round(Math.random() * randomPause);
            if (randomPause < randomPause / 2) {
                boolean randomMax = Math.random() < 0.5;
                emitDelay = randomMax ? randomPause - 1 : randomPause / 2; //如果暂停的时间太少， 则暂停预计的一般时间. 一般几率暂停给定的最大时间（插1ms)
            }
            Timer emitTimer = new Timer();
            emitTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SocketMockEventGenerator.this.emit(dOut, data);
                }
            }, emitDelay);
        } else {
            emit(dOut, data);
        }


    }

    private void emit(DataOutputStream dOut, MockEvent data) {
        try {
            data.setEmitTime(System.currentTimeMillis());
            LOG.info("-----emit----> " + data);
            dOut.writeBytes(data.line() + "\r\n");
            dOut.flush();
        } catch (Exception e) {
            LOG.error("fail to send data by socket", e);
        }
    }
}
