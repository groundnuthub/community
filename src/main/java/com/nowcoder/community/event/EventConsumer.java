package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);

    @Resource
    private MessageService messageService;

    @Resource
    private DiscussPostService postService;

    @Resource
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    //ConsumerRecord:从kafka接收的键/值对,这还包括一个主题名称和一个从中接收记录的分区号，一个指向Kafka分区中的记录的偏移量以及一个由相应ProducerRecord标记的时间戳.
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null || record.value() ==null){
            logger.error("消息内容为空！");
            return;
        }

        //JSONObject.parseObject将（）内的数据转换成对应的对象
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
            if(event == null){
                logger.error("消息格式不正确！");
                return;
            }

            //发送通知
        Message message=new Message();

        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content =new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //消费发帖时间
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){

        if(record==null || record.value() ==null){
            logger.error("消息内容为空！");
            return;
        }

        //JSONObject.parseObject将（）内的数据转换成对应的对象
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式不正确！");
            return;
        }

        DiscussPost post = postService.findDiscussPost(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);

    }

}
