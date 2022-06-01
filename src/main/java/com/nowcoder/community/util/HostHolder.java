package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {

     //ThreadLocal是以线程为key存取数据的，其中的set和get方法是线获取当前线程，然后使用map将线程存为key，再将线程中的数据存入对应的key中
     private ThreadLocal<User> users=new ThreadLocal<>();

     public User getUsers() {
          return users.get();
     }

     public void setUsers(User user) {
          users.set(user);
     }

     public void clear(){
          users.remove();
     }
}
