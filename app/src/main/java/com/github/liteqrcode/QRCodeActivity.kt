package com.github.liteqrcode

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.github.liteqrcode.codec.QRCodeDecoder
import com.github.liteqrcode.image.GlideEngine
import com.google.zxing.ResultPoint
import com.huantansheng.easyphotos.EasyPhotos
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_qrcode.*


class QRCodeActivity : BaseActivity() {

    private var isLight = false
    private val requestCode = 132

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        barcode_scanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (result.text == null) return

                barcode_scanner.setStatusText(result.text)
                Log.e("qwe", "result = ${result.text}")
                showTips(result.text)
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
        })

        tv_zxing_flashlight.setOnClickListener {
            if (!isLight) {
                barcode_scanner.setTorchOn()
                isLight = true
            } else {
                isLight = false
                barcode_scanner.setTorchOff()
            }
        }

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
        barcode_scanner.resume()
    }

    override fun onPause() {
        super.onPause()
        barcode_scanner.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcode_scanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

}
