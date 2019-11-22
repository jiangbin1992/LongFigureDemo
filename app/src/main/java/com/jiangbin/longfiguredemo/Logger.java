package com.jiangbin.longfiguredemo;

import android.util.Log;

/**
 * 调试log工具类
 *
 * @author jaingbin
 */
public class Logger {


    public final static boolean debug = true;

    public static void d(String msg) {
        if (debug)
            Log.d(Logger.class.getName(), msg);
    }

    public static void i(String msg) {
        if (debug)
            Log.i(Logger.class.getName(), msg);

    }

    public static void e(String msg) {
        if (debug)
            Log.e(Logger.class.getName(), msg);
    }

    public static void v(String msg) {
        if (debug)
            Log.v(Logger.class.getName(), msg);
    }

    public static void w(String msg) {
        if (debug)
            Log.v(Logger.class.getName(), msg);
    }


    public static void i(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数

        if (debug) {
            int max_str_length = 2001 - tag.length();
            //大于4000时
            while (msg.length() > max_str_length) {
                Log.i(tag, msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.i(tag, msg);
        }
    }
}
