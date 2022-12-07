package top.cuteworld.sample.jobs.batch.sample;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.walkthrough.common.sink.AlertSink;

public class BatchSampleJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //设置为批处理模式
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        DataStreamSource<String> source = env.fromElements("sample1", "sample2");

        SingleOutputStreamOperator<Object> map = source.map((MapFunction<String, Object>) value -> "Break:" + value);

        map.print().name("print");
        env.execute("hello-batch");
    }
}
