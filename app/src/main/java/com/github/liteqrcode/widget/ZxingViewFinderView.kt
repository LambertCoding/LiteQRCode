package com.github.liteqrcode.widget

import android.content.Context
import android.graphics.*
import android.support.annotation.Nullable
import android.text.TextUtils
import android.util.AttributeSet
import com.github.liteqrcode.R
import com.journeyapps.barcodescanner.ViewfinderView


/**
 * @author yu
 * @date 2019/3/18
 */
class ZxingViewFinderView(context: Context, @Nullable attrs: AttributeSet) : ViewfinderView(context, attrs) {

    private val scannerBoundColor: Int
    private val scannerBoundWidth: Float // viewfinder width
    private val scannerBoundCornerColor: Int // viewfinder corner color
    private val scannerBoundCornerWidth: Float // viewfinder corner width
    private val scannerBoundCornerHeight: Float // viewfinder corner height
    private val scannerLaserResId: Int // laser resource
    private val scannerLaserHeight: Float // laser resource
    private val scannerLaserRepeatMode: Int // laser anim repeat mode
    private var scannerTipText: String? = null // tip text
    private val scannerTipTextSize: Float // tip text size
    private val scannerTipTextColor: Int // tip text color
    private val scannerTipTextMargin: Float // tip text margin between viewfinder
    private val scannerTipTextGravity: Int // tip text gravity
    private var scannerLaserBitmap: Bitmap? = null // laser bitmap
    private var scannerLaserTop: Float = 0f // laser top position

    private var laserMoveDirection = 1 // 扫描线移动的方向
    private val LASER_MOVE_DISTANCE_PER_UNIT_TIME = 4 // 扫描线移动的距离

    init {
        val attributes = getContext().obtainStyledAttributes(attrs, R.styleable.ZxingViewFinderView)

        scannerBoundColor = attributes.getColor(R.styleable.ZxingViewFinderView_scannerBoundColor, Color.WHITE)
        scannerBoundWidth = attributes.getDimension(R.styleable.ZxingViewFinderView_scannerBoundWidth, 0.5f)
        scannerBoundCornerColor =
            attributes.getColor(R.styleable.ZxingViewFinderView_scannerBoundCornerColor, Color.BLUE)
        scannerBoundCornerWidth =
            attributes.getDimension(R.styleable.ZxingViewFinderView_scannerBoundCornerWidth, 1.5f)
        scannerBoundCornerHeight =
            attributes.getDimension(R.styleable.ZxingViewFinderView_scannerBoundCornerHeight, 24f)

        scannerLaserResId = attributes.getResourceId(R.styleable.ZxingViewFinderView_scannerLaserResId, 0)
        scannerLaserHeight = attributes.getDimension(R.styleable.ZxingViewFinderView_scannerLaserHeight, 4f)
        scannerLaserRepeatMode = attributes.getInt(R.styleable.ZxingViewFinderView_scannerLaserRepeatMode, 0)

        scannerTipText = attributes.getString(R.styleable.ZxingViewFinderView_scannerTipText)
        scannerTipTextSize = attributes.getDimension(R.styleable.ZxingViewFinderView_scannerTipTextSize, 14f)
        scannerTipTextColor = attributes.getColor(R.styleable.ZxingViewFinderView_scannerTipTextColor, Color.WHITE)
        scannerTipTextMargin = attributes.getDimension(R.styleable.ZxingViewFinderView_scannerTipTextMargin, 40f)
        scannerTipTextGravity = attributes.getInt(R.styleable.ZxingViewFinderView_scannerTipTextGravity, 0)

        attributes.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        refreshSizes()
        if (framingRect == null || previewFramingRect == null) {
            return
        }
        val frame = framingRect
        val previewFrame = previewFramingRect
        val width = width
        val height = height // Draw the exterior
        drawExteriorDarkened(canvas, frame, width, height)
        if (resultBitmap != null) { // Draw the opaque result bitmap over the scanning rectangle
            paint.alpha = ViewfinderView.CURRENT_POINT_OPACITY
            canvas.drawBitmap(resultBitmap, null, frame, paint)
        } else {
            drawFrameBound(canvas, frame)
            drawFrameCorner(canvas, frame)
            drawLaserLine(canvas, frame)
            drawTipText(canvas, frame, width)
            drawResultPoint(canvas, frame, previewFrame) // Request another update at the animation interval,
            // but only repaint the laser line, // not the entire viewfinder mask.
            postInvalidate()
        }
    }

