package com.udg.monitorgea;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity
{
    //TAG
    private final String TAG = this.getClass().getSimpleName();

    //Views
    private TextView currentValueTv;
    private TextView currentAmountTv;
    private TextView currentPeriodTv;
    private ProgressBar progressBar;

    //Gráfica
    private LineChart lineChart;

    //Variables
    private float currentValue, referenceValue, lastValue;
    private int chartIdx = 0;
    private int energyIdx;
    private String registersName = "";

    //Listas
    private ArrayList<Entry> values = new ArrayList<>();
    private ArrayList<Register> registers = new ArrayList<>();

    //Referencias de bd
    private DatabaseReference sensorRegisters;
    private DatabaseReference period;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_info);

        //Obtener el tipo de energía, GAS por default
        energyIdx = getIntent().getIntExtra("element", MainActivity.GAS_IDX);
        switch (energyIdx)
        {
            case MainActivity.GAS_IDX:
                registersName = "gas";
                break;

            case MainActivity.ELEC_IDX:
                registersName = "electricidad";
                break;

            case MainActivity.AGUA_IDX:
                registersName = "agua";
                break;
        }

        //Inicializar views
        initViews();

        //Inicializar base de datos
        initDatabase();

        //Mostrar valores
        //initValues();
        setValues();
    }

    private void initViews()
    {
        TextView valueTitle = findViewById(R.id.valor_actual_titulo);
        TextView amountTitle = findViewById(R.id.saldo_actual_titulo);

        progressBar = findViewById(R.id.progress_bar);

        if (energyIdx == MainActivity.GAS_IDX)
        {
            valueTitle.setText("Porcentaje actual");
            amountTitle.setText("Saldo estimado");
        }
        else
        {
            valueTitle.setText("Acumulado actual");
            amountTitle.setText("Costo estimado");
        }

        currentValueTv = findViewById(R.id.valor_actual_tv);
        currentAmountTv = findViewById(R.id.saldo_actual_tv);
        currentPeriodTv = findViewById(R.id.periodo_actual_tv);

        lineChart = findViewById(R.id.consumo_periodo_grafica);
        lineChart.setTouchEnabled(false);
        lineChart.setPinchZoom(false);
    }

    private void setValues()
    {
        if (values.size() == 0) return;

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
            switch (energyIdx)
            {
                case MainActivity.GAS_IDX:
                    set = new LineDataSet(values, "Nivel de gas");
                    break;

                case MainActivity.ELEC_IDX:
                    set = new LineDataSet(values, "kW/h");
                    break;

                case MainActivity.AGUA_IDX:
                    set = new LineDataSet(values, "Litros");
                    break;

                default:
                    set = new LineDataSet(values, "Nivel de gas");
            }

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
        if (progressBar.getVisibility() == View.VISIBLE)
        {
            progressBar.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);
        }
    }

    private void refreshTextViews()
    {
        switch (energyIdx)
        {
            case MainActivity.GAS_IDX:
                currentValueTv.setText((int)(currentValue) + "%");
                //Teniendo un taque estacionario de 300L, a $10.98 el litro
                double capacidadTotal = 300;
                double currentAmount = capacidadTotal * (currentValue / 100) * 10.98;
                currentAmountTv.setText("$" + Constants.DOUBLE_FORMAT.format(currentAmount));
                break;

            case MainActivity.ELEC_IDX:
                currentValueTv.setText(Constants.DOUBLE_FORMAT.format(currentValue) + " kW/h");
                currentAmountTv.setText("$" + Constants.DOUBLE_FORMAT.format(currentValue * 2.75));
                break;

            case MainActivity.AGUA_IDX:
                currentValueTv.setText(Constants.DOUBLE_FORMAT.format(currentValue) + " L");
                //Calcular tarifa deacuerdo a litros
                //De 0 a 6000 litros... $62.07
                double waterAmount = 0;
                if (currentValue > 0 && currentValue <= 6000)
                {
                    waterAmount = 62.07;
                }
                else if (currentValue > 6000 && currentValue <= 10000)
                {
                    waterAmount = 140.17;
                }
                else if (currentValue > 10000 && currentValue <= 14000)
                {
                    waterAmount = 219.27;
                }
                else if (currentValue > 14000 && currentValue <= 17000)
                {
                    waterAmount = 291.52;
                }
                else if (currentValue > 17000 && currentValue <= 20000)
                {
                    waterAmount = 363.77;
                }
                else if (currentValue > 20000 && currentValue <= 25000)
                {
                    waterAmount = 484.69;
                }

                currentAmountTv.setText("$" + Constants.DOUBLE_FORMAT.format(waterAmount));
                break;
        }
    }

    private void initDatabase()
    {
        //Obtener fechas
        period = FirebaseDatabase.getInstance().getReference("periodos/" + registersName);
        period.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Log.d(TAG, dataSnapshot.toString());

                //Obtener epoch
                Long epoch = dataSnapshot.child("fecha_inicial").getValue(Long.class);
                if (epoch != null)
                {
                    //Obtener fecha actual
                    Date now = new Date();

                    //Convertir epoch a objeto Date
                    Date initalDate = new Date(epoch * 1000L);

                    //Convertir a texto
                    SimpleDateFormat format = new SimpleDateFormat("dd MMM", Locale.getDefault());

                    //Escribir en Tv
                    String mPeriod = format.format(initalDate) + " - " + format.format(now);
                    currentPeriodTv.setText(mPeriod);
                }

                //Obtener valor de referencia
                Float mReference = dataSnapshot.child("valor").getValue(Float.class);
                if (mReference != null)
                {
                    referenceValue = mReference;
                }
                else
                {
                    referenceValue = 0f;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(TAG, databaseError.getMessage() + "\n" + databaseError.getDetails());
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                //Obtener registros
                sensorRegisters = FirebaseDatabase.getInstance().getReference("registros/" + registersName);
                sensorRegisters.addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        //Obtener valor
                        Float mValue = dataSnapshot.child("valor").getValue(Float.class);
                        if (mValue != null)
                        {
                            values.add(new Entry(++chartIdx, mValue));
                            setValues();

                            if (energyIdx == MainActivity.GAS_IDX)
                            {
                                currentValue = mValue;
                            }
                            else
                            {
                                lastValue = mValue;
                                currentValue = lastValue - referenceValue;
                            }

                            refreshTextViews();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }
        }, 500);
    }
}
