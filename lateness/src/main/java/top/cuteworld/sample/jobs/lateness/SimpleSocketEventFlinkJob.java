package top.cuteworld.sample.jobs.lateness;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SimpleSocketEventFlinkJob {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleSocketEventFlinkJob.class);

    public static void main(String[] args) throws Exception {

        LOG.info("hello flink is running...");

        Configuration configuration = new Configuration();
        configuration.setInteger(RestOptions.PORT, 8082);
        configuration.setBoolean(RestOptions.ENABLE_FLAMEGRAPH, true);

        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment(configuration);

        //开启每300ms一次的checkout存储
        env.enableCheckpointing(300);
        //确保被执行一次
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        env.setParallelism(1);

        Thread emitData = new Thread(new SocketMockEventGenerator());
        emitData.start();

        Long current = System.currentTimeMillis();
        DataStreamSource<String> source = env.socketTextStream("localhost", 9093, "\r\n", 3000l);//从socket获得数据
        source.print("molihua");
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
