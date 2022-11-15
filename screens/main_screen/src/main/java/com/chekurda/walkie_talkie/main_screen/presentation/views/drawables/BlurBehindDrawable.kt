package com.chekurda.walkie_talkie.main_screen.presentation.views.drawables

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.chekurda.design.custom_view_tools.utils.dp
import java.util.concurrent.CountDownLatch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class BlurBehindDrawable(
    private val behindView: View,
    private val parentView: View?
) {
    private var queue: DispatchQueue? = null
    private var blurredBitmapTmp: Array<Bitmap?>? = null
    private var backgroundBitmap: Array<Bitmap?>? = null
    private var renderingBitmap: Array<Bitmap?>? = null
    private var renderingBitmapCanvas: Array<Canvas?>? = null
    private var backgroundBitmapCanvas: Array<Canvas?>? = null
    private var blurCanvas: Array<Canvas?>? = null
    private var processingNextFrame = false
    private var invalidate = true
    private var blurAlpha = 0f
    private var show = false
    private var error = false
    private var animateAlpha = true
    private val DOWN_SCALE = 6f
    private var lastH = 0
    private var lastW = 0
    private var toolbarH = 0
    private var wasDraw = false
    private var skipDraw = false
    private var panTranslationY = 0f
    var blurBackgroundTask = BlurBackgroundTask()
    var emptyPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    var errorBlackoutPaint = Paint()

    init {
        errorBlackoutPaint.color = Color.BLACK
    }

    fun draw(canvas: Canvas) {
        val bitmap = renderingBitmap
        if ((bitmap != null || error) && animateAlpha) {
            if (show && blurAlpha != 1f) {
                blurAlpha += 0.09f
                if (blurAlpha > 1f) {
                    blurAlpha = 1f
                }
                parentView!!.invalidate()
            } else if (!show && blurAlpha != 0f) {
                blurAlpha -= 0.09f
                if (blurAlpha < 0) {
                    blurAlpha = 0f
                }
                parentView!!.invalidate()
            }
        }
        val alpha = if (animateAlpha) blurAlpha else 1f
        if (bitmap == null && error) {
            errorBlackoutPaint.alpha = (50 * alpha).toInt()
            canvas.drawPaint(errorBlackoutPaint)
            return
        }
        if (alpha == 1f) {
            canvas.save()
        } else {
            canvas.saveLayerAlpha(
                0f, 0f, parentView!!.measuredWidth.toFloat(), parentView.measuredHeight.toFloat(),
                (alpha * 255).toInt(), Canvas.ALL_SAVE_FLAG
            )
        }
        if (bitmap != null) {
            emptyPaint.alpha = (255 * alpha).toInt()
            canvas.save()
            canvas.scale(
                parentView!!.measuredWidth / bitmap[1]!!.width.toFloat(), parentView.measuredHeight / bitmap[1]!!
                    .height.toFloat()
            )
            canvas.drawBitmap(bitmap[1]!!, 0f, 0f, emptyPaint)
            canvas.restore()
            canvas.save()
            canvas.translate(0f, panTranslationY)
            canvas.scale(
                parentView.measuredWidth / bitmap[0]!!.width.toFloat(), toolbarH / bitmap[0]!!
                    .height.toFloat()
            )
            canvas.drawBitmap(bitmap[0]!!, 0f, 0f, emptyPaint)
            canvas.restore()
            wasDraw = true
            canvas.drawColor(0x1a000000)
        }
        canvas.restore()
        if (show && !processingNextFrame && (renderingBitmap == null || invalidate)) {
            processingNextFrame = true
            invalidate = false
            if (blurredBitmapTmp == null) {
                blurredBitmapTmp = arrayOfNulls(2)
                blurCanvas = arrayOfNulls(2)
            }
            for (i in 0..1) {
                if (blurredBitmapTmp!![i] == null || parentView!!.measuredWidth != lastW || parentView.measuredHeight != lastH) {
                    val lastH = parentView!!.measuredHeight
                    val lastW = parentView.measuredWidth
                    toolbarH = getStatusBarHeight(behindView.context)// + behindView.dp(200)
                    try {
                        val h = if (i == 0) toolbarH else lastH
                        blurredBitmapTmp!![i] = Bitmap.createBitmap(
                            (lastW / DOWN_SCALE).toInt(),
                            (h / DOWN_SCALE).toInt(), Bitmap.Config.ARGB_8888
                        )
                        blurCanvas!![i] = Canvas(blurredBitmapTmp!![i]!!)
                    } catch (e: Exception) {
                        Log.e("TAGTAG", "", e)
                        runOnUIThread {
                            error = true
                            parentView.invalidate()
                        }
                        return
                    }
                } else {
                    blurredBitmapTmp!![i]!!.eraseColor(Color.TRANSPARENT)
                }
                if (i == 1) {
                    blurredBitmapTmp!![i]!!.eraseColor(Color.WHITE)
                }
                blurCanvas!![i]!!.save()
                blurCanvas!![i]!!.scale(1f / DOWN_SCALE, 1f / DOWN_SCALE, 0f, 0f)
                var backDrawable = behindView.background
                if (backDrawable == null) {
                    backDrawable = ColorDrawable(Color.WHITE)
                    Log.e("TAGTAG", "backDrawable is null 152")
                }
                behindView.setTag(TAG_DRAWING_AS_BACKGROUND, i)
                if (i == STATIC_CONTENT) {
                    blurCanvas!![i]!!.translate(0f, -panTranslationY)
                    behindView.draw(blurCanvas!![i])
                }
                if (i == ADJUST_PAN_TRANSLATION_CONTENT) {
                    val oldBounds = backDrawable.bounds
                    backDrawable.setBounds(0, 0, behindView.measuredWidth, behindView.measuredHeight)
                    backDrawable.draw(blurCanvas!![i]!!)
                    backDrawable.bounds = oldBounds
                    behindView.draw(blurCanvas!![i])
                }
                behindView.setTag(TAG_DRAWING_AS_BACKGROUND, null)
                blurCanvas!![i]!!.restore()
            }
            lastH = parentView!!.measuredHeight
            lastW = parentView.measuredWidth
            blurBackgroundTask.width = parentView.measuredWidth
            blurBackgroundTask.height = parentView.measuredHeight
            if (blurBackgroundTask.width == 0 || blurBackgroundTask.height == 0) {
                processingNextFrame = false
                return
            }
            if (queue == null) {
                queue = DispatchQueue("blur_thread_$this")
            }
            queue!!.postRunnable(blurBackgroundTask)
        }
    }

    private val blurRadius: Int
        get() = max(7, max(lastH, lastW) / 180)

    fun clear() {
        invalidate = true
        wasDraw = false
        error = false
        blurAlpha = 0f
        lastW = 0
        lastH = 0
        if (queue != null) {
            queue!!.cleanupQueue()
            queue!!.postRunnable {
                if (renderingBitmap != null) {
                    if (renderingBitmap!![0] != null) {
                        renderingBitmap!![0]!!.recycle()
                    }
                    if (renderingBitmap!![1] != null) {
                        renderingBitmap!![1]!!.recycle()
                    }
                    renderingBitmap = null
                }
                if (backgroundBitmap != null) {
                    if (backgroundBitmap!![0] != null) {
                        backgroundBitmap!![0]!!.recycle()
                    }
                    if (backgroundBitmap!![1] != null) {
                        backgroundBitmap!![1]!!.recycle()
                    }
                    backgroundBitmap = null
                }
                renderingBitmapCanvas = null
                skipDraw = false
                runOnUIThread {
                    if (queue != null) {
                        queue!!.recycle()
                        queue = null
                    }
                }
            }
        }
    }

    fun invalidate() {
        invalidate = true
        parentView?.invalidate()
    }

    val isFullyDrawing: Boolean
        get() = !skipDraw && wasDraw && (blurAlpha == 1f || !animateAlpha) && show && parentView!!.alpha == 1f

    fun checkSizes() {
        val bitmap = renderingBitmap
        if (bitmap == null || parentView!!.measuredHeight == 0 || parentView.measuredWidth == 0) {
            return
        }
        generateBlurredBitmaps()
        lastH = parentView.measuredHeight
        lastW = parentView.measuredWidth
    }

    private fun generateBlurredBitmaps() {
        var bitmap = renderingBitmap
        if (bitmap == null) {
            renderingBitmap = arrayOfNulls(2)
            bitmap = renderingBitmap
            renderingBitmapCanvas = arrayOfNulls(2)
        }
        if (blurredBitmapTmp == null) {
            blurredBitmapTmp = arrayOfNulls(2)
            blurCanvas = arrayOfNulls(2)
        }
        blurBackgroundTask.canceled = true
        blurBackgroundTask = BlurBackgroundTask()
        for (i in 0..1) {
            val lastH = parentView!!.measuredHeight
            val lastW = parentView.measuredWidth
            toolbarH = getStatusBarHeight(behindView.context) + behindView.context.dp(200)
            val h = if (i == 0) toolbarH else lastH
            if (bitmap!![i] == null || bitmap[i]!!.height != h || bitmap[i]!!.width != parentView.measuredWidth) {
                if (queue != null) {
                    queue!!.cleanupQueue()
                }
                blurredBitmapTmp!![i] = Bitmap.createBitmap(
                    (lastW / DOWN_SCALE).toInt(),
                    (h / DOWN_SCALE).toInt(), Bitmap.Config.ARGB_8888
                )
                if (i == 1) {
                    blurredBitmapTmp!![i]!!.eraseColor(Color.WHITE)
                }
                blurCanvas!![i] = Canvas(blurredBitmapTmp!![i]!!)
                val bitmapH = ((if (i == 0) toolbarH else lastH) / DOWN_SCALE).toInt()
                val bitmapW = (lastW / DOWN_SCALE).toInt()
                renderingBitmap!![i] = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888)
                renderingBitmapCanvas!![i] = Canvas(renderingBitmap!![i]!!)
                renderingBitmapCanvas!![i]!!.scale(
                    renderingBitmap!![i]!!.getWidth()
                        .toFloat() / blurredBitmapTmp!![i]!!.getWidth().toFloat(), renderingBitmap!![i]!!.getHeight()
                        .toFloat() / blurredBitmapTmp!![i]!!.getHeight().toFloat()
                )
                blurCanvas!![i]!!.save()
                blurCanvas!![i]!!.scale(1f / DOWN_SCALE, 1f / DOWN_SCALE, 0f, 0f)
                var backDrawable = behindView.background
                if (backDrawable == null) {
                    Log.e("TAGTAG", "backDrawable is null 288")
                }
                behindView.setTag(TAG_DRAWING_AS_BACKGROUND, i)
                if (i == STATIC_CONTENT) {
                    blurCanvas!![i]!!.translate(0f, -panTranslationY)
                    behindView.draw(blurCanvas!![i])
                }
                if (i == ADJUST_PAN_TRANSLATION_CONTENT) {
                    val oldBounds = backDrawable.bounds
                    backDrawable.setBounds(0, 0, behindView.measuredWidth, behindView.measuredHeight)
                    backDrawable.draw(blurCanvas!![i]!!)
                    backDrawable.bounds = oldBounds
                    behindView.draw(blurCanvas!![i])
                }
                behindView.setTag(TAG_DRAWING_AS_BACKGROUND, null)
                blurCanvas!![i]!!.restore()
                stackBlurBitmap(blurredBitmapTmp!![i]!!, blurRadius)
                emptyPaint.alpha = 255
                if (i == 1) {
                    renderingBitmap!![i]!!.eraseColor(Color.WHITE)
                }
                renderingBitmapCanvas!![i]!!.drawBitmap(blurredBitmapTmp!![i]!!, 0f, 0f, emptyPaint)
            }
        }
    }

    fun show(show: Boolean) {
        this.show = show
    }

    fun setAnimateAlpha(animateAlpha: Boolean) {
        this.animateAlpha = animateAlpha
    }

    fun onPanTranslationUpdate(y: Float) {
        panTranslationY = y
        parentView!!.invalidate()
    }

    inner class BlurBackgroundTask : Runnable {
        var canceled = false
        var width = 0
        var height = 0
        override fun run() {
            if (backgroundBitmap == null) {
                backgroundBitmap = arrayOfNulls(2)
                backgroundBitmapCanvas = arrayOfNulls(2)
            }
            val bitmapWidth = (width / DOWN_SCALE).toInt()
            for (i in 0..1) {
                val h = ((if (i == 0) toolbarH else height) / DOWN_SCALE).toInt()
                if (backgroundBitmap!![i] != null && (backgroundBitmap!![i]!!
                        .height != h || backgroundBitmap!![i]!!.width != bitmapWidth)) {
                    if (backgroundBitmap!![i] != null) {
                        backgroundBitmap!![i]!!.recycle()
                        backgroundBitmap!![i] = null
                    }
                }
                val t = System.currentTimeMillis()
                if (backgroundBitmap!![i] == null) {
                    try {
                        backgroundBitmap!![i] = Bitmap.createBitmap(bitmapWidth, h, Bitmap.Config.ARGB_8888)
                        backgroundBitmapCanvas!![i] = Canvas(backgroundBitmap!![i]!!)
                        backgroundBitmapCanvas!![i]!!.scale(
                            bitmapWidth / blurredBitmapTmp!![i]!!
                                .width.toFloat(), h / blurredBitmapTmp!![i]!!.height.toFloat()
                        )
                    } catch (e: Throwable) {
                        Log.e("TAGTAG", e.stackTrace.toString())
                    }
                }
                if (i == 1) {
                    backgroundBitmap!![i]!!.eraseColor(Color.WHITE)
                } else {
                    backgroundBitmap!![i]!!.eraseColor(Color.TRANSPARENT)
                }
                emptyPaint.alpha = 255
                stackBlurBitmap(blurredBitmapTmp!![i]!!, blurRadius)
                if (backgroundBitmapCanvas!![i] != null) {
                    backgroundBitmapCanvas!![i]!!.drawBitmap(blurredBitmapTmp!![i]!!, 0f, 0f, emptyPaint)
                }
                if (canceled) {
                    return
                }
            }
            runOnUIThread {
                if (canceled) {
                    return@runOnUIThread
                }
                val bitmap = renderingBitmap
                val canvas = renderingBitmapCanvas
                renderingBitmap = backgroundBitmap!!
                renderingBitmapCanvas = backgroundBitmapCanvas
                backgroundBitmap = bitmap
                backgroundBitmapCanvas = canvas
                processingNextFrame = false
                parentView?.invalidate()
            }
        }
    }

    private fun runOnUIThread(runnable: Runnable) {
        parentView?.handler?.post(runnable)
    }

    private fun getThemedColor(key: String): Int =
        Color.WHITE

    companion object {
        const val TAG_DRAWING_AS_BACKGROUND = (1 shl 26) + 3
        const val STATIC_CONTENT = 0
        const val ADJUST_PAN_TRANSLATION_CONTENT = 1
    }
}

