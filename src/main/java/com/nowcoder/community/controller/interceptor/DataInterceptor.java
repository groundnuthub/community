package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DataService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Resource
    private DataService dataService;

    @Resource
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //统计UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        //统计DAU
        User user =hostHolder.getUsers();
        if(user != null){
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
