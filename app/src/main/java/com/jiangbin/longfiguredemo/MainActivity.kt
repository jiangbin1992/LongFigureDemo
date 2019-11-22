package com.jiangbin.longfiguredemo

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.widget.Toast
import com.jiangbin.longfiguredemo.longFigure.DrawLongPictureUtil
import com.jiangbin.longfiguredemo.longFigure.Info
import dev.DevUtils
import dev.utils.app.PermissionUtils
import dev.utils.app.toast.ToastTintUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var drawLongPictureUtil: DrawLongPictureUtil
    private var mCurrentSelectedPath = listOf<String>(
            "https://kksnail01-images.oss-cn-shanghai.aliyuncs.com/2019110721011723258.png",
            "https://kksnail01-images.oss-cn-shanghai.aliyuncs.com/2019110813284593284.png",
            "https://kksnail01-images.oss-cn-shanghai.aliyuncs.com/2019110712150637832.png"
    )//地址不对了自己换一下啊

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 初始化工具类
        DevUtils.init(this.applicationContext)
        drawLongPictureUtil = DrawLongPictureUtil(this@MainActivity)
        drawLongPictureUtil.setListener(object : DrawLongPictureUtil.Listener {
            override fun onSuccess(path: String?) {
                // 加载本地图片
                //val file = File(externalCacheDir!!.toString() + "/image.jpg")
                Toast.makeText(
                        getApplicationContext(), "长图生成完成",
                        Toast.LENGTH_LONG
                ).show()
                intent = Intent(this@MainActivity, ImageLoadActivity::class.java)
                intent.putExtra("url",path)
                startActivity(intent)


            }

            override fun onFail() {

            }

        })
        initView()
    }

    private fun initView() {

        bt.setOnClickListener {
            initPermission()

        }

    }

    private fun initPermission() {
        PermissionUtils.permission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        ).callBack(object : PermissionUtils.PermissionCallBack {
            override fun onGranted(permissionUtils: PermissionUtils?) {
                if (mCurrentSelectedPath.isNotEmpty()) {
                    val info = Info()
                    info.imageList = mCurrentSelectedPath
                    drawLongPictureUtil.setData(info)
                    drawLongPictureUtil.startDraw()
                }
            }

            override fun onDenied(permissionUtils: PermissionUtils?) {
                ToastTintUtils.error("请打开您的相机和存储权限")
            }
        }).request()


    }

}
