package top.cuteworld.sample.jobs.userbehaivor.clickcount;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

/**
 * 计数
 */
public class ProductViewCountWindowResult<IN, OUT, KEY, W extends Window> extends ProcessWindowFunction<Long, ProductViewAccount, String, TimeWindow> {

    @Override
    public void process(String key, ProcessWindowFunction<Long, ProductViewAccount, String, TimeWindow>.Context context, Iterable<Long> elements, Collector<ProductViewAccount> out) throws Exception {
        String productId = key;
        Long end = context.window().getEnd();
        Long count = elements.iterator().next();
        out.collect(new ProductViewAccount(productId, end, count));
    }
}
