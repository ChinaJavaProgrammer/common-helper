package com.base.util.webbaseconfig.controller;

/**
 * @author dh
 * @param <T>
 */
public class ResponseListData<T> extends BaseRespData {

    protected T list;

    protected Long total;

    protected  Integer pageNum;

    protected Integer pageSize;

    protected Integer isPage = 0;

    public ResponseListData() {
    }

    public T getList() {
        return this.list;
    }

    public void setList(T list) {
        this.list = list;
    }


    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getIsPage() {
        return isPage;
    }

    public void setIsPage(Integer isPage) {
        this.isPage = isPage;
    }
}
