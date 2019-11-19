package com.sixi.gateway.marketservice.constant;

/**
 * @Author: ZY
 * @Date: 2019/8/22 13:37
 * @Version 1.0
 * @Description:
 */
public class AuthConast {

    /**
     * 无效的签名方法
     */
    public static final int RESP_CD_INVALID_SIGNATURE_TYPE = 10021;

    /**
     * 缺少必要的签名参数
     */
    public static final int RESP_CD_MISSING_SIGNATURE_PARAM = 10022;

    /**
     * 无效签名
     */
    public static final int RESP_CD_INVALID_SIGNATURE = 10023;

    /**
     * 非法时间截
     */
    public static final int RESP_CD_INVALID_TIMESTAMP = 10024;

    /**
     * nonce 已使用
     */
    public static final int RESP_CD_NONCE_USED = 10025;

    /**
     * 非法的APPID
     */
    public static final int RESP_CD_INVALID_APP_ID = 10026;

    /**
     * APPID已停止授权
     */
    public static final int RESP_CD_REFUSED_APP_ID = 10027;

    /**
     * 无效的方法路径
     */
    public static final int RESP_CD_INVALID_PATH = 10028;

    /**
     * 无效的请求类型
     */
    public static final int RESP_CD_METHOS_TYPE = 10029;

    /**
     * 无效的秘钥
     */
    public static final int CODE_NOT_OBTAINED_SECRET_KEY = 10030;

    /**
     * 无效签名类型
     */
    public static final String RESP_MSG_INVALID_SIGNATURE_TYPE = "无效的签名类型";

    /**
     * 缺少必要签名参数
     */
    public static final String RESP_MSG_MISSING_SIGNATURE_PARAM = "缺少必要签名参数";
    /**
     * 无效签名
     */
    public static final String RESP_MSG_INVALID_SIGNATURE = "无效签名，请检查签名算法或公私钥";

    /**
     * 时间非法
     */
    public static final String RESP_MSG_INVALID_TIMESTAMP = "系统时间不正确,请检查手机时间是否正确";


    public static final String RESP_MSG_NONCE_USED = "nonce已使用";

    public static final String RESP_MSG_INVALID_APP_ID = "不存在的APP_ID";

    public static final String RESP_MSG_REFUSED_APP_ID = "APP_ID已停止授权";

    /**
     * 无效的方法路径
     */
    public static final String RESP_MSG_INVALID_PATH = "无效的方法路径";

    /**
     * 无效的请求类型
     */
    public static final String RESP_MSG_METHOS_TYPE = "网关服务不支持GET请求";

    /**
     * 无效的请求类型
     */
    public static final String MSG_NOT_OBTAINED_SECRET_KEY = "未获取到秘钥";
}
