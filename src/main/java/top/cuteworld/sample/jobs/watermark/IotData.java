package top.cuteworld.sample.jobs.watermark;


public class IotData {

    public IotData() {

    }

    public IotData(String deviceId, Long time, String information) {
        this.time = time;
        this.deviceId = deviceId;
        this.information = information;
    }

    private Long time;
    private String deviceId;
    private String information;
    private Long number = 1l;

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    @Override
    public String toString() {
        return "IotData{" +
                "time=" + time +
                ", deviceId='" + deviceId + '\'' +
                ", information='" + information + '\'' +
                '}';
    }
}
