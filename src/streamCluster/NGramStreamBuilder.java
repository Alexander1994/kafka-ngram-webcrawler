package streamCluster;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.util.Arrays;
import java.util.Properties;

class NGramStreamBuilder {
    private final Properties props;
    private final String topicIn;
    private final String topicOut;
    private final int gramCount;

    NGramStreamBuilder(int gramCount, String topicIn, String topicOut) {
        this.topicIn = topicIn;
        this.topicOut = topicOut;
        this.gramCount = gramCount;
        props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "web-ngrams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // setting offset reset to earliest so that I can rerun with the same pre-loaded data
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }

    public KafkaStreams getStream() {
        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> source = builder.stream(this.topicIn);

        // Constants for use in building the pipeline
        final String newLineRegex = RegexChars.newLine();
        final String whiteSpaceRegex = RegexChars.whiteSpace();
        final int gramLength = this.gramCount;

        KTable<String, Long> counts = source
                .flatMapValues(new ValueMapper<String, Iterable<String>>() {
                    @Override
                    public Iterable<String> apply(String value) {
                        String onlyAlphabet = value.toLowerCase().replaceAll("[^a-z ]+", "");
                        String[] wordArr = onlyAlphabet.split(whiteSpaceRegex + "|" + newLineRegex);

                        if (gramLength > 1) {
                            final int nGramArrLength = wordArr.length - gramLength + 1;
                            String[] nGramArr = new String[nGramArrLength];
                            for (int i = 0; i < nGramArrLength; i++) {
                                String currNGram = "";
                                for (int j = 0; j < gramLength; j++) {
                                    currNGram += wordArr[i + j].trim();
                                    currNGram += (j < gramLength - 1) ? " " : "";
                                }
                                nGramArr[i] = currNGram;
                            }
                            return Arrays.asList(nGramArr);
                        } else {
                            int wordArrLength = wordArr.length;
                            for (int i=0; i< wordArrLength; i++) wordArr[i] = wordArr[i].trim();
                        }
                        return Arrays.asList(wordArr);
                    }
                }).map(new KeyValueMapper<String, String, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(String key, String value) {
                        return new KeyValue<>(value, value);
                    }
                })
                .groupByKey()
                .count("Counts")
                .filter((key, value) -> value > 1 );

        counts.to(Serdes.String(), Serdes.Long(), this.topicOut);
        counts.print(Serdes.String(), Serdes.Long());
        return new KafkaStreams(builder, this.props);
    }
}
