package com.thomascook.core

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.ScaleDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * Collection interface for accessing view group elements using collection interface and functions.
 */
class ViewsCollection(private val viewGroup: ViewGroup?) : AbstractList<View>() {

    override fun get(index: Int): View {
        if (this.viewGroup == null || index >= viewGroup.childCount) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }

        return viewGroup.getChildAt(index)
    }

    override val size: Int
        get() = viewGroup?.childCount ?: 0
}

/**
 * Collection of helper functions related to UI
 */
object ViewUtils {
    /**
     * Sets specified font to all TextViews in the specified view container.
     */
    @JvmStatic
    fun setTextViewsFont(viewGroup: ViewGroup,
                         typeFaceAssetPath: String) {

        val typeface = Typeface.createFromAsset(viewGroup.context.assets, typeFaceAssetPath)

        if (typeface != null)
            setTextViewsFont(viewGroup, typeface)
    }

    /**
     * Function that sets typeface on all it's TextView children
     */
    @JvmStatic
    private fun setTextViewsFont(viewGroup: ViewGroup,
                                 typeface: Typeface) {

        ViewsCollection(viewGroup).forEach { view ->
            if (view is TextView) {
                view.typeface = typeface
            } else if (view is ViewGroup) {
                setTextViewsFont(view, typeface)
            }
        }
    }

    @JvmStatic
    fun setLetterSpacing(viewGroup: ViewGroup, spacing: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return
        ViewsCollection(viewGroup).forEach { view ->
            if (view is TextView) {
                view.letterSpacing = spacing
            } else if (view is ViewGroup) {
                setLetterSpacing(view, spacing)
            }
        }
    }

    private const val KEY_RECYCLE_POSITION = "key_list_pos"

    /**
     * Saves the recycle view position in the specified bundle
     *
     * @param bundle       Bundle to save data in.
     * @param recyclerView Reference to a recycler view.
     */
    @JvmStatic
    fun saveRecyclerViewPosition(
        bundle: Bundle?, recyclerView: RecyclerView?) {
        if (bundle == null)
            return

        val lm = (recyclerView?.layoutManager as LinearLayoutManager?) ?: return

        val visiblePos = lm.findLastCompletelyVisibleItemPosition()
        if (visiblePos != RecyclerView.NO_POSITION) {
            bundle.putInt(KEY_RECYCLE_POSITION, visiblePos)
        }
    }

    /**
     * Restores the state of a recycler view which was saved with saveRecyclerViewPosition function.
     *
     * @param bundle       Bundle to save data in.
     * @param recyclerView Reference to a recycler view.
     */
    @JvmStatic
    fun restoreRecyclerViewPosition(
        bundle: Bundle?, recyclerView: RecyclerView?) {

        if (bundle == null || recyclerView == null)
            return

        val visiblePos = bundle.getInt(KEY_RECYCLE_POSITION, RecyclerView.NO_POSITION)
        if (visiblePos != RecyclerView.NO_POSITION) {
            val lm = recyclerView.layoutManager as LinearLayoutManager? ?: return

            lm.scrollToPosition(visiblePos)

            bundle.remove(KEY_RECYCLE_POSITION)
        }
    }

    /**
     * Used to load vector drawable and set it's size to intrinsic values
     *
     * @param context  Reference to {@link Context}
     * @param resId    Vector image resource id. If set to 0 then null is returned.
     * @param newWidth If not 0 then set the drawable's width to this value and scale
     *                 height accordingly.
     * @return On success a reference to a vector drawable
     */
    @JvmStatic
    fun getScaledDrawable(context: Context,
                          @DrawableRes resId: Int,
                          newWidth: Float = 0f): Drawable? {
        if (resId == 0)
            return null

        val drawableCompat = AppCompatResources.getDrawable(context, resId)

        if (drawableCompat != null) {

            drawableCompat.setBounds(0, 0, drawableCompat.intrinsicWidth, drawableCompat.intrinsicHeight)

            if (newWidth.toDouble() != 0.0) {
                val scale = newWidth / drawableCompat.intrinsicWidth
                val height = scale * drawableCompat.intrinsicHeight
                val scaledDrawable = ScaleDrawable(drawableCompat, Gravity.CENTER, 1.0f, 1.0f)
                scaledDrawable.setBounds(0, 0, newWidth.toInt(), height.toInt())
                scaledDrawable.level = 10000
                return scaledDrawable
            }
        }
        return drawableCompat
    }
}