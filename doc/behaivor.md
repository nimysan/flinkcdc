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

## 设置MSK信息

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
