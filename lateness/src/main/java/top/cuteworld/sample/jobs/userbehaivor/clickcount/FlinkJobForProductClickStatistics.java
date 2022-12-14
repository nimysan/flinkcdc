package top.cuteworld.sample.jobs.userbehaivor.clickcount;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * 从kafka分析数据
 * <p>
 * SQL
 * <pre>
 *    create table uv_results
 * (
 * pid varchar(124) not null,
 * count bigint(20) not null,
 * last_modified_at datetime not null
 * );
 *
 * </pre>
 * <p>
 * <p>
 * Window说明文档
 * <ul>
 * <li>1. assigner（分配器）：如何将元素分配给窗口</li>
 * <li>2. function（计算函数）：为窗口定义的计算：其实是一个计算函数，完成窗口内容的计算。</li>
 * <li>3. triger（触发器）：在什么条件下触发窗口的计算</li>
 * <li>4. evictor（退出器）：定义从窗口中移除数据</li>
 * </ul>
 *
 * <strong>Note that using ProcessWindowFunction for simple aggregates such as count is quite inefficient. The next section shows how a ReduceFunction or AggregateFunction can be combined with a ProcessWindowFunction to get both incremental aggregation and the added information of a ProcessWindowFunction. </strong>
 */
public class FlinkJobForProductClickStatistics {

//    public final static String BROKERS = "b-3.rdskafkareplication.qh77pm.c1.kafka.us-east-1.amazonaws.com:9092,b-1.rdskafkareplication.qh77pm.c1.kafka.us-east-1.amazonaws.com:9092,b-2.rdskafkareplication.qh77pm.c1.kafka.us-east-1.amazonaws.com:9092";

    public final static String BROKERS = "localhost:9092";

    public static void main(String[] args) throws Exception {
        //env
        // 定义一个配置 import org.apache.flink.configuration.Configuration;包下
        Configuration configuration = new Configuration();

// 指定本地WEB-UI端口号
        configuration.setInteger(RestOptions.PORT, 8082);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        final ObjectMapper objectMapper = new ObjectMapper();

        //read kafka
        KafkaSource<UserBehaviorItem> source = KafkaSource.<UserBehaviorItem>builder()
                .setBootstrapServers(BROKERS)
                .setTopics("flink-user-behavior-data")
                .setGroupId("flink-statistics-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new UADeserializationSchema())
                .build();

        DataStreamSource<UserBehaviorItem> kafka_source = env
                .fromSource(source, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ZERO), "Kafka Source");

        kafka_source.setParallelism(1); //设置为1， 方便观察

        SingleOutputStreamOperator<ProductViewAccount> singleOutputStreamOperator = kafka_source
                .keyBy(UserBehaviorItem::getProductId)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new CountAggregate(), new ProductViewCountWindowResult());
        singleOutputStreamOperator.addSink(JdbcSink.sink(
                "insert into uv_results (pid, count, last_modified_at, window_end) values (?, ?, ?, ?)",
                (statement, pvc) -> {
                    statement.setString(1, pvc.getProductId());
                    statement.setLong(2, pvc.getCount());
                    statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    statement.setTimestamp(4, new Timestamp(pvc.getWindowEnd()));
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(2)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://localhost:3306/uv")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("test")
                        .withPassword("newpass")
                        .build()
        )).name("sinkuvjdbc").setParallelism(1);


        //执行
        System.out.println(env.getExecutionPlan());
        env.setParallelism(1).execute("用户浏览行为统计");
    }
}
