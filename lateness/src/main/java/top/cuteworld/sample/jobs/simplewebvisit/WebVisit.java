package top.cuteworld.sample.jobs.simplewebvisit;

import java.util.Date;

/**
 * 网站访问数据
 */
public class WebVisit {
    // IP地址
    private String ip;
    // Cookie ID
    private String cookieId;
    // 访问页面Url
    private String pageUrl;
    // 页面打开时间
    private Date openTime;
    // 浏览器类型
    private String browser;

    public WebVisit() {
    }

    public WebVisit(String ip, String cookieId, String pageUrl, Date openTime, String browser) {
        this.ip = ip;
        this.cookieId = cookieId;
        this.pageUrl = pageUrl;
        this.openTime = openTime;
        this.browser = browser;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCookieId() {
        return cookieId;
    }

    public void setCookieId(String cookieId) {
        this.cookieId = cookieId;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    @Override
    public String toString() {
        return "WebVisit{" +
                "ip='" + ip + '\'' +
                ", cookieId='" + cookieId + '\'' +
                ", pageUrl='" + pageUrl + '\'' +
                ", openTime=" + openTime +
                ", browser='" + browser + '\'' +
                '}';
    }
}