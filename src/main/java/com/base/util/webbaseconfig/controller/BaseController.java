package com.base.util.webbaseconfig.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.validation.ValidationException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 基础controller
 * @author dh
 */
public abstract class BaseController {


    public BaseController() {
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    private ResponseEntity<String> getRes(BaseRespData respData) {
        Gson gosn = (new GsonBuilder()).setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();
        return ResponseEntity.status(200).body(gosn.toJson(respData));
    }

    private ResponseEntity<String> getRes(int code, String message) {
        BaseRespData data = new BaseRespData();
        data.setCode(code);
        data.setMessage(message);
        return this.getRes(data);
    }

    protected ResponseEntity<String> error(String message) {
        return this.getRes(400, message);
    }

    protected ResponseEntity<String> error(int code, String message) {
        return this.getRes(code, message);
    }

    protected ResponseEntity<String> httpError(int code, String message) {
        BaseRespData data = new BaseRespData();
        data.setCode(code);
        data.setMessage(message);
        Gson gosn = (new GsonBuilder()).setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();
        return ResponseEntity.status(code).body(gosn.toJson(data));
    }

    protected ResponseEntity<String> success(String message) {
        return this.getRes(200, message);
    }

    protected <T> ResponseEntity<String> response(Object data) {
        if(data instanceof Page){
            return pageResponse((List)data);
        }

        ResponseData<T> res = new ResponseData();
        if (data != null) {
            res.setCode(200);
            res.setData((T) data);
            res.setMessage("restapi.ok");
            return this.getRes(res);
        } else {
            res.setCode(200);
            res.setMessage("restapi.ok");
            return this.getRes(res);
        }
    }

    protected <T> ResponseEntity<String> pageResponse(List<T> respData) {
        ResponseData<PageInfo<T>> res = new ResponseData();
        PageInfo<T> pageData = new PageInfo(respData);
        res.setCode(200);
        res.setMessage("restapi.ok");
        res.setData(pageData);
        return this.getRes(res);
    }

    protected <T> ResponseEntity<String> listResponse(Object data){

       if(data instanceof  Page){
           ResponseListData<List> res = new ResponseListData<>();
           res.setCode(200);
           res.setMessage("restapi.ok");
           PageInfo<T> pageData = new PageInfo((List) data);
           res.setPageNum(pageData.getPageNum());
           res.setPageSize(pageData.getPageSize());
           res.setTotal(pageData.getTotal());
           res.setIsPage(1);
           res.setList(pageData.getList());
           return this.getRes(res);
       }else if(data instanceof List){
           ResponseListData<List> res = new ResponseListData<>();
           res.setCode(200);
           res.setMessage("restapi.ok");
           res.setList((List)data);
           return this.getRes(res);
       }else {
           Object o = JSONObject.toJSON(data);
           ResponseListData<List> res = new ResponseListData<>();
           res.setList(Arrays.asList(o));
           return this.getRes(res);
       }
    }


    protected void validate(BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            StringBuffer sb = new StringBuffer();
            for (ObjectError error : bindingResult.getAllErrors()) {
                sb.append(error.getDefaultMessage()).append(",");
            }
            if(sb.length()>0){
                throw new ValidationException(sb.toString().substring(0,sb.length()-1));
            }
        }
    }

}
