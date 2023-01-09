package top.cuteworld.sample.jobs.watermark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class IotSensorDataIn5SecondJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //source
        Duration duration = Duration.ofSeconds(20l);

        DataStreamSource<IotData> sourceStream = env.addSource(new IotDataSource());

        WatermarkStrategy<IotData> ws = WatermarkStrategy.
                <IotData>forBoundedOutOfOrderness(duration).
                withTimestampAssigner(new SerializableTimestampAssigner<IotData>() {
                    @Override
                    public long extractTimestamp(IotData element, long recordTimestamp) {
                        return element.getTime();
                    }
                });
        sourceStream.assignTimestampsAndWatermarks(ws);


        //keyBy
        KeyedStream<IotData, Object> objectKeyedStream = sourceStream.keyBy((KeySelector<IotData, Object>) value -> value.getDeviceId());

        objectKeyedStream
                .window(TumblingEventTimeWindows.of(Time.of(5, TimeUnit.SECONDS)))
                .sum("number").print();

//        iotDataDataStreamSource.print();
        env.execute("IotGOGOEventCountSumJob");
    }
}
