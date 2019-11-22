package com.jiangbin.longfiguredemo.longFigure;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.jiangbin.longfiguredemo.R;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.nanchen.compresshelper.CompressHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by jiangbin on 2019/11/20 11:57
 */
public class DrawLongPictureUtil extends LinearLayout {

    private final String TAG = "DrawLongPictureUtil";
    private Context context;
    private SharedPreferences sp;
    private Listener listener;

    private Info shareInfo;
    // 图片的url集合
    private List<String> imageUrlList;
    // 保存下载后的图片url和路径键值对的链表
    private LinkedHashMap<String, String> localImagePathMap;

    private View rootView;
    private LinearLayout llShareContainer;

    // 长图的宽度，默认为屏幕宽度
    private int longPictureWidth;
    // 最终压缩后的长图宽度
    private int finalCompressLongPictureWidth;
    // 长图两边的间距
    private int picMargin;

    // 被认定为长图的长宽比
    private int maxSingleImageRatio = 3;
    private int widthTop = 0;
    private int heightTop = 0;

    private int widthContent = 0;
    private int heightContent = 0;

    private int widthBottom = 0;
    private int heightBottom = 0;

    public DrawLongPictureUtil(Context context) {
        super(context);
        init(context);
    }

    public DrawLongPictureUtil(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawLongPictureUtil(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void removeListener() {
        this.listener = null;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        this.context = context;
        this.sp = context.getApplicationContext().getSharedPreferences(TAG, Context.MODE_PRIVATE);

        longPictureWidth = PhoneUtil.getPhoneWid(context);
        picMargin = 40;
        rootView = LayoutInflater.from(context).inflate(R.layout.layout_draw_canvas, this, false);
        initView();
    }

    private void initView() {
        llShareContainer = rootView.findViewById(R.id.llShareContainer);
        layoutView(llShareContainer);

    }

    /**
     * 手动测量view宽高
     */
    private void layoutView(View v) {
        int width = PhoneUtil.getPhoneWid(context);
        int height = PhoneUtil.getPhoneHei(context);

        v.layout(0, 0, width, height);
        int measuredWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int measuredHeight = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        v.measure(measuredWidth, measuredHeight);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
    }

    public void setData(Info info) {
        this.shareInfo = info;
        this.imageUrlList = shareInfo.getImageList();
        if (this.imageUrlList == null) {
            this.imageUrlList = new ArrayList<>();
        }
        if (localImagePathMap != null) {
            localImagePathMap.clear();
        } else {
            localImagePathMap = new LinkedHashMap<>();
        }
    }

    public void startDraw() {
        // 需要先下载全部需要用到的图片（用户头像、图片等），下载完成后再进行长图的绘制操作
        downloadAllPic();
    }


    private void downloadAllPic() {
        //下载方法，这里替换你选用的三方库，或者你可以选用我使用的这个三方库
        //implementation 'com.liulishuo.filedownloader:library:1.7.4'
        if (imageUrlList.isEmpty()) return;

        FileDownloader.setup(context);
        FileDownloadListener queueTarget = new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
            }

            @Override
            protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                localImagePathMap.put(task.getUrl(), task.getTargetFilePath());
                if (localImagePathMap.size() == imageUrlList.size()) {
                    //全部图片下载完成开始绘制
                    draw();
                }
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                listener.onFail();
                e.printStackTrace();
            }

            @Override
            protected void warn(BaseDownloadTask task) {
            }
        };

        for (String url : imageUrlList) {
            String storePath = BitmapUtil.getImgFilePath();
            FileDownloader.getImpl().create(url)
                    .setCallbackProgressTimes(0)
                    .setListener(queueTarget)
                    .setPath(storePath)
                    .asInQueueTask()
                    .enqueue();
        }

