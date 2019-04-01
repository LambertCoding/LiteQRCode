package com.github.liteqrcode

import android.view.KeyEvent
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

/**
 * @param barcodeView  DecoratedBarcodeView
 * @param decodeSingle true 单次扫码    false 连续扫码
 * @author yu
 * @date 2019/4/1
 */
class ScanQRCodeHelper(
    private val barcodeView: DecoratedBarcodeView,
    private val decodeSingle: Boolean,
    private val callback: ScanCallback
) : BarcodeCallback {

    init {
        if (decodeSingle) {
            barcodeView.decodeSingle(this)
        } else {
            barcodeView.decodeContinuous(this)
        }
    }

    interface ScanCallback {
        fun onSuccess(qrCode: String)
    }

    private var isLight = false

    override fun barcodeResult(result: BarcodeResult) {
        // 解析结果
        callback.onSuccess(result.text)
    }

    override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
        // 扫描时闪动的检测点
        // no use
    }

    /**
     * 打开或者关闭闪光灯
     */
    fun toggleFlash() {
        isLight = if (!isLight) {
            barcodeView.setTorchOn()
            true
        } else {
            barcodeView.setTorchOff()
            false
        }
    }

    /**
     * Activity对应方法中调用
     */
    fun onResume() {
        barcodeView.resume()
    }

    /**
     * Activity对应方法中调用
     */
    fun onPause() {
        barcodeView.pause()
    }

    /**
     * 复写Activity的onKeyDown
     * return ScanQRCodeHelper.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
     */
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeView.onKeyDown(keyCode, event)
    }

}