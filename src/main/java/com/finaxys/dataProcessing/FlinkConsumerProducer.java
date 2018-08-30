package com.finaxys.dataProcessing;

import com.finaxys.model.BlocksTransactions;
import com.finaxys.model.Test;
import com.finaxys.schema.BlocksTransactionsSchema;
import com.finaxys.schema.TestSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.util.Properties;

public class FlinkConsumerProducer {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        // configure Kafka consumer
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092"); // Broker default host:port
        props.setProperty("group.id", "flink-consumer"); // Consumer group ID

        FlinkKafkaConsumer011<BlocksTransactions> flinkBlocksTransactionsConsumer = new FlinkKafkaConsumer011<>(args[0], new BlocksTransactionsSchema(), props);
        flinkBlocksTransactionsConsumer.setStartFromEarliest();

        DataStream<BlocksTransactions> blocksTransactions = env.addSource(flinkBlocksTransactionsConsumer);

        //blocksTransactions.print();

        tableEnv.registerDataStream("blocksTransactionsTable", blocksTransactions);

        Table sqlResult
                = tableEnv.sqlQuery(
                        "SELECT block_hash, count(tx_hash) " +
                        "FROM blocksTransactionsTable " +
                        "GROUP BY block_hash");

        DataStream<Test> resultStream = tableEnv
                .toRetractStream(sqlResult, Row.class)
                .map(t -> {
                    Row r = t.f1;
                    String field2 = r.getField(0).toString();
                    long count = Long.valueOf(r.getField(1).toString());
                    return new Test(field2, count);
                })
                .returns(Test.class);

        resultStream.print();

        resultStream.addSink(new FlinkKafkaProducer011<>("localhost:9092", "TargetTopic", new TestSchema()));

        env.execute();

    }

}