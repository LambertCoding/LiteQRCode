package com.github.liteqrcode

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.github.liteqrcode.codec.QRCodeDecoder
import com.github.liteqrcode.image.GlideEngine
import com.huantansheng.easyphotos.EasyPhotos
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_qrcode.*


class QRCodeActivity : BaseActivity() {

    private val requestCode = 132

    private lateinit var qrCodeHelper: ScanQRCodeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        qrCodeHelper = ScanQRCodeHelper(barcode_scanner, false, object : ScanQRCodeHelper.ScanCallback {
            override fun onSuccess(qrCode: String) {

                barcode_scanner.setStatusText(qrCode)
                Log.e("qwe", "result = $qrCode")
                showTips(qrCode)

            }
        })

        tv_zxing_flashlight.setOnClickListener { qrCodeHelper.toggleFlash() }

        tv_zxing_gallery.setOnClickListener {
            EasyPhotos.createAlbum(this, false, GlideEngine.instance).start(requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (RESULT_OK == resultCode && this@QRCodeActivity.requestCode == requestCode) {

            val bitmap = data?.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)?.get(0)

            Observable.just(bitmap)
                .map { QRCodeDecoder.syncDecodeQRCode(bitmap) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ showTips(it) }, { showTips("未识别二维码") })
                .also { addDisposable(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        qrCodeHelper.onResume()
    }

    override fun onPause() {
        super.onPause()
        qrCodeHelper.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return qrCodeHelper.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

}
