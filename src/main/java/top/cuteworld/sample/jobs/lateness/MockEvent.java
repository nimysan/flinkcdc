package top.cuteworld.sample.jobs.lateness;

import org.mockito.Mock;

import java.util.Date;

/**
 *
 */
public class MockEvent {
    private String eventId;
    private Long eventTime;
    private Long emitTime;

    public MockEvent(String eventId, Long eventTime) {
        this.eventId = eventId;
        this.eventTime = eventTime;
        this.emitTime = System.currentTimeMillis();
    }

    public MockEvent(String source) {
        System.out.println("----received----" + source);
        String[] split = source.split("\\|");
        this.eventId = split[0];
        this.eventTime = Long.parseLong(split[1]);
        this.emitTime = Long.parseLong(split[2]);
    }

    public String line() {
        return this.eventId + "|" + Long.toString(this.eventTime) + "|" + Long.toString(this.emitTime);
    }

    @Override
    public String toString() {
        return "MockEvent{" + "eventId='" + eventId + '\'' + ", eventTime=" + new Date(eventTime) + ", emitTime=" + new Date(emitTime) + '}';
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
