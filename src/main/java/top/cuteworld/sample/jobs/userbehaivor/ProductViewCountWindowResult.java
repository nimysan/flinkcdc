package top.cuteworld.sample.jobs.userbehaivor;

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

/**
 * 计数
 */
public class ProductViewCountWindowResult<IN, OUT, KEY, W extends Window> implements WindowFunction<Long, ProductViewAccount, String, TimeWindow> {

    @Override
    public void apply(String key, TimeWindow window, Iterable<Long> input, Collector<ProductViewAccount> out) throws Exception {
        String productId = key;
        Long end = window.getEnd();
        Long count = input.iterator().next();
        out.collect(new ProductViewAccount(productId, end, count));
    }
}
