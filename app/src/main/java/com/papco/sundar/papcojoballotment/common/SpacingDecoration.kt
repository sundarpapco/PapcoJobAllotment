package com.papco.sundar.papcojoballotment.common

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingDecoration(val context:Context):RecyclerView.ItemDecoration() {

    private val twelveDp=getPixelValue(12)
    private val seventyDp=getPixelValue(70)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val dataCount=parent.adapter!!.itemCount
        val viewPosition=parent.getChildAdapterPosition(view)

        outRect.left=twelveDp
        outRect.right=twelveDp

        when(viewPosition){
            0->{
                outRect.top=twelveDp
                outRect.bottom=twelveDp/2
                return
            }

            dataCount-1 ->{
                outRect.top=twelveDp/2
                outRect.bottom=seventyDp
                return
            }

            else->{
                outRect.top=twelveDp/2
                outRect.bottom=twelveDp/2
            }
        }


    }

    private fun getPixelValue(Dp: Int): Int {

        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            Dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()

    }

}