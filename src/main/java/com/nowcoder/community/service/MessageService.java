package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<Message> selectConversations(int userId, int begin, int limit){
        return messageMapper.selectConversations(userId,begin,limit);
    }

    public int selectConversationsCount(int userId){
        return messageMapper.selectConversationsCount(userId);
    }

    public List<Message> selectLetters(String conversationId,int begin,int limit){
        return messageMapper.selectLetters(conversationId,begin,limit);
    }

    public int selectLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    public int selectLetterUnreadCount(int userId,String conversationId){
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));

        return messageMapper.insertMessage(message);
    }

    public int updateStatus(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }

    public int deleteStatus(int id,int status){
        return messageMapper.deleteStatus(id,status);
    }


}
