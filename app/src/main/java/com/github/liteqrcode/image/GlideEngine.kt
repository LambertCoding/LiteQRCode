package com.github.liteqrcode.image

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.huantansheng.easyphotos.engine.ImageEngine

/**
 * @author yu
 * @date 2019/3/19
 */
class GlideEngine : ImageEngine {

   companion object {
       val instance by lazy { GlideEngine() }
   }

    /**
     * 加载图片到ImageView
     *
     * @param context   上下文
     * @param photoPath 图片路径
     * @param imageView 加载到的ImageView
     */
    override fun loadPhoto(context: Context, photoPath: String, imageView: ImageView) {
        GlideApp.with(context).load(photoPath).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
    }

    /**
     * 加载gif动图图片到ImageView，gif动图不动
     *
     * @param context   上下文
     * @param gifPath   gif动图路径
     * @param imageView 加载到的ImageView
     *
     *
     * 备注：不支持动图显示的情况下可以不写
     */
    override fun loadGifAsBitmap(context: Context, gifPath: String, imageView: ImageView) {
        GlideApp.with(context).asBitmap().load(gifPath).into(imageView)
    }

    /**
     * 加载gif动图到ImageView，gif动图动
     *
     * @param context   上下文
     * @param gifPath   gif动图路径
     * @param imageView 加载动图的ImageView
     *
     *
     * 备注：不支持动图显示的情况下可以不写
     */
    override fun loadGif(context: Context, gifPath: String, imageView: ImageView) {
        GlideApp.with(context).asGif().load(gifPath).transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }


    /**
     * 获取图片加载框架中的缓存Bitmap
     *
     * @param context 上下文
     * @param path    图片路径
     * @param width   图片宽度
     * @param height  图片高度
     * @return Bitmap
     * @throws Exception 异常直接抛出，EasyPhotos内部处理
     */
    @Throws(Exception::class)
    override fun getCacheBitmap(context: Context, path: String, width: Int, height: Int): Bitmap {
        return GlideApp.with(context).asBitmap().load(path).submit(width, height).get()
    }

}