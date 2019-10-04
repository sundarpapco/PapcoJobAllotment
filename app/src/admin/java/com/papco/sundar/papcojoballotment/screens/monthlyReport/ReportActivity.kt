package com.papco.sundar.papcojoballotment.screens.monthlyReport

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarDataSet
import com.papco.sundar.papcojoballotment.chart.ChartValues
import com.papco.sundar.papcojoballotment.chart.DateAxisFormatter
import com.papco.sundar.papcojoballotment.chart.DurationAxisFormatter
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.common.MonthPickerDialogFragment
import kotlinx.android.synthetic.admin.activity_report.*
import kotlinx.android.synthetic.main.loading_bar.*
import java.util.*

class ReportActivity : AppCompatActivity(), MonthPickerDialogFragment.MonthSelectionListener {

    private val viewModel: ReportActivityVM by lazy {
        ViewModelProviders.of(this).get(ReportActivityVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        initViews()

        viewModel.chartDetail.observe(this, Observer {
            loadDataInChart(it)
            hideLoadingBar()
        })
        loadPreviousMonthChart()
    }


    override fun onResume() {
        super.onResume()
        supportActionBar?.title = "Monthly report"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item == null) return false

        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    private fun initViews() {
        initChartView()
        tvMonth.setOnClickListener {
            if (!isLoading())
                showMonthPickerDialog()
        }
    }

    private fun initChartView() {

        //chart.setOnChartValueSelectedListener(this)

        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)

        chart.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(true)

        chart.setDrawGridBackground(false)
        // chart.setDrawYLabels(false);

        val xAxisFormatter = DateAxisFormatter()


        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //xAxis.typeface = tfLight
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 15
        xAxis.valueFormatter = xAxisFormatter

        val yAxisFormatter = DurationAxisFormatter()

        val leftAxis = chart.getAxisLeft()
        //leftAxis.setTypeface(tfLight)
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = yAxisFormatter
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = chart.axisRight
        rightAxis.setDrawGridLines(false)
        //rightAxis.setTypeface(tfLight)
        rightAxis.setLabelCount(8, false)
        rightAxis.valueFormatter = yAxisFormatter
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        /*val l = chart.getLegend()
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM)
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT)
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL)
        l.setDrawInside(false)
        l.setForm(Legend.LegendForm.SQUARE)
        l.setFormSize(9f)
        l.setTextSize(11f)
        l.setXEntrySpace(4f)*/

        /*val mv = XYMarkerView(this, xAxisFormatter)
        mv.setChartView(chart) // For bounds control
        chart.setMarker(mv) // Set the marker to the chart*/

    }

    private fun loadPreviousMonthChart() {
        val calendar = Calendar.getInstance(Locale.getDefault())
        val year = calendar.get(Calendar.YEAR)
        when (val month = calendar.get(Calendar.MONTH)) {
            0 -> {
                loadChart(11, year - 1)
            }
            else -> {
                loadChart(month - 1, year)
            }
        }
    }

    private fun monthName(monthOfYear: Int): String {
        when (monthOfYear) {
            0 -> {
                return "January"
            }
            1 -> {
                return "February"
            }
            2 -> {
                return "March"
            }
            3 -> {
                return "April"
            }
            4 -> {
                return "May"
            }
            5 -> {
                return "June"
            }
            6 -> {
                return "July"
            }
            7 -> {
                return "August"
            }
            8 -> {
                return "September"
            }
            9 -> {
                return "October"
            }
            10 -> {
                return "November"
            }
            11 -> {
                return "December"
            }
        }
        return "Invalid"
    }

    private fun loadDataInChart(chartValues: ChartValues) {

        val averageTime =
            chartValues.totalDuration.divideBy(chartValues.noOfWorkingDays).asDetailString()

        chart.data = chartValues.barData
        chart.invalidate()
        chart.animateY(500)

        tvWorkingDays.text = "${chartValues.noOfWorkingDays} working days this month"
        tvAverageTime.text = "$averageTime per working day"
    }

    private fun showLoadingBar() {
        loading_progress_bar.visibility = View.VISIBLE
    }

    private fun hideLoadingBar() {
        loading_progress_bar.visibility = View.GONE
    }

    private fun isLoading(): Boolean {
        return loading_progress_bar.visibility == View.VISIBLE
    }

    private fun showMonthPickerDialog() {

        MonthPickerDialogFragment.getInstance(viewModel.loadedMonth, viewModel.loadedYear)
            .show(supportFragmentManager, MonthPickerDialogFragment.TAG)

    }

    override fun onMonthSelected(selectedMonth: Int, selectedYear: Int) {
        if (selectedMonth != viewModel.loadedMonth || selectedYear != viewModel.loadedYear) {
            loadChart(selectedMonth, selectedYear)
        }
    }

    private fun loadChart(monthOfYear: Int, year: Int) {
        tvMonth.text = "${monthName(monthOfYear)}, $year"
        showLoadingBar()
        viewModel.loadValues(monthOfYear, year)
    }
}