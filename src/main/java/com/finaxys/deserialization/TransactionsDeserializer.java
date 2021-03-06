package com.finaxys.deserialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finaxys.model.Transactions;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * Each deserializer model class is used to retrieve model class objects from JSON information in the Kafka Topic
 */
public class TransactionsDeserializer implements Deserializer<Transactions> {

    @Override
    public void configure(Map<String, ?> var1, boolean var2) {
    }

    @Override
    public Transactions deserialize(String var1, byte[] var2) {
        ObjectMapper mapper = new ObjectMapper();
        Transactions transactions = null;
        try {
            transactions = mapper.readValue(var2, Transactions.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public void close() {
    }


}
