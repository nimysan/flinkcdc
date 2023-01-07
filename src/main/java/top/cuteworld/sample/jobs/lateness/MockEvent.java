package top.cuteworld.sample.jobs.lateness;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class MockEvent implements java.io.Serializable {
    private String eventId;
    private Long eventTime;
    private Long emitTime;

    private Long id;

    public MockEvent(String eventId, Long eventTime) {
        this.eventId = eventId;
        this.eventTime = eventTime;
        this.emitTime = System.currentTimeMillis();
        this.id = Math.abs(new Random().nextLong());
    }

    public MockEvent(String source) {
        System.out.println("----received----" + source);
        String[] split = source.split("\\|");
        this.eventId = split[0];
        this.eventTime = Long.parseLong(split[1]);
        this.emitTime = Long.parseLong(split[2]);
        this.id = Long.parseLong(split[3]);
    }

    public String line() {
        return this.eventId + "|" + Long.toString(this.eventTime) + "|" + Long.toString(this.emitTime) + "|" + this.id;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
        String late = Math.round((emitTime - eventTime) / 1000) + "s";
        return "MockEvent (lateness-" + late + ") - {" + "eventId='" + eventId + '\'' + ", eventTime=" + sdf.format(new Date(eventTime)) + ", emitTime=" + sdf.format(new Date(emitTime)) + '}';

    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    public Long getEmitTime() {
        return emitTime;
    }

    public void setEmitTime(Long emitTime) {
        this.emitTime = emitTime;
    }
}
