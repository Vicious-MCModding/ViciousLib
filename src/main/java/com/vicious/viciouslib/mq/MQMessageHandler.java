package com.vicious.viciouslib.mq;


import com.vicious.viciouslib.LibCFG;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MQMessageHandler {
    public static Map<String, Consumer<ByteArrayDataInput>> handlerMap = new HashMap<>();
    public static void register(String identifier, Consumer<ByteArrayDataInput> consumer){
        if(handlerMap.putIfAbsent(identifier,consumer) != null){
            throw new IllegalArgumentException("MQMessageHandler: " + identifier + " has already been registered");
        }
    }
    // Method takes input byte string of MQCore message and parses the byte arrays to call on individual tasks.
    public void handleMessage(byte[] body) {
        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(body);
            String application = in.readUTF();
            Consumer<ByteArrayDataInput> consumer = handlerMap.get(application);
            if(consumer != null) {
                consumer.accept(in);
            }
            else {
               if(LibCFG.getInstance().MQ404.getBoolean()){
                   System.err.println("Error404: MQHandler not found for: " + application);
               }
            }
        } catch (Exception ex) {
            System.err.println("Could not handle MQ message." + ex.getMessage());
            ex.printStackTrace();
        }
    }
}