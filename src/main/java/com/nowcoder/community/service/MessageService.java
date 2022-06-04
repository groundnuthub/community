package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageService {

    @Resource
    private MessageMapper messageMapper;

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

}
