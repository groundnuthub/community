package com.nowcoder.community;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
//import org.elasticsearch.search.sort.SortBuilders;
//import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
//import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//用来加载main中的配置文件，让测试环境和正式环境相同
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper postMapper;

    @Autowired
    private DiscussPostRepository repository;

    @Resource
    private RestHighLevelClient restHighLevelClient;
    //ElasticsearchTemplate在es7以上无法使用
   /* @Resource
    private ElasticsearchTemplate elasticsearchTemplate;*/


    //判断某id的文档（数据库中的行）是否存在
    @Test
    public void testExist(){
        boolean exists = repository.existsById(1101);
        System.out.println(exists);
    }

    @Test
    public void testInsert(){
        repository.save(postMapper.selectDiscussPost(241));
        repository.save(postMapper.selectDiscussPost(242));
        repository.save(postMapper.selectDiscussPost(243));
    }

    @Test
    public void testInsertList(){
        /*repository.saveAll(postMapper.selectDiscussPosts(101,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(102,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(103,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(111,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(112,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(131,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(132,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(133,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(134,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(11,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(138,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(145,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(146,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(149,0,100));
        repository.saveAll(postMapper.selectDiscussPosts(154,0,100));*/
    }

    //修改数据（覆盖）
    @Test
    public void testUpdate(){
        DiscussPost post = postMapper.selectDiscussPost(231);
        post.setContent("我是新人，使劲灌水。");
        //如果标题传入null,es中的title会设为null
        post.setTitle("新人灌水");
        repository.save(post);
    }

    //修改一条数据
    //覆盖es里的原内容 与 修改es中的内容 的区别：String类型的title被设为null，覆盖的话，会把es里的该对象的title也设为null；UpdateRequest，修改后该对象的title不变
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("discusspost", "230");
        request.timeout("1s");
        DiscussPost post = postMapper.selectDiscussPost(231);
        post.setContent("我在干吗？》赶紧灌水.");
        post.setTitle("灌水达人");//es中的title会保存原内容不变
        request.doc(JSON.toJSONString(post), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    @Test
    public void testDelete(){
        repository.deleteById(231);
    }

    @Test
    public void testDeleteAll(){
        repository.deleteAll();
    }

     //7.15 直接没了 discussRepository.search方法，放弃
    //SearchQuery spring中已不含此方法，无法使用
//    @Test
//    public void testSearchByRepository() {
//        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
//                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                .withPageable(PageRequest.of(0, 10))
//                .withHighlightFields(
//                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
//                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
//                ).build();
//
//        // elasticTemplate.queryForPage(searchQuery, class, SearchResultMapper)
//        // 底层获取得到了高亮显示的值, 但是没有返回.
//
//        Page<DiscussPost> page = repository.search(searchQuery);
//        System.out.println(page.getTotalElements());
//        System.out.println(page.getTotalPages());
//        System.out.println(page.getNumber());
//        System.out.println(page.getSize());
//        for (DiscussPost post : page) {
//            System.out.println(post);
//        }
//    }

    //不带高亮的查询
    @Test
    public void noHighlightQuery() throws IOException {
        //构建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("discusspost");//discusspost是索引名，就是表名

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                //在discusspost索引的title和content字段中都查询“互联网寒冬”
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                //sort定义排序规则SortBuilders.fieldSort("")你要根据什么排序
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))//按照type对查询结果进行倒序排序
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))//按照score分数进行倒序排序
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))//按照createTime时间进行倒序排序
                //一个可选项，用于控制允许搜索的时间.超过60秒视为超时：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0)// 指定从哪条开始查询
                .size(10);// 需要查出的总记录条数

        //将搜索条件加入到搜索请求对象中
        searchRequest.source(searchSourceBuilder);
        //执行搜索(RequestOptions.DEFAULT表示同步),向ES发起http请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