    /**
     * Draw a "laser scanner" line to show decoding is active
     */
    private fun drawLaserLine(canvas: Canvas, frame: Rect) {
        if (scannerLaserResId == 0) {
            paint.color = laserColor
            // 扫描线闪动
//            paint.alpha = ViewfinderView.SCANNER_ALPHA[scannerAlpha]
//            scannerAlpha = (scannerAlpha + 1) % ViewfinderView.SCANNER_ALPHA.size

            if (scannerLaserTop < frame.top) {
                scannerLaserTop = frame.top.toFloat()
                laserMoveDirection = 1
            }

            if (scannerLaserRepeatMode == 0) {
                // 重复
                if (scannerLaserTop > frame.bottom - scannerLaserHeight) {
                    scannerLaserTop = frame.top.toFloat()
                }
                laserMoveDirection = 1
            } else {
                // 倒放
                if (scannerLaserTop > frame.bottom - scannerLaserHeight) {
                    scannerLaserTop = frame.bottom - scannerLaserHeight
                    laserMoveDirection = -1
                }
            }

            canvas.drawRect(
                frame.left + 2F, scannerLaserTop,
                frame.right - 2F, scannerLaserTop + scannerLaserHeight,
                paint
            )

            scannerLaserTop += LASER_MOVE_DISTANCE_PER_UNIT_TIME * laserMoveDirection
        } else {
            if (scannerLaserBitmap == null) {
                scannerLaserBitmap = BitmapFactory.decodeResource(resources, scannerLaserResId)
            }
            if (scannerLaserBitmap != null) {
                val laserHeight = scannerLaserBitmap!!.height
                if (scannerLaserTop < frame.top) {
                    scannerLaserTop = frame.top.toFloat()
                    laserMoveDirection = 1
                }
                if (scannerLaserTop > frame.bottom - laserHeight) {
                    scannerLaserTop = (frame.bottom - laserHeight).toFloat()
                    laserMoveDirection = -1
                }
                val laserBitmapRect =
                    Rect(frame.left, scannerLaserTop.toInt(), frame.right, (scannerLaserTop + laserHeight).toInt())
                canvas.drawBitmap(scannerLaserBitmap!!, null, laserBitmapRect, paint)
                scannerLaserTop += LASER_MOVE_DISTANCE_PER_UNIT_TIME * laserMoveDirection
            }
        }
    }