        FileDownloader.getImpl().start(queueTarget, true);
    }

    private Bitmap getLinearLayoutBitmap(LinearLayout linearLayout, int w, int h) {
        Bitmap originBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(originBitmap);
        linearLayout.draw(canvas);
        return BitmapUtil.resizeImage(originBitmap, longPictureWidth, h);
    }

    private int getAllImageHeight() {
        int height = 0;
        for (int i = 0; i < imageUrlList.size(); i++) {
            int[] wh = BitmapUtil.getWidthHeight(localImagePathMap.get(imageUrlList.get(i)));
            int w = wh[0];
            int h = wh[1];
            wh[0] = (longPictureWidth - (picMargin) * 2);
            wh[1] = (wh[0]) * h / w;
            float imgRatio = h / w;
            if (imgRatio > maxSingleImageRatio) {
                wh[1] = wh[0] * maxSingleImageRatio;
                Log.d(TAG, "getAllImageHeight w h > maxSingleImageRatio = " + Arrays.toString(wh));
            }
            height = height + wh[1];
        }
        height = height + PhoneUtil.dp2px(context, 6F) * imageUrlList.size();
        Log.d(TAG, "---getAllImageHeight = " + height);
        return height;
    }

    private Bitmap getSingleBitmap(String path) {
        int[] wh = BitmapUtil.getWidthHeight(path);
        final int w = wh[0];
        final int h = wh[1];
        wh[0] = (longPictureWidth - (picMargin) * 2);
        wh[1] = (wh[0]) * h / w;
        Bitmap bitmap = null;
        try {
            // 长图，只截取中间一部分
            float imgRatio = h / w;
            if (imgRatio > maxSingleImageRatio) {
                wh[1] = wh[0] * maxSingleImageRatio;
                Log.d(TAG, "getSingleBitmap w h > maxSingleImageRatio = " + Arrays.toString(wh));
            }
            bitmap = Glide.with(context).asBitmap().load(path).into(wh[0], wh[1]).get();
            // bitmap = Glide.with(context).asBitmap().load(path).apply(new RequestOptions().centerCrop().override(wh[0], wh[1])).submit().get();
            Log.d(TAG, "getSingleBitmap glide bitmap w h = " + bitmap.getWidth() + " , " + bitmap.getHeight());
            return bitmap;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        if (bitmap == null) {
            return null;
        }
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            paint.setAntiAlias(true);
            paint.setDither(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            Log.d(TAG, "getRoundedCornerBitmap w h = " + output.getWidth() + " × " + output.getHeight());
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    private int getAllTopHeightWithIndex(int index, int heightTop) {
        if (index < 0) {
            Log.d(TAG, "---getAllTopHeightWithIndex = " + heightTop);
            return heightTop;
        }
        int height = 0;
        for (int i = 0; i < index + 1; i++) {
            int[] wh = BitmapUtil.getWidthHeight(localImagePathMap.get(imageUrlList.get(i)));
            int w = wh[0];
            int h = wh[1];
            wh[0] = (longPictureWidth - (picMargin) * 2);
            wh[1] = (wh[0]) * h / w;
            float imgRatio = h / w;
            if (imgRatio > maxSingleImageRatio) {
                wh[1] = wh[0] * maxSingleImageRatio;
                Log.d(TAG, "getAllImageHeight w h > maxSingleImageRatio = " + Arrays.toString(wh));
            }
            height = height + wh[1];
        }
        height = heightTop + height + PhoneUtil.dp2px(context, 6F) * (index + 1);
        Log.d(TAG, "---getAllTopHeightWithIndex = " + height);
        return height;
    }

    private void draw() {

        int allBitmapHeight = 0;
        // 计算图片的总高度
        if (imageUrlList != null & imageUrlList.size() > 0) {
            allBitmapHeight = allBitmapHeight + getAllImageHeight() + PhoneUtil.dp2px(context, 16);
        }

        // 创建空白画布
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmapAll;
        try {
            bitmapAll = Bitmap.createBitmap(longPictureWidth, allBitmapHeight, config);
        } catch (Exception e) {
            e.printStackTrace();
            config = Bitmap.Config.RGB_565;
            bitmapAll = Bitmap.createBitmap(longPictureWidth, allBitmapHeight, config);
        }
        Canvas canvas = new Canvas(bitmapAll);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        if (imageUrlList != null && imageUrlList.size() > 0) {
            Bitmap bitmapTemp;
            int imageRadius = PhoneUtil.dp2px(context, 0F);
            for (int i = 0; i < imageUrlList.size(); i++) {
                // bitmapTemp = getSingleBitmap(localImagePathMap.get(imageUrlList.get(i)));
                String filePath = localImagePathMap.get(imageUrlList.get(i));
                bitmapTemp = BitmapUtil.fitBitmap(BitmapFactory.decodeFile(filePath),
                        longPictureWidth - picMargin * 2);
                Bitmap roundBitmap = getRoundedCornerBitmap(bitmapTemp, imageRadius);
                int top = 0;
                if (i == 0) {
                    top = heightTop + heightContent + PhoneUtil.dp2px(context, 13);
                } else {
                    top = getAllTopHeightWithIndex(i - 1, heightTop + heightContent + PhoneUtil.dp2px(context, 13));
                }
                if (roundBitmap != null) {
                    canvas.drawBitmap(roundBitmap, picMargin, top, paint);
                }
            }
        }


        // 生成最终的文件，并压缩大小，这里使用的是：implementation 'com.github.nanchen2251:CompressHelper:1.0.5'
        try {
            String path = BitmapUtil.saveBitmapBackPath(bitmapAll);
            float imageRatio = BitmapUtil.getImageRatio(path);
            if (imageRatio >= 10) {
                finalCompressLongPictureWidth = 750;
            } else if (imageRatio >= 5 && imageRatio < 10) {
                finalCompressLongPictureWidth = 900;
            } else {
                finalCompressLongPictureWidth = longPictureWidth;
            }
            String result;
            // 由于长图一般比较大，所以压缩时应注意OOM的问题，这里并不处理OOM问题，请自行解决。
            try {
                result = new CompressHelper.Builder(context).setMaxWidth(finalCompressLongPictureWidth)
                        .setMaxHeight(Integer.MAX_VALUE) // 默认最大高度为960
                        .setQuality(80)    // 默认压缩质量为80
                        .setFileName("长图_" + System.currentTimeMillis()) // 设置你需要修改的文件名
                        .setCompressFormat(Bitmap.CompressFormat.JPEG) // 设置默认压缩为jpg格式
                        .setDestinationDirectoryPath(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                                        + "/长图分享/")
                        .build()
                        .compressToFile(new File(path))
                        .getAbsolutePath();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();

                finalCompressLongPictureWidth = finalCompressLongPictureWidth / 2;
                result = new CompressHelper.Builder(context).setMaxWidth(finalCompressLongPictureWidth)
                        .setMaxHeight(Integer.MAX_VALUE) // 默认最大高度为960
                        .setQuality(50)    // 默认压缩质量为80
                        .setFileName("长图_" + System.currentTimeMillis()) // 设置你需要修改的文件名
                        .setCompressFormat(Bitmap.CompressFormat.JPEG) // 设置默认压缩为jpg格式
                        .setDestinationDirectoryPath(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                                        + "/长图分享/")
                        .build()
                        .compressToFile(new File(path))
                        .getAbsolutePath();
            }
            Log.d(TAG, "最终生成的长图路径为：" + result);
            if (listener != null) {
                listener.onSuccess(result);
            }
            //删除下载的图片
            BitmapUtil.getImgFilePathDelete();

        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFail();
            }
        }
    }

    public interface Listener {

        /**
         * 生成长图成功的回调
         *
         * @param path 长图路径
         */
        void onSuccess(String path);

        /**
         * 生成长图失败的回调
         */
        void onFail();
    }
}