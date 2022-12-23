# 统计用户行为

[实践来源](https://blog.51cto.com/mapengfei/2580330)

## 需求
统计最近一小时的热门商品, 每5分钟更新一次

## 数据来源

用户数据埋点 - Kafka - Flink - Print(也可以是sink到MySQL)

## 数据埋点

```json
{
  "event_time": 123123412314,
  "product_id": "p1234",
  "action": "click",
  "user_id": "u_1233"
}
```