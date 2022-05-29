package com.nowcoder.community.entity;

public class Page {

    //当前页码
    private int current = 1;
    //显示上限
    private int limit = 10;
    //数据总量（用于计算总页数）
    private int count;
    //查询路径（用于复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1 && limit<=100) {
            this.limit = limit;
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        if(count>=0){
            this.count = count;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    //获取当前页的起始行
    public int getBegin(){
        return (current*limit)-limit;
    }

    //用来获取总页数
    public int getTotal(){
        if(count % limit==0){
            return count/limit;
        }else{
            return count/limit + 1;
        }
    }

    //获取起始页码
    public int getFrom(){
        int from=current-2;
        return from <1 ? 1 : from;
    }

    //获取结束页码
    public int getTo(){
        int to=current + 2;
        int total=getTotal();
        return to > total ? total : to;
    }
}
