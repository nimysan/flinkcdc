package top.cuteworld.sample.jobs.simplewebvisit;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Date;
import java.util.UUID;

/**
 * 基于Flink过滤出来使用Chrome访问页面的事件
 * <p>
 * https://zhuanlan.zhihu.com/p/335399517
 */
public class FilterChromeBrowserTask {

    public static void main(String[] args) throws Exception {
        // 1. 构建Stream执行上下文
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. 模拟构建流数据
        DataStreamSource<WebVisit> webLogDS = env.fromElements(
                new WebVisit("17.231.12.10", UUID.randomUUID().toString(), "page1.html", new Date(), "chrome")
                , new WebVisit("17.51.13.17", UUID.randomUUID().toString(), "page3.html", new Date(), "ie")
                , new WebVisit("191.21.51.85", UUID.randomUUID().toString(), "page1.html", new Date(), "chrome")
        );

        // 3. 只过滤出来使用chrome访问页面的事件
        SingleOutputStreamOperator<WebVisit> chromeWebLogDS = webLogDS.filter(new FilterFunction<WebVisit>() {
            public boolean filter(WebVisit webVisit) throws Exception {
                if (webVisit.getBrowser().equalsIgnoreCase("chrome")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        // 4. 打印数据
        chromeWebLogDS.print("heyhere");

        System.out.println(env.getExecutionPlan());
        // 5. 启动Flink应用
        env.execute();
    }
}