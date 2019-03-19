package com.github.liteqrcode

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import com.github.liteqrcode.codec.QRCodeEncoder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart.setOnClickListener {
            startActivity(Intent(this, QRCodeActivity::class.java))
        }

        btnCreate.setOnClickListener {
            val content = etContent.text.toString().trim()
            if (content.isEmpty()) {
                showTips("请输入需要生成的信息")
            } else {
                Observable.create<Bitmap> {
                    val logo = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                    it.onNext(logo)
                    it.onComplete()
                }
                    .map { QRCodeEncoder.syncEncodeQRCode(content, 400, Color.BLACK, Color.WHITE, it) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { imageView.setImageBitmap(it) }
                    .also { addDisposable(it) }
            }
        }
    }

}
