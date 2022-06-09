package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

//spring提供的针对数据访问层的注解
@Repository
//针对es，我们继承ElasticsearchRepository并将其声明为泛型，其中的discussPost表示这个数据访问层要处理的实体类是谁，Integer代表的是实体类的主键的类型
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

}
