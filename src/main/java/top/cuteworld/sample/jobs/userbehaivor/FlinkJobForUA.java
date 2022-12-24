package top.cuteworld.sample.jobs.userbehaivor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.IOException;
import java.sql.Time;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        final ObjectMapper objectMapper = new ObjectMapper();

        //read kafka
        KafkaSource<UserBehaviorItem> source = KafkaSource.<UserBehaviorItem>builder()
                .setBootstrapServers(BROKERS)
                .setTopics("flink-user-behavior-data")
                .setGroupId("flink-statistics-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new UADeserializationSchema())
                .build();
        DataStreamSource<UserBehaviorItem> kafka_source = env
                .fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source")
                //添加JDBC sink
                ;
        kafka_source
                .map(new MapFunction<UserBehaviorItem, ProductClickCount>() {
                    @Override
                    public ProductClickCount map(UserBehaviorItem value) throws Exception {
                        return new ProductClickCount(value.getProductId(), 1l);
                    }
                })
                .keyBy(ProductClickCount::getPid)

                .countWindow(100)
                .reduce(new ReduceFunction<ProductClickCount>() {
                    @Override
                    public ProductClickCount reduce(ProductClickCount left, ProductClickCount right) throws Exception {
                        return new ProductClickCount(left.getPid(), left.getCount() + right.getCount());
                    }
                }).addSink(JdbcSink.sink(
                        "insert into uv_results (pid, count, last_modified_at) values (?, ?, ?)",
                        (statement, pcc) -> {
                            statement.setString(1, pcc.getPid());
                            statement.setLong(2, pcc.getCount());
                            long now = System.currentTimeMillis();
                            Time sqlTime = new Time(now);
                            statement.setTime(3, sqlTime);
                        },
                        JdbcExecutionOptions.builder()
                                .withBatchSize(10)
                                .withBatchIntervalMs(200)
                                .withMaxRetries(2)
                                .build(),
                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                .withUrl("jdbc:mysql://localhost:3306/uv")
                                .withDriverName("com.mysql.cj.jdbc.Driver")
                                .withUsername("test")
                                .withPassword("newpass")
                                .build()
                ));
//                .print("---product-click-size");


        //执行
//        source
        env.execute("用户浏览行为统计");
    }
}
