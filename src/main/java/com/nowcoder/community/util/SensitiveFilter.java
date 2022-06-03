package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

//将此方法交于容器托管，这个bean在服务开始启动时就被调用。Component与service和mapper不一样的是，Component使用来标注可以在各种层中使用的方法
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private final TrieNode rootNode= new TrieNode();

    //PostConstruct表示在当前方法的构造方法执行完之后立刻执行
    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader=new BufferedReader(new InputStreamReader(is))
        ){
            String keywords;
            while ((keywords=reader.readLine()) != null) {
                this.addKeyword(keywords);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败"+e.getMessage());
        }
    }

    //将敏感词传入前缀树种，设置前缀树节点
    public void addKeyword(String keywords){
        TrieNode tempNode = rootNode;
        for(int i=0;i<keywords.length();i++){
            char c = keywords.charAt(i);
            //先通过字符获取子节点
            TrieNode subNode=tempNode.getSubNodes(c);

            //判断是否已经有了一个子节点，没有的话则新增一个子节点
            if(subNode == null){
                //重新定义一个子节点
                subNode =new TrieNode();
                //给当前节点挂载一个子节点
                tempNode.addSubNodes(c,subNode);
            }

            //将当前节点指向子节点，进入下一层循环
            tempNode = subNode;

            //判断是否为最后一个字符，是的话设置结束标识
            if(i ==keywords.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     *过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)) {
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;

        //指针2
        int begin = 0;

        //指针3
        int position = 0;

        StringBuilder sb=new StringBuilder();

        while (begin<text.length()){
            char c = text.charAt(position);
            //判断c是否为特殊符号
            if(isSymbol(c)){
                //若指针1指向跟节点，将此符号计入结果，将指针2后移一个字符
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //只要出现特殊符号，指针3都会后移一个字符，将特殊字符忽略
                position++;
                continue;
            }

            //当指针2指向的第一个字符不是特殊字符时，将当前节点转为下级节点开始检测
            tempNode = tempNode.getSubNodes(c);
            if(tempNode == null){
                //以begin指针指向的字符为开头的字符不是敏感词,把begin所指向的字符追加进字符串中
                sb.append(text.charAt(begin));
                //将指针2后移一个字符，指针3移至指针2所指向的字符
                position=++begin;
                //把指针1归位至根节点
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd){
                //以begin指针指向的字符为开头的字符是敏感词,把begin所指向的字符替换成*号
                sb.append(REPLACEMENT);
                //将指针3后移一个字符，指针2移至指针3所指向的字符
                 begin=++position;
                //把指针1归位至根节点
                tempNode = rootNode;
            }else {
                if(position<text.length()-1){
                    position++;
                }else if(position==text.length()-1){
                    sb.append(text, begin, text.length()-1);
                    //将指针2后移一个字符，指针3移至指针2所指向的字符
                    begin=position;
                }
            }
        }
        return sb.toString();
    }

    //判断是不是特殊符号，isAsciiAlphanumeric是判断是不是普通文字，是的话返回true不是返回false
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF是东南亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c <0x2E80 || c>0x9FFF);
    }

    //前缀树
    private class TrieNode {

        //定义关键词结束标识
        private boolean isKeywordEnd = false;

        //定义当前节点的下级节点,key是char的包装类，用来存放下级节点的字符，value是用来存放节点，存放的类型为当前类
        private final Map<Character,TrieNode> subNodes =new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNodes(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        public TrieNode getSubNodes(Character c){
            return subNodes.get(c);
        }
    }

}
