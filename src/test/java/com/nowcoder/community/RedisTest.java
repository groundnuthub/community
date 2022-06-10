package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//用来加载main中的配置文件，让测试环境和正式环境相同
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings(){
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey,1);


        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash(){
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","张三");


        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    public void testLists(){
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);


        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,1));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));

    }

    @Test
    public void testSets(){
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey,"张三","李四","王五","赵六","汪柒");


        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSets(){
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey,"张三",10);
        redisTemplate.opsForZSet().add(redisKey,"李四",20);
        redisTemplate.opsForZSet().add(redisKey,"王五",30);
        redisTemplate.opsForZSet().add(redisKey,"赵六",40);
        redisTemplate.opsForZSet().add(redisKey,"汪柒",50);


        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"张三"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"张三"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,2));
    }

    @Test
    public void testKeys(){
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);
    }


    //多次访问同一个key，绑定这个key
    @Test
    public void testKey(){
        String redisKey ="test:count";

        BoundValueOperations operations=redisTemplate.boundValueOps(redisKey);

        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    //编程式事务
    @Test
    public void testTransaction(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                //启用事务
                operations.multi();

                operations.opsForSet().add(redisKey,"张三");
                operations.opsForSet().add(redisKey,"李四");
                operations.opsForSet().add(redisKey,"王五");

                System.out.println(operations.opsForSet().members(redisKey));

                //提交事务
                return operations.exec();
            }
        });
        System.out.println(obj);
    }

    //统计20万个重复数据的独立总数
    @Test
    public void testLoLog(){
        String redisKye = "test:hll:01";
        for (int i = 1; i <100000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKye,i);
        }
        for (int i = 1; i <100000 ; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKye,r);
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKye));
    }

    //将3组数据合并，再统计合并后的重复数据的独立总数
    @Test
    public void testHyperLoLog(){
        String redisKye = "test:hll:02";
        for (int i = 1; i <10000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKye,i);
        }
        String redisKye2 = "test:hll:03";
        for (int i = 5001; i <15000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKye2,i);
        }
        String redisKye3 = "test:hll:04";
        for (int i = 10001; i <20000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKye3,i);
        }
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,redisKye,redisKye2,redisKye3);

        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
    }

    //统计一组数据的boolean值
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";

        //记录
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);

        //查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,4));

        //统计
        //redisTemplate->execute->RedisCallback->doInRedis->传入redis链接connection
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
    }

    //统计三组数据的布尔值，并对三组数据做or运算
    @Test
    public void testBitMapOperation(){
        String redisKey = "test:bm:02";
        //记录
        redisTemplate.opsForValue().setBit(redisKey,0,true);
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,2,true);

        String redisKey2 = "test:bm:03";
        //记录
        redisTemplate.opsForValue().setBit(redisKey2,2,true);
        redisTemplate.opsForValue().setBit(redisKey2,3,true);
        redisTemplate.opsForValue().setBit(redisKey2,4,true);

        String redisKey3 = "test:bm:04";
        //记录
        redisTemplate.opsForValue().setBit(redisKey3,4,true);
        redisTemplate.opsForValue().setBit(redisKey3,5,true);
        redisTemplate.opsForValue().setBit(redisKey3,6,true);

        String redisKey4 = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey4.getBytes(),redisKey2.getBytes(),redisKey3.getBytes(),redisKey.getBytes());
                return connection.bitCount(redisKey4.getBytes());
            }
        });
        System.out.println(obj);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey4,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey4,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey4,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey4,3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey4,4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey4,5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey4,6));
    }

}
