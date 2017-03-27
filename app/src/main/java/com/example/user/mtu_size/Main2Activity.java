package com.example.user.mtu_size;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class Main2Activity extends AppCompatActivity{
    private LineChart mChart;
    private int time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mChart = (LineChart) findViewById(R.id.linechart);
        // customize line chart
        mChart.setDescription("");
        mChart.setNoDataTextDescription("No data for the moment");

        // enable value highlighting
        mChart.setHighlightPerTapEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // we want alse enable scaling and dragging
        mChart.setScaleEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setDrawGridBackground(false);

        // enable pinch zoomto avoid scaling x and y axis separately
        mChart.setPinchZoom(true);

        // alternative background color
        mChart.setBackgroundColor(Color.BLACK);

        // now we work on data
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add data to line chart
        mChart.setData(data);

        // get length object
        Legend l = mChart.getLegend();

        // customize legend
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis x1 = mChart.getXAxis();
        x1.setPosition(XAxis.XAxisPosition.BOTTOM);
        x1.setTextColor(Color.WHITE);
        x1.setDrawGridLines(true);
        x1.setAvoidFirstLastClipping(true);


        YAxis y1 = mChart.getAxisLeft();
        y1.setTextColor(Color.WHITE);
        y1.setAxisMaxValue(140f);
        y1.setDrawGridLines(true);
        y1.setAxisMinValue(0f);

        YAxis y12 = mChart.getAxisRight();
        y12.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // now, we're going to simulate real time data addition

        new Thread(new Runnable(){

            @Override
            public void run(){
                // add 100 entries
                for (int i = 0; i < 100; i++){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            // TODO auto-generated method stub
                            addEntry(); // chart is notified of update
                        }
                    });

                    // pause between adds
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // manage error
                    }
                }
            }
        }).start();
    }

    // we need to create a method to add entry to the line chart
    private void addEntry(){
        LineData data = mChart.getData();

        if (data != null) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);

            if (set == null){
                //creation if null
                set = creatSet();
                data.addDataSet(set);
            }

            // add a new random value
            data.addXValue(String.valueOf(time++));
            data.addEntry(new Entry((float) (Math.random() * 120) + 5f, set.getEntryCount()), 0);

            // notify charge data have changed
            mChart.notifyDataSetChanged();

            // limit number of visible entries
            mChart.setVisibleXRange(0,9);

            // scroll to the last entry
            mChart.moveViewToX(data.getXValCount() - 10);
        }
    }

    // method to create set
    private LineDataSet creatSet() {
        LineDataSet set = new LineDataSet(null, "ECG");
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 177));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);
        return set;
    }

    //===================================================================================

    // return button
    public void Home(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    // take a screenshot
    public void screenshot(View view) {
        takeScreenshot();
    }

    // Screenshot
    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }
}


