package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表，每个会话只显示最新的消息
    List<Message> selectConversations(int userId,int begin,int limit);

    int selectConversationsCount(int userId);

    List<Message> selectLetters(String conversationId,int begin,int limit);

    int selectLetterCount(String conversationId);

    int selectLetterUnreadCount(int userId,String conversationId);
}
