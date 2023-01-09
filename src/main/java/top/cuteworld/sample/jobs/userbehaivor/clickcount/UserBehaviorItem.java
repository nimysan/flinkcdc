package top.cuteworld.sample.jobs.userbehaivor;

import java.util.Date;

public class UserBehaviorItem {
    private Date eventTime;
    private String productId;
    private String userId;
    private String action;

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "UserBehaviorItem{" +
                "eventTime=" + eventTime +
                ", productId='" + productId + '\'' +
                ", userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}


