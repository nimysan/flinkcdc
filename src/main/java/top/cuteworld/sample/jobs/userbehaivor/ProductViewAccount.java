package top.cuteworld.sample.jobs.userbehaivor;

public class ProductViewAccount {

    private String productId;
    private Long windowEnd;
    private Long count;

    public ProductViewAccount(String productId, Long windowEnd, Long count) {
        this.productId = productId;
        this.windowEnd = windowEnd;
        this.count = count;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Long getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(Long windowEnd) {
        this.windowEnd = windowEnd;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "ProductViewAccount{" +
                "productId='" + productId + '\'' +
                ", windowEnd=" + windowEnd +
                ", count=" + count +
                '}';
    }
}
