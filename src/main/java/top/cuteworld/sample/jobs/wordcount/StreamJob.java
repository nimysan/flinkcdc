package top.cuteworld.sample.jobs.wordcount;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class StreamJob {
    public static void main(String[] args) throws Exception {

        //环境信息
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //数据来源是本机9999端口，换行符分隔，您也可以考虑将hostname和port参数通过main方法的入参传入
        DataStream<String> text = env.socketTextStream("localhost", 9999, "\n");
        //通过text对象转换得到新的DataStream对象，
        //转换逻辑是分隔每个字符串，取得的所有单词都创建一个WordWithCount对象
        DataStream<WordWithCount> windowCounts = text.flatMap(new FlatMapFunction<String, WordWithCount>() {
                    @Override
                    public void flatMap(String s, Collector<WordWithCount> collector) throws Exception {
                        for (String word : s.split("\\s")) {
                            collector.collect(new WordWithCount(word, 1L));
                        }
                    }
                })
                .keyBy("word")//key为word字段
                .window(SlidingProcessingTimeWindows.of(Time.seconds(10), Time.seconds(5)))    //五秒一次的翻滚时间窗口
                .reduce(new ReduceFunction<WordWithCount>() { //reduce策略
                    @Override
                    public WordWithCount reduce(WordWithCount a, WordWithCount b) throws Exception {
                        return new WordWithCount(a.word, a.count + b.count);
                    }
                });
        //单线程输出结果
        windowCounts.print().setParallelism(1);

        // 重启20次, 每5000ms
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(100, 5000));

        // 执行
        env.execute("WordCount Stream Application");
    }

}
