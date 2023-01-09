package top.cuteworld.sample.jobs.lateness;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Iterator;

/**
 * Window时长为10s
 * <p>
 * 1. 系统创建时间窗口为 20:10:00 - 20:10:10
 * 2. 中间产生两个元素， 每个Event的Timestamp: 20：10：03, 第二个Event的Timestamp: 20:10:09
 * 3. 不再有Event到来， 则第一个窗口永远不不会被触发计算并关闭
 * <p>
 * <p>
 * 如果想测试准确， 你必须多次测试， 确保你的EventTime的时间差不多是03s开始
 * 验证Lateness的例子
 * <p>
 * 文档参见： <a href="https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/operators/windows/#allowed-lateness">Allowed lateness<a/>
 */
public class FlinkJobLatenessJob {

    private static final Logger LOG = LoggerFactory.getLogger(FlinkJobLatenessJob.class);

    private static final int SAMPLE_LATENESS = 3;

    public static void main(String[] args) throws Exception {

        LOG.info("hello flink is running...");

        Configuration configuration = new Configuration();
        //8082 指定port
        configuration.setInteger(RestOptions.PORT, 8082);
        //启用flamegraph
        configuration.setBoolean(RestOptions.ENABLE_FLAMEGRAPH, true);

        configuration.setInteger(TaskManagerOptions.NUM_TASK_SLOTS, 1);

        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment(configuration);

        //开启每300ms一次的checkout存储
        env.enableCheckpointing(300);
        //确保被执行一次
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        env.setParallelism(1);
//        env.getConfig().setAutoWatermarkInterval(9000);
        Thread emitData = new Thread(new SocketMockEventGenerator(1000, 10 * 1000, Long.MAX_VALUE, false));
        emitData.start();

        Long current = System.currentTimeMillis();


        //从socket获得数据
        DataStreamSource<String> randomPauseSource = env.socketTextStream("localhost", 9093, "\r\n", -1).setParallelism(1);

        //延迟数据
        final OutputTag<MockEvent> lateOutputTag = new OutputTag<MockEvent>("late-data") {
        };


        SingleOutputStreamOperator<String> dataStream = randomPauseSource.map((MapFunction<String, MockEvent>) MockEvent::new)
                .assignTimestampsAndWatermarks(
                        //无延迟的Watermark, 使用event.getEventTime()作为Flink EventTime
                        WatermarkStrategy.<MockEvent>forBoundedOutOfOrderness(Duration.ZERO).withTimestampAssigner((event, timestamp) -> event.getEventTime())
                )

                //根据eventId分区
                .keyBy(MockEvent::getEventType)
                //每10s翻转一个时间窗口
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                //允许指定秒延迟
                .allowedLateness(Time.seconds(SAMPLE_LATENESS))
                .sideOutputLateData(lateOutputTag)
                .process(new ProcessWindowFunction<MockEvent, String, String, TimeWindow>() {
                    @Override
                    public void process(String s, ProcessWindowFunction<MockEvent, String, String, TimeWindow>.Context context, Iterable<MockEvent> elements, Collector<String> out) throws Exception {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");

                        String currentTime = sdf.format(new Date());
                        StringBuffer buffer = new StringBuffer("\r\n");

                        Long currentWatermark = context.currentWatermark();
                        String currentWatermarkString = "当前Watermark:" + sdf.format(new Date(currentWatermark));


                        buffer.append("Processed at --->" + currentTime + " Window (" + sdf.format(new Date(context.window().getStart())) + "---" + sdf.format(new Date(context.window().getEnd())) + "] " + currentWatermarkString + "---\r\n");
                        int count = 0;

                        long windowEnd = context.window().getEnd();
                        Iterator<MockEvent> iterator = elements.iterator();
                        while (iterator.hasNext()) {
                            MockEvent next = iterator.next();
                            buffer.append(next.toString() + " -> ");
                            Long eventComeToFlink = (windowEnd - next.getEmitTime());
                            if (eventComeToFlink >= 0) {
                                //在时间窗口内就到达了
                            } else {
                                //这是延迟到达的数据
                                String eventLateness = "比窗口关闭时间延迟:" + Math.abs(eventComeToFlink);
                                if (Math.abs(eventComeToFlink) > SAMPLE_LATENESS * 1000) {
//                                    buffer.append(eventLateness);
                                }
                                buffer.append(eventLateness);
                            }
                            buffer.append("\r\n");
                            count++;
                        }

                        out.collect(buffer.toString());
                    }
                }).returns(TypeInformation.of(String.class));


        dataStream.print("cuteworldsink").setParallelism(1);
        dataStream.getSideOutput(lateOutputTag).print("latedata");

        env.execute("测试Flink lateness 应用");

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
