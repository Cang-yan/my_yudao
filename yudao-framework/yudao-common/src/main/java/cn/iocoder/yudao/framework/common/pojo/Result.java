package cn.iocoder.yudao.framework.common.pojo;

/**
 * @description:
 * @author: Yang Xin
 * @time: 2022/10/24 11:16
 */

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Result {

    /**
     * 错误码
     *
     * @see ErrorCode#getCode()
     */
    private Integer code;

    /*
     * 返回数据
     * */
    private Object data;

    /**
     * 错误提示，用户可阅读
     *
     * @see ErrorCode#getMsg() ()
     */
    private String msg;


    public Result(Integer code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public Map<String, Object> map() {
        Map<String, Object> statusMap = new LinkedHashMap();
        statusMap.put("code", this.getCode());
        statusMap.put("data", this.getData());
        statusMap.put("msg", this.getMsg());

        return statusMap;
    }


    public static Result.Builder builder() {
        return new Result.Builder();
    }

    public static Result.Builder builder(int code, String message) {
        return new Result.Builder(code, message);
    }

    public static Result.Builder builder(int code, String message, Object dataValue, Long totalCount) {
        return (new Result.Builder(code, message)).addDataValue(dataValue).addDataCount(totalCount);
    }

    public static Result.Builder successBuilder() {
        return new Result.Builder(GlobalErrorCodeConstants.SUCCESS.getCode(), GlobalErrorCodeConstants.SUCCESS.getMsg());
    }

    public static Result.Builder successFailedInnerBuilder(String message) {
        return new Result.Builder(GlobalErrorCodeConstants.SUCCESS.getCode(), message);
    }

    public static Result.Builder failedRpcBuilder(ErrorCode errorCode) {
        return new Result.Builder(errorCode.getCode(), errorCode.toString());
    }

    public static Result.Builder failedBuilder() {
        return new Result.Builder(GlobalErrorCodeConstants.FAILED.getCode(), GlobalErrorCodeConstants.FAILED.getMsg());
    }

    public static Result.Builder failedBuilder(int code,String msg) {
        return new Result.Builder(code, msg);
    }

    public static Result.Builder failedBuilder(ErrorCode errorCode) {
        return new Result.Builder(errorCode.getCode(), errorCode.getMsg());
    }

    public static class Builder {
        private int code;
        private String msg;
        private Map<String, Object> data;

        private Builder() {
            this.data = new LinkedHashMap();
        }

        private Builder(int code, String message) {
            this.data = new LinkedHashMap();
            this.code = code;
            this.msg = message;
        }

        private Builder(int code, String message, Map<String, Object> data) {
            this.data = new LinkedHashMap();
            this.code = code;
            this.msg = message;
            this.data = data;
        }


        public Result.Builder addStatus(int code) {
            this.code = code;
            return this;
        }

        public Result.Builder addMessage(String message) {
            this.msg = message;
            return this;
        }


        public Result.Builder addData(String key, Object data) {
            this.data.put(key, data);
            return this;
        }

        public Result.Builder addDataValue(Object data) {
            return this.addData("value", data);
        }

        public Result.Builder addDataCount(Long count) {
            return this.addData("count", count);
        }


        public Result build() {
            return new Result(this.code, this.data, this.msg);
        }

        public Map<String, Object> map() {
            return (new Result(this.code, this.data, this.msg)).map();
        }
    }
}

