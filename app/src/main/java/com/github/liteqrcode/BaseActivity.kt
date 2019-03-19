package com.github.liteqrcode

import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * @author yu
 * @date 2019/3/19
 */
open class BaseActivity : AppCompatActivity() {

    private val disposables by lazy { CompositeDisposable() }

    protected fun addDisposable(disposable: Disposable) = disposables.add(disposable)

    protected fun showTips(message: String?) {
        if (!message.isNullOrEmpty())
            Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