//        System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> list = new LinkedList<>();

        /*//搜索结果
        SearchHits hits = searchResponse.getHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();*/

        //SearchHit表示搜索结果列表中的单个结果
        //searchResponse.getHits().getHits()表示获取搜索的出的文档
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
//            System.out.println(discussPost);
            list.add(discussPost);
        }
        System.out.println(list.size());
        for (DiscussPost post : list) {
            System.out.println(post);
        }
    }

    // 7.x版本弃用 template ,因此放弃
//    @Test
//    public void testSearchByTemplate() {
//        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
//                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                .withPageable(PageRequest.of(0, 10))
//                .withHighlightFields(
//                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
//                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
//                ).build();
//
//        Page<DiscussPost> page = elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
//            @Override
//            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
//                SearchHits hits = response.getHits();
//                if (hits.getTotalHits() <= 0) {
//                    return null;
//                }
//
//                List<DiscussPost> list = new ArrayList<>();
//                for (SearchHit hit : hits) {
//                    DiscussPost post = new DiscussPost();
//
//                    String id = hit.getSourceAsMap().get("id").toString();
//                    post.setId(Integer.valueOf(id));
//
//                    String userId = hit.getSourceAsMap().get("userId").toString();
//                    post.setUserId(Integer.valueOf(userId));
//
//                    String title = hit.getSourceAsMap().get("title").toString();
//                    post.setTitle(title);
//
//                    String content = hit.getSourceAsMap().get("content").toString();
//                    post.setContent(content);
//
//                    String status = hit.getSourceAsMap().get("status").toString();
//                    post.setStatus(Integer.valueOf(status));
//
//                    String createTime = hit.getSourceAsMap().get("createTime").toString();
//                    post.setCreateTime(new Date(Long.valueOf(createTime)));
//
//                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
//                    post.setCommentCount(Integer.valueOf(commentCount));
//
//                    // 处理高亮显示的结果
//                    HighlightField titleField = hit.getHighlightFields().get("title");
//                    if (titleField != null) {
//                        post.setTitle(titleField.getFragments()[0].toString());
//                    }
//
//                    HighlightField contentField = hit.getHighlightFields().get("content");
//                    if (contentField != null) {
//                        post.setContent(contentField.getFragments()[0].toString());
//                    }
//
//                    list.add(post);
//                }
//
//                return new AggregatedPageImpl(list, pageable,
//                        hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
//            }
//        });
//
//        System.out.println(page.getTotalElements());
//        System.out.println(page.getTotalPages());
//        System.out.println(page.getNumber());
//        System.out.println(page.getSize());
//        for (DiscussPost post : page) {
//            System.out.println(post);
//        }
//    }


    // 别人写的 大概看了下文档，没问题直接用了
    @Test
    public void highlightQuery() throws Exception{
        SearchRequest searchRequest = new SearchRequest("discusspost");//discusspost是索引名，就是表名
        Map<String,Object> res = new HashMap<>();

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //highlightBuilder.field定义高亮显示的具体内容
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        //多个单词高亮的话，要把这个设置为trues
        highlightBuilder.requireFieldMatch(false);
        //定义前置标签
        highlightBuilder.preTags("<span style='color:red'>");
        //定义后置标签
        highlightBuilder.postTags("</span>");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)// 指定从哪条开始查询
                .size(10)// 需要查出的总记录条数
                .highlighter(highlightBuilder);//高亮
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<DiscussPost> list = new ArrayList<>();
        //匹配到的总记录数
        long total = searchResponse.getHits().getTotalHits().value;
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            //getSourceAsString(),将取得的结果转换成string类型便于parseObject转换成对应的实体对象
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            // 处理高亮显示的结果
            //获取与标题有关的高亮显示的内容
            //HighlightField这是对某个字段的封装类型,其中数据是以数组的方式存储的
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
//            System.out.println(discussPost);
            list.add(discussPost);
        }
        res.put("list",list);
        res.put("total",total);
        if(res.get("list")!= null){
            for (DiscussPost post : list = (List<DiscussPost>) res.get("list")) {
                System.out.println(post);
            }
            System.out.println(res.get("total"));
        }
    }


}
