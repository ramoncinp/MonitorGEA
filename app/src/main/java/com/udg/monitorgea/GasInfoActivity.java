package com.udg.monitorgea;

import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class GasInfoActivity extends AppCompatActivity
{
    //Views
    private TextView currentValueTv;
    private TextView currentAmountTv;
    private TextView currentPeriodTv;

    //Gráfica
    private LineChart lineChart;

    //Variables
    private int currentValue;
    private int chartIdx;

    //Listas
    private ArrayList<Entry> values = new ArrayList<>();

    //Objetos
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gas_info);

        //Inicializar views
        initViews();

        //Mostrar valores
        currentValue = 90;
        for (chartIdx = 1; chartIdx <= 10; chartIdx++)
        {
            values.add(new Entry(chartIdx, currentValue));
            if (chartIdx <= 8 || chartIdx > 12)
            {
                currentValue--;
            }
        }
        setValues();

        //Definir handler para pruebas
        setHandler();
    }

    private void setHandler()
    {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                currentValue--;
                if (currentValue > 5)
                {
                    if (currentValue % 2 == 0)
                    {
                        values.add(new Entry(++chartIdx, currentValue));
                        setValues();
                    }
                    mHandler.postDelayed(this, 400);
                }
            }
        }, 400);
    }

    private void initViews()
    {
        currentValueTv = findViewById(R.id.valor_actual_tv);
        currentAmountTv = findViewById(R.id.saldo_actual_tv);
        currentPeriodTv = findViewById(R.id.periodo_actual_tv);

        lineChart = findViewById(R.id.consumo_periodo_grafica);
        lineChart.setTouchEnabled(false);
        lineChart.setPinchZoom(false);
    }

    private void setValues()
    {
        LineDataSet set;
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0)
        {
            set = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set.setValues(values);

            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        }
        else
        {
            set = new LineDataSet(values, "Nivel de gas");
            set.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            set.setLineWidth(2f);
            set.setCircleRadius(1f);
            set.setCircleColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            set.setDrawValues(false);
            set.setDrawFilled(true);
            set.setFillColor(ContextCompat.getColor(this, R.color.colorPrimary));
            set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            YAxis left = lineChart.getAxisLeft();
            left.setAxisMinimum(0f);
            left.setTextSize(10f);

            YAxis right = lineChart.getAxisRight();
            right.setEnabled(false);

            LineData data = new LineData(dataSets);
            lineChart.setData(data);

            Description description = new Description();
            description.setText("");
            lineChart.setDescription(description);
            lineChart.animateY(800);
        }

        refreshTextViews();
    }

    private void refreshTextViews()
    {
        //Suponiendo que 100% cuesta $800
        currentAmountTv.setText("$" + Constants.DOUBLE_FORMAT.format(currentValue * 2500 / 100));
        currentValueTv.setText(String.valueOf(currentValue) + "%");
    }
}
