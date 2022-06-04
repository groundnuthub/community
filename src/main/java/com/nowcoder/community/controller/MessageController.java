package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import javax.annotation.Resource;
import java.util.*;

@Controller
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @RequestMapping(path = "/letter/list" ,method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user=hostHolder.getUsers();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setCount(messageService.selectConversationsCount(user.getId()));

        //会话列表
        List<Message> messageList = messageService.selectConversations(user.getId(),page.getBegin(),page.getLimit());
        List<Map<String,Object>> conversation=new ArrayList<>();
        if(messageList != null){
            for (Message message:messageList) {
                Map<String,Object> map=new HashMap<>();
                map.put("conversation",message);

                map.put("letterCount",messageService.selectLetterCount(message.getConversationId()));

                map.put("unreadCount",messageService.selectLetterUnreadCount(user.getId(),message.getConversationId()));


                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.findUser(targetId));

                conversation.add(map);
            }
        }
        model.addAttribute("conversations",conversation);

        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}" ,method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Model model, Page page) {
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setCount(messageService.selectLetterCount(conversationId));

        List<Message> letterList = messageService.selectLetters(conversationId,page.getBegin(), page.getLimit());
        List<Map<String,Object>> letters =new ArrayList<>();
        if(letterList != null){
            for (Message message:letterList){
                Map<String,Object> map=new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUser(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);

        model.addAttribute("target",getLetterTarget(conversationId));

        //将私信列表中的未读消息提取出来，将其改成已读
        List<Integer> ids = getLetterIds(letterList);

        if(!ids.isEmpty()){
            messageService.updateStatus(ids);
        }

        return "/site/letter-detail";
    }

    @RequestMapping(path = "/letter/send" ,method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }
        Message message=new Message();
        message.setFromId(hostHolder.getUsers().getId());
        message.setToId(target.getId());
        if(message.getFromId()<message.getToId()){
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else {
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/letter/delete",method = RequestMethod.POST)
    @ResponseBody
    public String deleteStatus(int id){
        messageService.deleteStatus(id,2);
        return CommunityUtil.getJSONString(0);
    }


    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int d0 = Integer.parseInt(ids[0]);
        int d1 = Integer.parseInt(ids[1]);
        if(hostHolder.getUsers().getId() == d0){
            return userService.findUser(d1);
        }else {
            return userService.findUser(d0);
        }
    }

    private List<Integer> getLetterIds(List<Message> LetterList){
        List<Integer> ids=new ArrayList<>();

        if(LetterList!=null){
            for(Message message:LetterList){
                if(hostHolder.getUsers().getId()==message.getToId() && message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

}
