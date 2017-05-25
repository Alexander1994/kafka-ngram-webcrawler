package streamCluster;

import org.apache.kafka.streams.KafkaStreams;

public class NGramStreamMain {

    public static void main(String[] args) throws Exception {

        NGramStreamBuilder builder = new NGramStreamBuilder(2, "web-page-text", "ngram-counts");

        KafkaStreams streams = builder.getStream();
        streams.start();

        // usually the stream application would be running forever, for the prototype I am using finite data
        Thread.sleep(5000L);

        streams.close();
    }
}