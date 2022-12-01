# 准备过程

[整体安装和体验教程](https://ververica.github.io/flink-cdc-connectors/release-2.1/content/quickstart/mysql-postgres-tutorial.html#enriching-orders-and-load-to-elasticsearch)

## 环境说明
1. 在AWS us-east-1上使用Linux 2
2. 与教程不同， 实验使用Flink 1.16版本

> account id: 390468416359  AMI(ami-0f4b62812662f73d1): FlinkCDC-Mysql-ES-AMI
## 安装docker

[install docker on AWS Linux 2](https://www.cyberciti.biz/faq/how-to-install-docker-on-amazon-linux-2/)

### 安装和设置docker

```bash
sudo yum update
sudo yum install docker
sudo usermod -a -G docker ec2-user
id ec2-user
newgrp docker

sudo systemctl enable docker.service
sudo systemctl start docker.service
sudo systemctl status docker.service
```

### 安装docker compose

```bash
# 1. Get pip3 
sudo yum install python3-pip
 
# 2. Then run any one of the following
sudo pip3 install docker-compose # with root access
sudo find / -name "docker-compose" -ls
```

## Flink CDC Get Started

[Documents](https://ververica.github.io/flink-cdc-connectors/release-2.1/content/quickstart/mysql-postgres-tutorial.html)

### 启动相关服务

```bash
cd flinkcdc/scripts
docker-compose up -d

[ec2-user@ip-172-31-23-252 ~]$ docker ps
CONTAINER ID   IMAGE                           COMMAND                  CREATED          STATUS          PORTS                                                                                  NAMES
64f5aa8f8e21   elastic/elasticsearch:7.6.0     "/usr/local/bin/dock…"   52 seconds ago   Up 28 seconds   0.0.0.0:9200->9200/tcp, :::9200->9200/tcp, 0.0.0.0:9300->9300/tcp, :::9300->9300/tcp   ec2-user_elasticsearch_1
407450644f0b   elastic/kibana:7.6.0            "/usr/local/bin/dumb…"   52 seconds ago   Up 28 seconds   0.0.0.0:5601->5601/tcp, :::5601->5601/tcp                                              ec2-user_kibana_1
fedd87be53f7   debezium/example-mysql:1.1      "docker-entrypoint.s…"   52 seconds ago   Up 28 seconds   0.0.0.0:3306->3306/tcp, :::3306->3306/tcp, 33060/tcp                                   ec2-user_mysql_1
e58079ddc2dd   debezium/example-postgres:1.1   "docker-entrypoint.s…"   52 seconds ago   Up 28 seconds   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp                                              ec2-user_postgres_1
```

启动好以后可以通过端口转发，在本地浏览器访问

[如何做端口转发](https://wangdoc.com/ssh/port-forwarding)

[配置端口转发](https://harttle.land/2022/05/02/ssh-port-forwarding.html)

```bash
#ssh -i us-east-1.pem ec2-user@34.207.59.225
ssh -L 56012:34.207.59.225:5601 ec2-user@34.207.59.225
```

> 本地访问kibana地址: http://localhost:56012 (访问正常设置成功)

### 在原始库中插入数据  数据准备

MySQL源库数据准备

```bash
# 进入数据库
docker-compose exec mysql mysql -uroot -p123456
#创建数据库和脚本
```

```iso92-sql
-- MySQL
CREATE DATABASE mydb;
USE mydb;
CREATE TABLE products (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(512)
);
ALTER TABLE products AUTO_INCREMENT = 101;

INSERT INTO products
VALUES (default,"scooter","Small 2-wheel scooter"),
       (default,"car battery","12V car battery"),
       (default,"12-pack drill bits","12-pack of drill bits with sizes ranging from #40 to #3"),
       (default,"hammer","12oz carpenter's hammer"),
       (default,"hammer","14oz carpenter's hammer"),
       (default,"hammer","16oz carpenter's hammer"),
       (default,"rocks","box of assorted rocks"),
       (default,"jacket","water resistent black wind breaker"),
       (default,"spare tire","24 inch spare tire");

CREATE TABLE orders (
  order_id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_date DATETIME NOT NULL,
  customer_name VARCHAR(255) NOT NULL,
  price DECIMAL(10, 5) NOT NULL,
  product_id INTEGER NOT NULL,
  order_status BOOLEAN NOT NULL -- Whether order has been placed
) AUTO_INCREMENT = 10001;

INSERT INTO orders
VALUES (default, '2020-07-30 10:08:22', 'Jark', 50.50, 102, false),
       (default, '2020-07-30 10:11:09', 'Sally', 15.00, 105, false),
       (default, '2020-07-30 12:00:30', 'Edward', 25.25, 106, false);
```

PostgreSQL数据准备

```bash
docker-compose exec postgres psql -h localhost -U postgres
```

```iso92-sql
-- PG
CREATE TABLE shipments (
  shipment_id SERIAL NOT NULL PRIMARY KEY,
  order_id SERIAL NOT NULL,
  origin VARCHAR(255) NOT NULL,
  destination VARCHAR(255) NOT NULL,
  is_arrived BOOLEAN NOT NULL
);
ALTER SEQUENCE public.shipments_shipment_id_seq RESTART WITH 1001;
ALTER TABLE public.shipments REPLICA IDENTITY FULL;
INSERT INTO shipments
VALUES (default,10001,'Beijing','Shanghai',false),
       (default,10002,'Hangzhou','Shanghai',false),
       (default,10003,'Shanghai','Hangzhou',false);
```


### 下载启动Flink服务
> 清注意版本
[CDC connector下载地址](https://github.com/ververica/flink-cdc-connectors/releases)

[ElasticSearch Connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/datastream/elasticsearch/)

```bash
#具体的下载地址可以从这里找到 https://downloads.apache.org/flink
wget https://downloads.apache.org/flink/flink-1.16.0/flink-1.16.0-bin-scala_2.12.tgz
tar xzvf flink-1.16.0-bin-scala_2.12.tgz

wget https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-elasticsearch7/1.16.0/flink-sql-connector-elasticsearch7-1.16.0.jar
wget https://repo1.maven.org/maven2/com/ververica/flink-sql-connector-postgres-cdc/2.2.1/flink-sql-connector-postgres-cdc-2.2.1.jar
wget https://repo1.maven.org/maven2/com/ververica/flink-sql-connector-mysql-cdc/2.2.1/flink-sql-connector-mysql-cdc-2.2.1.jar

mv *.jar flink-1.16.0/lib/ # 将jar包移动lib目录下

# 启动flink
#install java
sudo yum install java-1.8.0-openjdk
sudo yum install java-1.8.0-openjdk-devel
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.342.b07-1.amzn2.0.1.x86_64
export PATH=$JAVA_HOME/bin:$PATH
cd flink-1.16.0
./bin/start-cluster.sh

#如果需要外部网络访问web dashboard
修改 conf/flink-conf.yaml
rest.bind-address: 0.0.0.0 #localhost修改为0.0.0.0

ssh -L 56013:34.207.59.225:8081 ec2-user@34.207.59.225
```

> 本地访问Flink web dashboard地址: http://localhost:56013 (访问正常设置成功)

### 访问Flink SQLClient

```bash
 ./bin/sql-client.sh
 
 
    ______ _ _       _       _____  ____  _         _____ _ _            _  BETA
   |  ____| (_)     | |     / ____|/ __ \| |       / ____| (_)          | |
   | |__  | |_ _ __ | | __ | (___ | |  | | |      | |    | |_  ___ _ __ | |_
   |  __| | | | '_ \| |/ /  \___ \| |  | | |      | |    | | |/ _ \ '_ \| __|
   | |    | | | | | |   <   ____) | |__| | |____  | |____| | |  __/ | | | |_
   |_|    |_|_|_| |_|_|\_\ |_____/ \___\_\______|  \_____|_|_|\___|_| |_|\__|

        Welcome! Enter 'HELP;' to list all available commands. 'QUIT;' to exit.

Command history file path: /home/ec2-user/.flink-sql-history
Flink SQL>
 
-- Flink SQL                   
Flink SQL> SET execution.checkpointing.interval = 3s; #设置执行间隔
Flink SQL> SHOW CATALOGS;
default_catalog

Flink SQL> SHOW DATABASES;
default_database
```

以下是几个建表语句：
```iso92-sql
CREATE TABLE products (
    id INT,
    name STRING,
    description STRING,
    PRIMARY KEY (id) NOT ENFORCED
  ) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'localhost',
    'port' = '3306',
    'username' = 'root',
    'password' = '123456',
    'database-name' = 'mydb',
    'table-name' = 'products'
  );

CREATE TABLE orders (
   order_id INT,
   order_date TIMESTAMP(0),
   customer_name STRING,
   price DECIMAL(10, 5),
   product_id INT,
   order_status BOOLEAN,
   PRIMARY KEY (order_id) NOT ENFORCED
 ) WITH (
   'connector' = 'mysql-cdc',
   'hostname' = 'localhost',
   'port' = '3306',
   'username' = 'root',
   'password' = '123456',
   'database-name' = 'mydb',
   'table-name' = 'orders'
 );

CREATE TABLE shipments (
   shipment_id INT,
   order_id INT,
   origin STRING,
   destination STRING,
   is_arrived BOOLEAN,
   PRIMARY KEY (shipment_id) NOT ENFORCED
 ) WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'localhost',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'public',
   'table-name' = 'shipments'
 );

-- 这个表是连接到ES
CREATE TABLE enriched_orders (
   order_id INT,
   order_date TIMESTAMP(0),
   customer_name STRING,
   price DECIMAL(10, 5),
   product_id INT,
   order_status BOOLEAN,
   product_name STRING,
   product_description STRING,
   shipment_id INT,
   origin STRING,
   destination STRING,
   is_arrived BOOLEAN,
   PRIMARY KEY (order_id) NOT ENFORCED
 ) WITH (
     'connector' = 'elasticsearch-7',
     'hosts' = 'http://localhost:9200',
     'index' = 'enriched_orders'
 );

```
可以通过
http://localhost:56012/app/kibana#/discover?_g=(refreshInterval:(pause:!t,value:0),time:(from:now-15m,to:now))&_a=(columns:!(_source),index:'02b222e0-7129-11ed-999f-29886cf3f628',interval:auto,query:(language:kuery,query:''),sort:!(!(_score,desc)))
查看 enriched_orders 是否有正常进入

## 总结

1. Flink通过各种类型的Connector连接到各个源库；
2. Flink可以"动态"，"持续"的从源库按照规则同步数据到新库；
