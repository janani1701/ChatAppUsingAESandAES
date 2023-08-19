package com.app.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {


    static Map<String, Map<String, LinkedBlockingQueue<MessageRequest>>> messageQueue = new ConcurrentHashMap<>();


    public static void offerMessage(MessageRequest messageRequest) {
        Map<String, LinkedBlockingQueue<MessageRequest>> q = messageQueue.get(messageRequest.getReceiver_name());


        if (q == null) {
            q = new ConcurrentHashMap<>();
            messageQueue.put(messageRequest.getReceiver_name(), q);
        }

        LinkedBlockingQueue<MessageRequest> messages = q.get(messageRequest.getSender_name());

        if (messages == null) {
            messages = new LinkedBlockingQueue<>();
            q.put(messageRequest.getSender_name(), messages);
        }

        DBUtils.insertMessage(messageRequest);

        messages.offer(messageRequest);

    }

    public static List<MessageRequest> pollMessages(String receiverName, String senderName) {

        Map<String, LinkedBlockingQueue<MessageRequest>> map = messageQueue.get(receiverName);

        if (map == null) {
            return DBUtils.getMessages(senderName, receiverName);
        }

        LinkedBlockingQueue<MessageRequest> mQ = map.get(senderName);
        if (mQ == null || mQ.isEmpty()) {
            return DBUtils.getMessages(senderName, receiverName);
        }

        List<MessageRequest> messages = new ArrayList<>();
        mQ.drainTo(messages);
        return messages;

    }
}
