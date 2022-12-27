package top.cuteworld.sample.jobs.lateness;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.Date;
import java.util.Iterator;

/**
 * 验证Lateness的例子
 * <p>
 * 文档参见： <a href="https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/operators/windows/#allowed-lateness">Allowed lateness<a/>
 */
public class FlinkJobLatenessJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        MockEventGen.startServer();
        Thread emitData = new Thread(new MockEventGen());
        emitData.start();

        Long current = System.currentTimeMillis();
        DataStreamSource<String> tuple2SingleOutputStreamOperator = env.socketTextStream("localhost", 9093);//从socket获得数据
        tuple2SingleOutputStreamOperator.map((MapFunction<String, MockEvent>) value -> new MockEvent(value))
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<MockEvent>noWatermarks().withTimestampAssigner((event, timestamp) -> event.getEventTime())
                )//指定使用event.f1字段作为EventTime
                .keyBy(MockEvent::getEventId)
                .window(TumblingEventTimeWindows.of(Time.seconds(3)))
//                .allowedLateness(Time.seconds(2))//允许迟到两秒钟
                .process(new ProcessWindowFunction<MockEvent, String, String, TimeWindow>() {
                    @Override
                    public void process(String s, ProcessWindowFunction<MockEvent, String, String, TimeWindow>.Context context, Iterable<MockEvent> elements, Collector<String> out) throws Exception {
                        StringBuffer buffer = new StringBuffer("\r\n");
                        int count = 0;
                        Iterator<MockEvent> iterator = elements.iterator();
                        while (iterator.hasNext()) {
                            MockEvent next = iterator.next();
                            buffer.append("count " + count + " element " + next + " - " + new Date(next.getEventTime()) + " --- " + new Date(next.getEmitTime()) + " window end at " + new Date(context.window().getEnd()) + "\r\n");
                            count++;
                        }
                        out.collect(buffer.toString());
                    }
                }).returns(TypeInformation.of(String.class)).print("sink-result").setParallelism(1);

        env.execute("try lateness");

        /**
         * 输出结果
         * <pre>
         * 分两个窗口(根据跑的起始时间， 会有随机的在一个或者两个的10S窗口）
         * sink-result>
         * count 0 element (t1,1672155475870) - Tue Dec 27 23:37:55 CST 2022 window end at Tue Dec 27 23:38:00 CST 2022
         * count 1 element (t1,1672155476870) - Tue Dec 27 23:37:56 CST 2022 window end at Tue Dec 27 23:38:00 CST 2022
         * count 2 element (t1,1672155477870) - Tue Dec 27 23:37:57 CST 2022 window end at Tue Dec 27 23:38:00 CST 2022
         * count 3 element (t1,1672155478870) - Tue Dec 27 23:37:58 CST 2022 window end at Tue Dec 27 23:38:00 CST 2022
         *
         * sink-result>
         * count 0 element (t1,1672155480370) - Tue Dec 27 23:38:00 CST 2022 window end at Tue Dec 27 23:38:10 CST 2022
         * count 1 element (t1,1672155480670) - Tue Dec 27 23:38:00 CST 2022 window end at Tue Dec 27 23:38:10 CST 2022
         *
         * </pre>
         */

    }
}
