package com.jiangbin.longfiguredemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_imageload.*

/**
 * Created by jiangbin on 2019/11/22 16:08
 */
class ImageLoadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imageload)
        val  url=intent.getSerializableExtra("url")

        Glide.with(this@ImageLoadActivity).load(url).into(iv)
    }
}

