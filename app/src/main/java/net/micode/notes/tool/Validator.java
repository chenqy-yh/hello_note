package net.micode.notes.tool;

import android.content.Context;
import android.content.res.Resources;

public class Validator {

    //利用正则校验手机号是否是中国大陆手机号
    public static boolean isPhoneNum(String phoneNum) {
        String regex = "^((13[0-9])|(14[5,7,9])|(15[^4])|(16[6])|(17[0-8])|(18[0-9])|(19[1,8,9]))\\d{8}$";
        return phoneNum.matches(regex);
    }
    //判断传入的id参数合法
    public static boolean isValidResource(Context context,int resourceId) {
        try {
            String resourceType = context.getResources().getResourceTypeName(resourceId);
            // 判断资源类型是否为"drawable"
            return "drawable".equals(resourceType);
        } catch (Resources.NotFoundException e) {
            // 资源未找到，返回false
            return false;
        }
    }


}
