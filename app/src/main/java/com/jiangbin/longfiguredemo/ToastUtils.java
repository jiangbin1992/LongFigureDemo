package com.jiangbin.longfiguredemo;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;


public class ToastUtils {
    /**
     * Toast提示
     *
     * @param message
     */
    private static Toast mToast;

    public static void showToast(Context context, String message) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        // mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

//    public static void showGravityToast(Context context, String message) {
//        Toast toast = null;
//        if (toast == null) {
//            toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
//        } else {
//            toast.setText(message);
//            toast.setDuration(Toast.LENGTH_SHORT);
//        }
//        LinearLayout layout = (LinearLayout) toast.getView();
//        TextView tv = (TextView) layout.getChildAt(0);
//        layout.setGravity(Gravity.CENTER);
//        tv.setGravity(Gravity.CENTER);
//        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//        tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
//        tv.setPadding((int) (context.getResources().getDimension(R.dimen.dp_50)), 0, (int) (context.getResources().getDimension(R.dimen.dp_50)), 0);
//        tv.setTextColor(context.getResources().getColor(R.color.white));
//        layout.setBackgroundResource(R.mipmap.img_jifen_bg);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
//    }

    private static Toast mToastCenter;

    public static void showToastCenter(Context context, String message) {
        if (mToastCenter == null) {
            mToastCenter = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
        } else {
            mToastCenter.setText(message);
            mToastCenter.setDuration(Toast.LENGTH_SHORT);
        }
        mToastCenter.setGravity(Gravity.CENTER, 0, 0);
        mToastCenter.show();
    }

}
