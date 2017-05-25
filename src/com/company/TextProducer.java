package com.company;

import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

class TextProducer {
    private Producer<String, String> producer;
    private String topicName;

    TextProducer(String topic) {
        Properties p = new Properties();
        p.put("bootstrap.servers", "localhost:9092");
        p.put("group.id", "test");
        p.put("acks", "all");
        p.put("retries", 0);
        p.put("batch.size", 16384);
        p.put("linger.ms", 1);
        p.put("buffer.memory", 33554432);
        p.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        p.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer<String, String> (p);
        this.topicName = topic;
    }

    public void send(int id, String text) {
        this.producer.send(new ProducerRecord<String, String>(this.topicName, Integer.toString(id), text));
    }
    public void close() {
        producer.close();
    }
}
