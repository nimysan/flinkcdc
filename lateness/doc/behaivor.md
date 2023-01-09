# 统计用户行为

[实践来源](https://blog.51cto.com/mapengfei/2580330)

## 需求

统计最近一小时的热门商品, 每5分钟更新一次

## 数据来源

用户数据埋点 - Kafka - Flink - JDBC (也可以是sink到MySQL)

## 数据埋点

```json
{
  "event_time": 123123412314,
  "product_id": "p1234",
  "action": "click",
  "user_id": "u_1233"
}
```

### 设置MSK信息

机器：EC2： 54.152.225.130 （default vpc)

### 启动kafka管理界面

```bash
docker run -d --rm -p 9000:9000 \
    -e KAFKA_BROKERCONNECT=b-1.rdskafkareplication.qh77pm.c1.kafka.us-east-1.amazonaws.com:9092,b-3.rdskafkareplication.qh77pm.c1.kafka.us-east-1.amazonaws.com:9092,b-2.rdskafkareplication.qh77pm.c1.kafka.us-east-1.amazonaws.com:9092 \
    -e JVM_OPTS="-Xms32M -Xmx64M" \
    -e SERVER_SERVLET_CONTEXTPATH="/" \
    obsidiandynamics/kafdrop
#设置代理    
ssh -L 9000:54.152.225.130:9000 ec2-user@54.152.225.130    

```

### 每一分钟为一个tumbling window, 统计Product被点击的次数

代码参见： FlinkJobForProductClickStatistics

DataGen - Kafka Flink Job

-> Source(Kafka)  //从Kafka读取数据

-> KeyBy:ProductId //根据ProductId排序

->  window(TumblingEventTimeWindows.of(Time.minutes(1))) // 设置window窗口， 每一分钟一个窗口

-> aggregate(new CountAggregate(), new ProductViewCountWindowResult()) //统计并将结果放到Collector OUT

-> addSink(JdbcSink.sink)  //结果输出到JDBC数据库

```bash

ariaDB [uv]> select *  from uv_results;
+-----+-------+---------------------+---------------------+
| pid | count | last_modified_at    | window_end          |
+-----+-------+---------------------+---------------------+
| p_4 |     7 | 2022-12-27 16:50:01 | 2022-12-27 16:50:00 |
| p_5 |    10 | 2022-12-27 16:50:01 | 2022-12-27 16:50:00 |
| p_3 |     7 | 2022-12-27 16:50:01 | 2022-12-27 16:50:00 |
| p_9 |     6 | 2022-12-27 16:50:01 | 2022-12-27 16:50:00 |
| p_8 |     8 | 2022-12-27 16:50:01 | 2022-12-27 16:50:00 |
| p_2 |     4 | 2022-12-27 16:50:01 | 2022-12-27 16:50:00 |
| p_7 |     8 | 2022-12-27 16:50:01 | 2022-12-27 16:50:00 |
| p_6 |     3 | 2022-12-27 16:50:01 | 2022-12-27 16:50:00 |
| p_1 |     4 | 2022-12-27 16:50:01 | 2022-12-27 16:50:00 |
| p_5 |     9 | 2022-12-27 16:51:01 | 2022-12-27 16:51:00 |
| p_3 |     8 | 2022-12-27 16:51:01 | 2022-12-27 16:51:00 |
| p_9 |     5 | 2022-12-27 16:51:01 | 2022-12-27 16:51:00 |
| p_8 |     5 | 2022-12-27 16:51:01 | 2022-12-27 16:51:00 |
| p_2 |     6 | 2022-12-27 16:51:01 | 2022-12-27 16:51:00 |
| p_6 |     8 | 2022-12-27 16:51:01 | 2022-12-27 16:51:00 |
| p_4 |     5 | 2022-12-27 16:51:01 | 2022-12-27 16:51:00 |
| p_1 |     4 | 2022-12-27 16:51:01 | 2022-12-27 16:51:00 |
| p_7 |     7 | 2022-12-27 16:51:01 | 2022-12-27 16:51:00 |
| p_9 |     6 | 2022-12-27 16:52:00 | 2022-12-27 16:52:00 |
| p_1 |     8 | 2022-12-27 16:52:00 | 2022-12-27 16:52:00 |
| p_7 |     8 | 2022-12-27 16:52:00 | 2022-12-27 16:52:00 |
| p_6 |     9 | 2022-12-27 16:52:00 | 2022-12-27 16:52:00 |
| p_2 |     5 | 2022-12-27 16:52:00 | 2022-12-27 16:52:00 |
| p_8 |     2 | 2022-12-27 16:52:00 | 2022-12-27 16:52:00 |
| p_5 |     4 | 2022-12-27 16:52:00 | 2022-12-27 16:52:00 |
| p_3 |     6 | 2022-12-27 16:52:00 | 2022-12-27 16:52:00 |
| p_4 |     8 | 2022-12-27 16:52:00 | 2022-12-27 16:52:00 |
+-----+-------+---------------------+---------------------+

```

> 观察以上输出结果， 看window_end字段， 可以看到每分钟输出一次, 每次的row, 取决于keyBy后有多少window

### 问题是： 这个是统计每个时间窗口的数据， 如何将上个窗口的状态保存下来？ 继承到下个窗口呢？

#### 解决方案（1）：

> 鸡蛋不要都放在Flink里面

在sink的JDBC里面做聚合查询：
```bash
MariaDB [uv]> select pid,sum(count) from uv_results group by pid order by sum(count) desc;
+-----+------------+
| pid | sum(count) |
+-----+------------+
| p_5 |        104 |
| p_6 |         90 |
| p_7 |         88 |
| p_4 |         84 |
| p_9 |         83 |
| p_3 |         76 |
| p_8 |         75 |
| p_1 |         74 |
| p_2 |         67 |
+-----+------------+
```

#### 解决方案（2）：

> 万能的Flink
> 


### 错误解决

#### 问题： Kryo serializer scala extensions are not available.

```bash
22:40:59.516 [TumblingEventTimeWindows -> Sink: Print to Std. Out (1/1)#0] INFO org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer - Kryo serializer scala extensions are not available.
```