package com.udg.monitorgea;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Consumption extends AppCompatActivity
{
    //Constantes
    private final static String TAG = Consumption.class.getSimpleName();
    private final static int ANIMATION_DELAY = 800;
    public final static String SENSOR_TYPE = "sensor";

    //Variables
    private int sensorType = MainActivity.GAS_IDX;
    private String registersName = "";

    //Listar
    private ArrayList<Register> registers;

    //Views
    private ConstraintLayout chartLayout;
    private FloatingActionMenu floatingActionMenu;
    private ImageView arrowLeft;
    private ImageView arrowRight;
    private LinearLayout content;
    private TextView emptyTv;
    private TextView dateTitleTv;

    //Gráfica
    private LineChart chart;
    private DatePointer datePointer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);

        //Instanciar objeto para eje de tiempo
        datePointer = new DatePointer();

        //Obtener tipo de sensor
        if (getIntent().hasExtra(SENSOR_TYPE))
        {
            sensorType = getIntent().getIntExtra(SENSOR_TYPE, MainActivity.GAS_IDX);
            switch (sensorType)
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
        }

        //Inicializar views
        initViews();

        //Inicializar base de datos
        initDatabase();
    }

    private void initViews()
    {
        chart = findViewById(R.id.chart);
    }

    private void initDatabase()
    {
        //Preparar lista
        registers = new ArrayList<>();

        DatabaseReference registers = FirebaseDatabase.getInstance().getReference().child(registersName);
        registers.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                parseData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.d("Datos", "Error al obtener datos", databaseError.toException());
            }
        });
    }

    private void parseData(DataSnapshot dataSnapshot)
    {
        Log.d(TAG, dataSnapshot.toString());

        for (DataSnapshot child : dataSnapshot.getChildren())
        {
            Object value;
            if (sensorType == MainActivity.GAS_IDX)
                value = child.child("valor").getValue(Integer.class);
            else value = child.child("fecha").getValue(Double.class);

            Long epoch = child.child("fecha").getValue(Long.class);

            try
            {
                Register register = new Register(value, epoch);
                registers.add(register);
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }
        }

        setChartValues();
    }

    private void setChartValues()
    {
        //Crear lista de "datasets"
        List<ILineDataSet> dataSets = new ArrayList<>();

        //Crear objeto de calendario
        Calendar c = Calendar.getInstance();

        //Obtener el número de la semana actual
        c.setTime(new Date());

        //Crear lista de "Entries"
        List<Entry> mRegisters = new ArrayList<>();

        //Inicializar listas de datos
        for (int i = 0; i < registers.size(); i++)
        {
            if (i == 7) break;
            Object value = registers.get(i).getValor();
            mRegisters.add(new Entry(i, Float.parseFloat(value.toString())));
        }

        //Crear dataSet del tipo de producto y añadirlo a su lista
        LineDataSet registersSet = new LineDataSet(mRegisters, "Consumo Gas");
        registersSet.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        registersSet.setLineWidth(5);
        registersSet.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        registersSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSets.add(registersSet);

        LineData lineData = new LineData(dataSets);
        lineData.setDrawValues(true);
        lineData.setValueTextColor(R.color.colorPrimaryDark);
        lineData.setValueTextSize(10f);

        final String[] chartXValuesArray = datePointer.getDaysOfTheWeek();
        ChartTimeAxisFormatter formatter = new ChartTimeAxisFormatter();
        formatter.setFormatData(chartXValuesArray);

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        xAxis.setTextSize(10f);

        YAxis left = chart.getAxisLeft();
        left.setAxisMinimum(0f);
        left.setTextSize(10f);

        YAxis right = chart.getAxisRight();
        right.setEnabled(false);

        chart.setData(lineData);

        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        chart.setLogEnabled(false);

        chart.animateY(ANIMATION_DELAY);
    }
}
