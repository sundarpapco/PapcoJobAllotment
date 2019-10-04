package com.papco.sundar.papcojoballotment.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler

class DateAxisFormatter:IAxisValueFormatter{
    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return value.toInt().toString()
    }
}

class DurationAxisFormatter:IAxisValueFormatter{
    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return if(value==0f) "" else value.toString()
    }
}

class BarValueFormatter:IValueFormatter{
    override fun getFormattedValue(
        value: Float,
        entry: Entry?,
        dataSetIndex: Int,
        viewPortHandler: ViewPortHandler?
    ): String {
        return if(value==0f) "" else value.toString()
    }
}