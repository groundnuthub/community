package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int begin, int limit);

    int selectCountByEntity(int entityType,int entityId);

    Comment selectCommentById(int id);

    int insertComment(Comment comment);

}
