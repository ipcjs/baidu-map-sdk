package com.baidu.location.service;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
    public static final String CoorType_GCJ02 = "gcj02";
    public static final String CoorType_BD09LL = "bd09ll";
    public static final String CoorType_BD09MC = "bd09";

    /***
     *61 ： GPS定位结果，GPS定位成功。
     *62 ： 无法获取有效定位依据，定位失败，请检查运营商网络或者wifi网络是否正常开启，尝试重新请求定位。
     *63 ： 网络异常，没有成功向服务器发起请求，请确认当前测试手机网络是否通畅，尝试重新请求定位。
     *65 ： 定位缓存的结果。
     *66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果。
     *67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果。
     *68 ： 网络连接失败时，查找本地离线定位时对应的返回结果。
     *161： 网络定位结果，网络定位定位成功。
     *162： 请求串密文解析失败。
     *167： 服务端定位失败，请您检查是否禁用获取位置信息权限，尝试重新请求定位。
     *502： key参数错误，请按照说明文档重新申请KEY。
     *505： key不存在或者非法，请按照说明文档重新申请KEY。
     *601： key服务被开发者自己禁用，请按照说明文档重新申请KEY。
     *602： key mcode不匹配，您的ak配置过程中安全码设置有问题，请确保：sha1正确，“;”分号是英文状态；且包名是您当前运行应用的包名，请按照说明文档重新申请KEY。
     *501～700：key验证失败，请按照说明文档重新申请KEY。
     */

    public static float[] EARTH_WEIGHT = {0.1f, 0.2f, 0.4f, 0.6f, 0.8f}; // 推算计算权重_地球
//    public static float[] MOON_WEIGHT = {0.0167f, 0.033f, 0.067f, 0.1f, 0.133f};
//    public static float[] MARS_WEIGHT = {0.034f, 0.068f, 0.152f, 0.228f, 0.304f};
    public static final int RECEIVE_TAG = 1;
    public static final int DIAGNOSTIC_TAG = 2;

    public static final String FILE_NAME = "share_data";

    public static final String SP_KEY_BACK_RETURN = "key_back_return";
    public static final String SP_HOME_BACK_RETURN = "home_back_return";
    public static final String SP_PERMISSION_DIALOG = "permission_dialog";
    public static final String SP_PRIVACY_DIALOG = "privacy_dialog";
    public static final String SP_PRIVACY_STATUS = "privacy_status";
    /**
     * 存取字符串
     *
     * @param mContext
     * @param key
     * @param value
     */
    public static void putString(Context mContext, String key, String value) {
        SharedPreferences mPreferences = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPreferences.edit();
        mEditor.putString(key, value);
        mEditor.commit();
    }

    /**
     * 查询某个key是否已经存在
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences mPreferences = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return mPreferences.contains(key);
    }

    public static String getString(Context context, String key) {
        SharedPreferences mPreferences = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);

        return mPreferences.getString(key, "0");
    }
}
