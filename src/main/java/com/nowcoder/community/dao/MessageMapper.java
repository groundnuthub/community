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

    int insertMessage(Message message);

    int updateStatus(List<Integer> ids,int status);

    int deleteStatus(int id,int status);

    //查询某个主题下的最新的通知
    Message selectLatestNotice(int userId,String topic);

    //查询某个主题下共有多少条消息
    int selectNoticeCount(int userId,String topic);

    //查询某个主题下共有多少条未读消息
    int selectNoticeUnreadCount(int userId,String topic);

    //查询某个主题下的通知
    List<Message> selectNotice(int userId,String topic,int begin,int limit);

}
