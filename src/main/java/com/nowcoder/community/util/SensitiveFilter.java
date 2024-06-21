package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //敏感词替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        //类加载器是在类路径下加载资源
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-word.txt");
                BufferedReader read = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword = "";
            while ((keyword = read.readLine()) != null){
                this.addKeyword(keyword);
            }

        }catch (IOException e){
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    //在前缀树中添加一个敏感词
    public void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for(int i = 0; i < keyword.length(); i++){
            char c = keyword.charAt(i);
            TrieNode sonNode = tempNode.getSonNode(c);
            if(sonNode == null){
                //如果没有子节点的话，需要初始化一个子节点
                sonNode = new TrieNode();
                tempNode.addSonNode(c, sonNode);
            }

            //临时指针指向子节点，进入下一个循环
            tempNode = sonNode;

            if(i == keyword.length() - 1){
                //到最后一个字符了，要将关键词结束标识标记为true
                sonNode.setIsKeywordEnd(true);
            }
        }
    }


    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int end = 0;
        //存放结果
        StringBuilder sb = new StringBuilder();

        while(end < text.length()){
            char c = text.charAt(end);

            //先对符号进行处理
            if(isSymbol(c)){
                if(tempNode == rootNode){
                    //如果是符号，但是指针一位于根节点，则2、3都++
                    //且将该符号记录到输出结果中
                    begin++;
                    sb.append(c);
                }
                //如果是符号，但是指针一不位于根节点
                //说明此时特殊符号在敏感词之中，end++将其跳过
                end++;
                continue;
            }

            //到此处则c不为特殊符号
            //那就通过判断字符c是否在前缀树当中来确定是否要进行过滤
            //判断方法为看看c是否有下级节点
            tempNode = tempNode.getSonNode(c);
            if(tempNode == null){
                //第一种情况
                //说明没有下级节点，c不在前缀树中
                //将指针2、3都向前走一步
                end = ++begin;
                //且将该字符记录到结果中
                sb.append(c);
                //将指针1返回到根节点
                tempNode = rootNode;
            }else if(tempNode.isIsKeywordEnd()){
                //指针1指导了敏感词的最后节点
                //说明此时这个词需要进行屏蔽
                sb.append(REPLACEMENT);
                //同时2、3节点指向end的下一个节点
                begin = ++end;
                //将指针1返回到根节点
                tempNode = rootNode;
            }else{
                //剩下的情况就是end指向敏感词中间，如abc指向b，直接end++即可
                end++;
            }
        }

        //将最后一批字符串计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 是东亚文字范围
        //isAsciiAlphanumeric判断是否是大小写英文字母或阿拉伯数字
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    //定义一个内部类来作为节点
    private class TrieNode{
        //关键词结束标识，默认为false
        private boolean isKeywordEnd = false;

        //子节点(key是下级字符，value是下级节点)
        //因为可能有多个子节点，所以用map集合来装
        private Map<Character, TrieNode> sonNodes = new HashMap<>();

        public boolean isIsKeywordEnd(){
            return isKeywordEnd;
        }

        public void setIsKeywordEnd(boolean keywordEnd){
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSonNode(Character c, TrieNode node){
            sonNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSonNode(Character c){
            return sonNodes.get(c);
        }
    }
}
