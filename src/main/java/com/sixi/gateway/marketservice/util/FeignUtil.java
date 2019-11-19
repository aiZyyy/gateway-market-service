package com.sixi.gateway.marketservice.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author : chenjie
 * @date : 2019-08-30 11:0
 */
@Configuration
public class FeignUtil {

    public static String apply(String str) {
        return jsonToUpperUnderscore(str);
    }


    private static boolean isJsonArray(String str) {
        try {
            JSON.parseArray(str);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    private static boolean isJsonObject(String str) {
        try {
            JSON.parseObject(str);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    /**
     * 驼峰转下划线小写
     **/
    private static String jsonToUpperUnderscore(String str) {
        if (!isJsonArray(str) && !isJsonObject(str)) {
            return str;
        }
        if (isJsonArray(str)) {
            JSONArray array = JSON.parseArray(str);
            for (Object object : array) {
                jsonToUpperUnderscore(JSON.toJSONString(object));
            }
            return array.toJSONString();
        } else {
            JSONObject beforeJson = JSON.parseObject(str);
            JSONObject afterJson = new JSONObject();
            for (Map.Entry<String, Object> entry : beforeJson.entrySet()) {
                if (entry.getValue() != null) {
                    afterJson.put(entry.getKey(), doTypeDeal(jsonToUpperUnderscore(entry.getValue().toString())));
                }
            }
            return afterJson.toJSONString();
        }
    }

    /**
     * 插入值的类型处理
     **/
    private static Object doTypeDeal(String str) {
        if (str.startsWith("{") && str.endsWith("}")) {
            return JSON.parseObject(str);
        }
        if (str.startsWith("[") && str.endsWith("]")) {
            return JSON.parseArray(str);
        }
        return str;
    }
}
