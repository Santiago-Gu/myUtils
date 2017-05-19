package com.gufeilong.androidutilslib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by santiago on 17-1-24.
 */
public class DateUitls {

    private static final  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** 返回一个时间文件名  **/
    public static String getNowTimeFileName(long times){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyyyyyMMddHHmmss");
        Date date = new Date(times);
        return sdf.format(date);
    }

    public static  String formatDate(Date date)throws ParseException{
        return sdf.format(date);
    }

    public static Date parse(String strDate) throws ParseException{

        return sdf.parse(strDate);
    }
}
