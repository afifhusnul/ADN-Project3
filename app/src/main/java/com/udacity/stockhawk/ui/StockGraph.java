package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.stockhawk.R.id.chart;

/**
 * Created by NUSNAFIF on 1/19/2017.
 */

public class StockGraph extends AppCompatActivity {

    @BindView(chart)
    LineChart mChart;

    private String[] array;
    private Cursor cursor;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_graph);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        cursor = getContentResolver().query(Contract.Quote.makeUriForStock(intent.getStringExtra("symbol")), null, null, null, null);
        setTitle(intent.getStringExtra("symbol"));

        List<Entry> entries = new ArrayList<Entry>();

        while (cursor.moveToNext()){
            array = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY)).split("\n");
        }

        i = array.length - 1;
        for(i = array.length - 1; i > 0; i--){
            entries.add(new Entry(Float.parseFloat(array[i].split(",")[0]), Float.parseFloat(array[i].split(",")[1])));
        }

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new MyXAxisValueFormatter());
        xAxis.setTextColor(Color.WHITE);

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setValueFormatter(new MyYAxisValueFormatter());
        yAxis.setTextColor(Color.WHITE);

        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setValueFormatter(new MyYAxisValueFormatter());
        yAxisRight.setTextColor(Color.WHITE);

        LineDataSet dataSet = new LineDataSet(entries, intent.getStringExtra("symbol")); // add entries to dataset
        dataSet.setColors(new int[] { R.color.colorAccent }, getApplicationContext());
        dataSet.setValueTextColor(Color.GREEN);
        LineData lineData = new LineData(dataSet);

        mChart.getLegend().setTextColor(Color.WHITE);
        mChart.getDescription().setEnabled(false);
        mChart.setData(lineData);
        mChart.invalidate(); // refresh
    }

    // Format values on line X
    public class MyXAxisValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return new SimpleDateFormat("MM yyyy").format(new Date((long)value));
        }
    }

    // Format values on line Y
    public class MyYAxisValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return "$ " + value;
        }
    }
}
