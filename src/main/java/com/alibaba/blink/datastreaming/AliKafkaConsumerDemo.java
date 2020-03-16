package com.alibaba.blink.datastreaming;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.Serializable;
import java.util.Properties;

public class AliKafkaConsumerDemo implements Serializable {
    public static void main(String[] args) throws Exception {
        AliKafkaConsumerDemo aliKafkaConsumerDemo = new AliKafkaConsumerDemo();
        aliKafkaConsumerDemo.runExample();
    }
    public void runExample() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //加载kafka.properties
        Properties kafkaProperties =  JavaKafkaConfigurer.getKafkaProperties();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getProperty("bootstrap.servers"));
        //可更加实际拉去数据和客户的版本等设置此值，默认30s
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        //每次poll的最大数量
        //注意该值不要改得太大，如果poll太多数据，而不能在下次poll之前消费完，则会触发一次负载均衡，产生卡顿
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 30);
        //当前消费实例所属的消费组，请在控制台申请之后填写
        //属于同一个组的消费实例，会负载消费消息
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getProperty("group.id"));

        FlinkKafkaConsumer010 kafkaConsumer = new FlinkKafkaConsumer010<>(kafkaProperties.getProperty("topic"), new SimpleStringSchema(),props);

        env.addSource(kafkaConsumer)
                .print()
                .setParallelism(1);
        env.execute("alikafkaconsumerdemo");
    }
}