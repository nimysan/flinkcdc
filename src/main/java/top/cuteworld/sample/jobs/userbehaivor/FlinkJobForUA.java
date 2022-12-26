package top.cuteworld.sample.jobs.userbehaivor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.sql.Timestamp;

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
 */
public class FlinkJobForUA {

//    public final static String BROKERS = "b-3.rdskafkareplication.qh77pm.c1.kafka.us-east-1.amazonaws.com:9092,b-1.rdskafkareplication.qh77pm.c1.kafka.us-east-1.amazonaws.com:9092,b-2.rdskafkareplication.qh77pm.c1.kafka.us-east-1.amazonaws.com:9092";

    public final static String BROKERS = "localhost:9092";

    public static void main(String[] args) throws Exception {
        //env
        // 定义一个配置 import org.apache.flink.configuration.Configuration;包下
        Configuration configuration = new Configuration();

// 指定本地WEB-UI端口号
        configuration.setInteger(RestOptions.PORT, 8082);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);


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
                .fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        kafka_source.setParallelism(1); //设置为1， 方便观察

        kafka_source
//                .map(new MapFunction<UserBehaviorItem, ProductClickCount>() {
//                    @Override
//                    public ProductClickCount map(UserBehaviorItem value) throws Exception {
//                        return new ProductClickCount(value.getProductId(), 1l);
//                    }
//                })
//                .setParallelism(1)
                .keyBy(UserBehaviorItem::getProductId)
                //时间翻转为1分钟， 允许5s延迟
                .window(TumblingEventTimeWindows.of(Time.minutes(1), Time.seconds(30)))
//                .trigger()
//                .countWindow(1000, 20)
//                .reduce()
//                .aggregate()
                .aggregate(new CountAggregate(), new ProductViewCountWindowResult());
//                .aggregate(new CountAggregate(), new).print();

//                .reduce(new ReduceFunction<ProductClickCount>() {
//                    @Override
//                    public ProductClickCount reduce(ProductClickCount left, ProductClickCount right) throws Exception {
//                        return new ProductClickCount(left.getPid(), left.getCount() + right.getCount());
//                    }
//                })
//                .addSink(JdbcSink.sink(
//                        "insert into uv_results (pid, count, last_modified_at) values (?, ?, ?)",
//                        (statement, pcc) -> {
//                            statement.setString(1, pcc.);
//                            statement.setLong(2, pcc.getCount());
//                            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
//                        },
//                        JdbcExecutionOptions.builder()
//                                .withBatchSize(1000)
//                                .withBatchIntervalMs(200)
//                                .withMaxRetries(2)
//                                .build(),
//                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
//                                .withUrl("jdbc:mysql://localhost:3306/uv")
//                                .withDriverName("com.mysql.cj.jdbc.Driver")
//                                .withUsername("test")
//                                .withPassword("newpass")
//                                .build()
//                ));
//                .print("---product-click-size");


        //执行
//        source
        env.execute("用户浏览行为统计");
    }
}