private class DispatchQueue @JvmOverloads constructor(threadName: String, start: Boolean = true) : Thread() {

    @Volatile
    private var handler: Handler? = null
    private val syncLatch = CountDownLatch(1)
    var lastTaskTime: Long = 0
        private set

    init {
        name = threadName
        if (start) {
            start()
        }
    }

    fun postRunnable(runnable: Runnable?): Boolean {
        lastTaskTime = SystemClock.elapsedRealtime()
        return postRunnable(runnable, 0)
    }

    fun postRunnable(runnable: Runnable?, delay: Long): Boolean {
        try {
            syncLatch.await()
        } catch (e: Exception) { }
        return if (delay <= 0) {
            handler!!.post(runnable!!)
        } else {
            handler!!.postDelayed(runnable!!, delay)
        }
    }

    fun cleanupQueue() {
        try {
            syncLatch.await()
            handler!!.removeCallbacksAndMessages(null)
        } catch (e: Exception) { }
    }

    fun recycle() {
        handler!!.looper.quit()
    }

    override fun run() {
        Looper.prepare()
        handler = Handler()
        syncLatch.countDown()
        Looper.loop()
    }
}

private fun getStatusBarHeight(context: Context): Int {
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
}

private fun stackBlurBitmap(original: Bitmap, radius: Int): Bitmap? {
    if (radius < 1) return null
    val w = original.width
    val h = original.height
    val pix = IntArray(w * h)
    original.getPixels(pix, 0, w, 0, 0, w, h)
    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1
    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    val vmin = IntArray(max(w, h))
    var divsum = div + 1 shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
        dv[i] = i / divsum
        i++
    }
    yi = 0
    yw = yi
    val stack = Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int
    y = 0
    while (y < h) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        i = -radius
        while (i <= radius) {
            p = pix[yi + min(wm, max(i, 0))]
            sir = stack[i + radius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rbs = r1 - abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            i++
        }
        stackpointer = radius
        x = 0
        while (x < w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (y == 0) {
                vmin[x] = min(x + radius + 1, wm)
            }
            p = pix[yw + vmin[x]]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi++
            x++
        }
        yw += w
        y++
    }
    x = 0
    while (x < w) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = max(0, yp) + x
            sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            rbs = r1 - abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += w
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < h) {

            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
            pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (x == 0) {
                vmin[y] = min(y + r1, hm) * w
            }
            p = x + vmin[y]
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi += w
            y++
        }
        x++
    }
    original.setPixels(pix, 0, w, 0, 0, w, h)
    return original
}