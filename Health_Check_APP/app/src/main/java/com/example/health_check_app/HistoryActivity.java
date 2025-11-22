package com.example.health_check_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.health_check_app.models.AlertRecord;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    
    private TabLayout tabLayout;
    private LineChart chart;
    private RadioGroup timeRangeGroup;
    private RecyclerView alertLogRecyclerView;
    
    private List<AlertRecord> alertRecords;
    private AlertLogAdapter alertLogAdapter;
    
    private static final int TAB_HEART_RATE = 0;
    private static final int TAB_BLOOD_OXYGEN = 1;
    private static final int TAB_TEMPERATURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initializeViews();
        setupChart();
        setupAlertLog();
        setupListeners();
        
        // Load initial data
        loadChartData(TAB_HEART_RATE, true);
    }
    
    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        chart = findViewById(R.id.chart);
        timeRangeGroup = findViewById(R.id.timeRangeGroup);
        alertLogRecyclerView = findViewById(R.id.alertLogRecyclerView);
    }
    
    private void setupChart() {
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
    }
    
    private void setupAlertLog() {
        alertRecords = new ArrayList<>();
        // Sample data
        alertRecords.add(new AlertRecord("心率", "心率过高: 120 BPM"));
        alertRecords.add(new AlertRecord("体温", "体温异常: 38.2°C"));
        alertRecords.add(new AlertRecord("跌倒", "检测到跌倒"));
        
        alertLogAdapter = new AlertLogAdapter(alertRecords);
        alertLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertLogRecyclerView.setAdapter(alertLogAdapter);
    }
    
    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean is24Hours = timeRangeGroup.getCheckedRadioButtonId() == R.id.radio24Hours;
                loadChartData(tab.getPosition(), is24Hours);
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        timeRangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean is24Hours = checkedId == R.id.radio24Hours;
            loadChartData(tabLayout.getSelectedTabPosition(), is24Hours);
        });
    }
    
    private void loadChartData(int tabPosition, boolean is24Hours) {
        List<Entry> entries = new ArrayList<>();
        String label;
        int color;
        
        // Generate sample data
        int dataPoints = is24Hours ? 48 : 12; // 30min intervals for 24h, 5min for 1h
        
        switch (tabPosition) {
            case TAB_HEART_RATE:
                label = getString(R.string.heart_rate);
                color = getColor(R.color.chart_heart_rate);
                for (int i = 0; i < dataPoints; i++) {
                    entries.add(new Entry(i, 60 + (float)(Math.random() * 40)));
                }
                break;
            case TAB_BLOOD_OXYGEN:
                label = getString(R.string.blood_oxygen);
                color = getColor(R.color.chart_blood_oxygen);
                for (int i = 0; i < dataPoints; i++) {
                    entries.add(new Entry(i, 95 + (float)(Math.random() * 5)));
                }
                break;
            case TAB_TEMPERATURE:
                label = getString(R.string.body_temperature);
                color = getColor(R.color.chart_temperature);
                for (int i = 0; i < dataPoints; i++) {
                    entries.add(new Entry(i, 36.0f + (float)(Math.random() * 2)));
                }
                break;
            default:
                return;
        }
        
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
