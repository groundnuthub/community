package com.nowcoder.community.util;

public interface CommunityConstant {


    /**
     *激活成功
     */
    int ACTIVATION_SUCCESS=0;

    /**
     *重复激活
     */
    int ACTIVATION_REPEAT=1;

    /**
     *激活失败
     */
    int ACTIVATION_FAILURE=2;

    /**
     *默认情况下的登录凭证超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     *记住我下的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST=1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT=2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER=3;

    /**
     * 主题名称：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题名称：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题名称：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题名称：发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题名称：删帖
     */
    String TOPIC_DELETE = "delete";

    /**
     * 系统用户的id
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限：普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限：管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限：版主
     */
    String AUTHORITY_MODERATOR = "moderator";

}