    /**
     * Draw result points
     */
    private fun drawResultPoint(canvas: Canvas, frame: Rect, previewFrame: Rect) {
        val scaleX = frame.width() / previewFrame.width()
        val scaleY = frame.height() / previewFrame.height()
        val currentPossible = possibleResultPoints
        val currentLast = lastPossibleResultPoints
        val frameLeft = frame.left
        val frameTop = frame.top
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null
        } else {
            possibleResultPoints = ArrayList(5)
            lastPossibleResultPoints = currentPossible
            paint.alpha = ViewfinderView.CURRENT_POINT_OPACITY
            paint.color = resultPointColor
            for (point in currentPossible) {
                canvas.drawCircle(
                    frameLeft + (point.x * scaleX),
                    frameTop + (point.y * scaleY),
                    ViewfinderView.POINT_SIZE.toFloat(),
                    paint
                )
            }
        }
        if (currentLast != null) {
            paint.alpha = ViewfinderView.CURRENT_POINT_OPACITY / 2
            paint.color = resultPointColor
            val radius = ViewfinderView.POINT_SIZE / 2.0f
            for (point in currentLast) {
                canvas.drawCircle(
                    frameLeft + (point.x * scaleX),
                    frameTop + (point.y * scaleY),
                    radius,
                    paint
                )
            }
        }
    }

    /**
     * Draw tip text
     */
    private fun drawTipText(canvas: Canvas, frame: Rect, width: Int) {
        if (TextUtils.isEmpty(scannerTipText)) {
            scannerTipText = "提示"
        }
        paint.color = scannerTipTextColor
        paint.textSize = scannerTipTextSize
        val textWidth = paint.measureText(scannerTipText)
        val x = (width - textWidth) / 2 //根据 drawTextGravityBottom 文字在扫描框上方还是下文，默认下方
        val y =
            if (scannerTipTextGravity == 1) frame.bottom + scannerTipTextMargin else frame.top - scannerTipTextMargin
        canvas.drawText(scannerTipText!!, x, y, paint)
    }

    /**
     * Draw scanner frame bound * Note: draw inside frame
     */
    private fun drawFrameBound(canvas: Canvas, frame: Rect) {
        if (scannerBoundWidth <= 0) {
            return
        }
        paint.color = scannerBoundColor // top
        canvas.drawRect(
            frame.left.toFloat(),
            frame.top.toFloat(),
            frame.right.toFloat(),
            frame.top + scannerBoundWidth,
            paint
        ) // left
        canvas.drawRect(
            frame.left.toFloat(),
            frame.top.toFloat(),
            frame.left + scannerBoundWidth,
            frame.bottom.toFloat(),
            paint
        ) // right
        canvas.drawRect(
            frame.right - scannerBoundWidth,
            frame.top.toFloat(),
            frame.right.toFloat(),
            frame.bottom.toFloat(),
            paint
        ) // bottom
        canvas.drawRect(
            frame.left.toFloat(),
            frame.bottom - scannerBoundWidth,
            frame.right.toFloat(),
            frame.bottom.toFloat(),
            paint
        )
    }

    /**
     * Draw scanner frame corner
     */
    private fun drawFrameCorner(canvas: Canvas, frame: Rect) {
        if (scannerBoundCornerWidth <= 0 || scannerBoundCornerHeight <= 0) {
            return
        }
        paint.color = scannerBoundCornerColor // left top
        canvas.drawRect(
            frame.left - scannerBoundCornerWidth,
            frame.top - scannerBoundCornerWidth,
            frame.left + scannerBoundCornerHeight,
            frame.top.toFloat(),
            paint
        )
        canvas.drawRect(
            frame.left - scannerBoundCornerWidth,
            frame.top - scannerBoundCornerWidth,
            frame.left.toFloat(),
            frame.top + scannerBoundCornerHeight,
            paint
        ) // left bottom
        canvas.drawRect(
            frame.left - scannerBoundCornerWidth,
            frame.bottom + scannerBoundCornerWidth - scannerBoundCornerHeight,
            frame.left.toFloat(),
            frame.bottom + scannerBoundCornerWidth,
            paint
        )
        canvas.drawRect(
            frame.left - scannerBoundCornerWidth,
            frame.bottom.toFloat(),
            frame.left - scannerBoundCornerWidth + scannerBoundCornerHeight,
            frame.bottom + scannerBoundCornerWidth,
            paint
        ) // right top
        canvas.drawRect(
            frame.right + scannerBoundCornerWidth - scannerBoundCornerHeight,
            frame.top - scannerBoundCornerWidth,
            frame.right + scannerBoundCornerWidth,
            frame.top.toFloat(),
            paint
        )
        canvas.drawRect(
            frame.right.toFloat(),
            frame.top - scannerBoundCornerWidth,
            frame.right + scannerBoundCornerWidth,
            frame.top - scannerBoundCornerWidth + scannerBoundCornerHeight,
            paint
        ) // right bottom
        canvas.drawRect(
            frame.right + scannerBoundCornerWidth - scannerBoundCornerHeight,
            frame.bottom.toFloat(),
            frame.right + scannerBoundCornerWidth,
            frame.bottom + scannerBoundCornerWidth,
            paint
        )
        canvas.drawRect(
            frame.right.toFloat(),
            frame.bottom + scannerBoundCornerWidth - scannerBoundCornerHeight,
            frame.right + scannerBoundCornerWidth,
            frame.bottom + scannerBoundCornerWidth,
            paint
        )
    }

    /**
     * Draw the exterior (i.e. outside the framing rect) darkened
     */
    private fun drawExteriorDarkened(canvas: Canvas, frame: Rect, width: Int, height: Int) {
        paint.color = if (resultBitmap != null) resultColor else maskColor //top
        canvas.drawRect(0F, 0F, width.toFloat(), frame.top.toFloat(), paint) //left
        canvas.drawRect(0F, frame.top.toFloat(), frame.left.toFloat(), (frame.bottom + 1).toFloat(), paint) //right
        canvas.drawRect(
            (frame.right + 1).toFloat(),
            frame.top.toFloat(),
            width.toFloat(),
            (frame.bottom + 1).toFloat(),
            paint
        ) //bottom
        canvas.drawRect(0F, (frame.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), paint)
    }

}