package top.cuteworld.sample.jobs.userbehaivor;

/**
 * 计数
 */
public class ProductClickCount {
    public ProductClickCount(String pid, long count) {
        this.count = count;
        this.pid = pid;
    }

    private long count;
    private String pid;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "ProductClickCount{" +
                "count=" + count +
                ", pid='" + pid + '\'' +
                '}';
    }
}
