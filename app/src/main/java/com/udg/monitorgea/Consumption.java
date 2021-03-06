package com.udg.monitorgea;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Consumption extends AppCompatActivity
{
    //Constantes
    private final static String TAG = Consumption.class.getSimpleName();
    private final static int ANIMATION_DELAY = 200;
    public final static String SENSOR_TYPE = "sensor";

    //Variables
    private int sensorType = MainActivity.GAS_IDX;
    private Double total = 0d;
    private float referenceValue;
    private String registersName = "";
    private String registersDesc = "";

    //Listar
    private ArrayList<Register> registers;

    //Views
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton monthButton;
    private FloatingActionButton weekButton;
    private FloatingActionButton dayButton;
    private ImageView arrowLeft;
    private ImageView arrowRight;
    private LinearLayout content;
    private ProgressBar progressBar;
    private TextView noRgisters;
    private TextView dateTitleTv;
    private TextView totalTv;

    //Objetos
    private DatabaseReference sensorRegisters;

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
                    registersDesc = "Nivel gas";
                    break;

                case MainActivity.ELEC_IDX:
                    registersName = "electricidad";
                    registersDesc = "Consumo electricidad";
                    break;

                case MainActivity.AGUA_IDX:
                    registersName = "agua";
                    registersDesc = "Consumo agua";
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
        dateTitleTv = findViewById(R.id.registers_date_title);
        arrowLeft = findViewById(R.id.arrowToLeft);
        arrowRight = findViewById(R.id.arrowToRight);
        noRgisters = findViewById(R.id.no_registers);
        content = findViewById(R.id.registers_content);
        floatingActionMenu = findViewById(R.id.menu_registers_dates);
        monthButton = findViewById(R.id.fabMonth);
        weekButton = findViewById(R.id.fabWeek);
        dayButton = findViewById(R.id.fabDay);
        totalTv = findViewById(R.id.total);
        progressBar = findViewById(R.id.progress_bar);

        arrowLeft.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                datePointer.setOnePeriodBefore();
                setDateTitle();
                execQuery();
            }
        });

        arrowRight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                datePointer.setOnePeriodAfter();
                setDateTitle();
                execQuery();
            }
        });

        floatingActionMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener()
        {
            @Override
            public void onMenuToggle(boolean opened)
            {
                if (opened)
                {
                    int green = getResources().getColor(R.color.green_money);
                    int darkGreen = getResources().getColor(R.color.dark_green);
                    int red = getResources().getColor(R.color.colorPrimary);
                    int darkRed = getResources().getColor(R.color.colorPrimaryDark);

                    if (datePointer.getDateRangeType() == DatePointer.BYDAY)
                    {
                        dayButton.setColorNormal(green);
                        dayButton.setColorPressed(darkGreen);
                        weekButton.setColorNormal(red);
                        weekButton.setColorPressed(darkRed);
                        monthButton.setColorNormal(red);
                        monthButton.setColorPressed(darkRed);
                    }
                    else if (datePointer.getDateRangeType() == DatePointer.BYWEEK)
                    {
                        dayButton.setColorNormal(red);
                        dayButton.setColorPressed(darkRed);
                        weekButton.setColorNormal(green);
                        weekButton.setColorPressed(darkGreen);
                        monthButton.setColorNormal(red);
                        monthButton.setColorPressed(darkRed);
                    }
                    else
                    {
                        dayButton.setColorNormal(red);
                        dayButton.setColorPressed(darkRed);
                        weekButton.setColorNormal(red);
                        weekButton.setColorPressed(darkRed);
                        monthButton.setColorNormal(green);
                        monthButton.setColorPressed(darkGreen);
                    }
                }
            }
        });

        dayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (datePointer.getDateRangeType() != DatePointer.BYDAY)
                {
                    datePointer.setDateRangeType(DatePointer.BYDAY);
                    setDateTitle();
                    execQuery();
                }
                floatingActionMenu.close(true);
            }
        });

        weekButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (datePointer.getDateRangeType() != DatePointer.BYWEEK)
                {
                    datePointer.setDateRangeType(DatePointer.BYWEEK);
                    setDateTitle();
                    execQuery();
                }
                floatingActionMenu.close(true);
            }
        });

        monthButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (datePointer.getDateRangeType() != DatePointer.BYMONTH)
                {
                    datePointer.setDateRangeType(DatePointer.BYMONTH);
                    setDateTitle();
                    execQuery();
                }
                floatingActionMenu.close(true);
            }
        });

        setDateTitle();
    }

    private void setDateTitle()
    {
        Date firstPurchaseDate = datePointer.getFirstPurchaseDate();
        StringBuilder dateTitle = new StringBuilder();

        if (datePointer.getDateRangeType() == DatePointer.BYDAY)
        {
            dateTitle.append(datePointer.getFirstDateOfCurrentPeriodString());

            if (datePointer.isSameDay(new Date())) //Comparar con la semana actual
            {
                arrowRight.setVisibility(View.INVISIBLE);
            }
            else
            {
                arrowRight.setVisibility(View.VISIBLE);
            }
        }
        else if (datePointer.getDateRangeType() == DatePointer.BYWEEK)
        {
            dateTitle.append(datePointer.getFirstDateOfCurrentPeriodString());
            dateTitle.append(" - ");
            dateTitle.append(datePointer.getLastDateOfCurrentPeriodString());

            if (datePointer.isSameWeekNumber(new Date())) //Comparar con la semana actual
            {
                arrowRight.setVisibility(View.INVISIBLE);
            }
            else
            {
                arrowRight.setVisibility(View.VISIBLE);
            }

            if (firstPurchaseDate != null)
            {
                if (datePointer.isSameWeekNumber(firstPurchaseDate)) //Comparar con la semana del
                // primer consumo
                {
                    arrowLeft.setVisibility(View.INVISIBLE);
                }
                else
                {
                    arrowLeft.setVisibility(View.VISIBLE);
                }
            }
        }
        else
        {
            dateTitle.append(datePointer.getCurrentMonth());
            if (datePointer.isSameMonth(new Date())) //Comparar con la semana actual
            {
                arrowRight.setVisibility(View.INVISIBLE);
            }
            else
            {
                arrowRight.setVisibility(View.VISIBLE);
            }

            if (firstPurchaseDate != null)
            {
                if (datePointer.isSameMonth(firstPurchaseDate)) //Comparar con la semana del
                // primer consumo
                {
                    arrowLeft.setVisibility(View.INVISIBLE);
                }
                else
                {
                    arrowLeft.setVisibility(View.VISIBLE);
                }
            }
        }

        dateTitleTv.setText(dateTitle.toString());
    }

    private void initDatabase()
    {
        //Preparar lista
        registers = new ArrayList<>();

        //Obtener registros
        sensorRegisters = FirebaseDatabase.getInstance().getReference(registersName);

        //Ejecutar Query
        execQuery();
    }

    private void execQuery()
    {
        noRgisters.setVisibility(View.GONE);
        content.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String startDate = datePointer.getFirstDateOfCurrentPeriodString();
        String finishDate = datePointer.getLastDateOfCurrentPeriodString();

        Log.d(TAG, "Obteniendo consumos de bd");
        Log.d(TAG, "Fecha Inicial -> " + startDate);
        Log.d(TAG, "Fecha final -> " + finishDate);

        //Crear query
        Query query = sensorRegisters.orderByChild("fecha").startAt(datePointer.getFirstDateOfCurrentPeriodEpoch()).endAt(datePointer.getLastDateOfCurrentPeriodEpoch()).limitToLast(7);
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                parseData(dataSnapshot);
                Log.d(TAG, "Resultado de Query:");
                Log.d(TAG, dataSnapshot.toString());

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(TAG, databaseError.getMessage() + "\n" + databaseError.getDetails());

                noRgisters.setVisibility(View.VISIBLE);
                content.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void parseData(DataSnapshot dataSnapshot)
    {
        //Si no hubo registros...
        if (dataSnapshot.getValue() == null)
        {
            noRgisters.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);
            totalTv.setVisibility(View.GONE);

            total = 0d;
            referenceValue = 0f;

            return;
        }

        //Limpiar lista
        registers.clear();

        for (DataSnapshot child : dataSnapshot.getChildren())
        {
            Object value;
            if (sensorType == MainActivity.GAS_IDX)
                value = child.child("valor").getValue(Integer.class);
            else value = child.child("valor").getValue(Double.class);

            Long epoch = child.child("fecha").getValue(Long.class);

            if (epoch != null)
            {
                Register register = new Register(value, epoch, child.getKey());
                registers.add(0, register);
            }
        }

        setTotal();
        setChartValues();
        noRgisters.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    private void setChartValues()
    {
        //Definir eje X
        final String[] chartXValuesArray;

        if (datePointer.getDateRangeType() == DatePointer.BYDAY)
        {
            chartXValuesArray = datePointer.getHoursOfDay();
        }
        else if (datePointer.getDateRangeType() == DatePointer.BYWEEK)
        {
            chartXValuesArray = datePointer.getDaysOfTheWeek();
        }
        else
        {
            chartXValuesArray = datePointer.getDaysOfTheMonth();
        }

        //Crear lista de "datasets"
        List<ILineDataSet> dataSets = new ArrayList<>();

        //Crear objeto de calendario
        Calendar c = Calendar.getInstance();

        //Obtener el número de la semana actual
        c.setTime(new Date());

        //Crear lista de "Entries"
        List<Entry> mRegisters = new ArrayList<>();

        //Inicializar listas de datos
        for (int i = 0; i < datePointer.getDateRangeLength(); i++)
        {
            mRegisters.add(new Entry(i, 0));
        }

        //Si no es medicion de gas,
        if (sensorType != MainActivity.GAS_IDX && registers.size() > 1)
        {
            //Obtener registro más bajo
            Register lowerRegister = registers.get(registers.size() - 1);
            referenceValue = Float.parseFloat(Constants.DOUBLE_FORMAT.format(lowerRegister.getValor()));
            //Agregar registro más bajo
            addDataToChartColumn(lowerRegister, mRegisters);
        }

        //Agregar datos obtenidos
        for (Register register : registers)
        {
            addDataToChartColumn(register, mRegisters);
        }

        //Crear dataSet del tipo de producto y añadirlo a su lista
        LineDataSet registersSet = new LineDataSet(mRegisters, registersDesc);
        registersSet.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        registersSet.setLineWidth(5);
        registersSet.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        registersSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSets.add(registersSet);

        LineData lineData = new LineData(dataSets);
        lineData.setDrawValues(true);
        lineData.setValueTextColor(R.color.colorPrimaryDark);
        lineData.setValueTextSize(10f);

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

    private void setTotal()
    {
        if (sensorType != MainActivity.GAS_IDX)
        {
            if (registers.size() == 1)
            {
                totalTv.setVisibility(View.GONE);
                referenceValue = 0f;
                total = 0d;
            }
            else
            {
                Double valorFinal = (Double) registers.get(0).getValor();
                Double valorInicial = (Double) registers.get(registers.size() - 1).getValor();

                total = valorFinal - valorInicial;
                referenceValue = (float) (valorInicial * 1.0f);
            }

            String unit = "";
            if (sensorType == MainActivity.AGUA_IDX) unit = " L";
            else if (sensorType == MainActivity.ELEC_IDX) unit = " kW/h";

            String text = "Total: " + Constants.DOUBLE_FORMAT.format(total) + unit;
            totalTv.setVisibility(View.VISIBLE);
            totalTv.setText(text);
        }
    }

    private void addDataToChartColumn(Register register, List<Entry> registersEntries)
    {
        int listIndex = getRegisterChartIndex(register);

        Entry entry = registersEntries.get(listIndex);
        float lastValue = entry.getY();

        if (lastValue == 0)
        {
            if (sensorType == MainActivity.GAS_IDX)
            {
                //Si no se ha agregado un valor a esa columna, agregar
                entry.setY(Float.parseFloat(register.getValor().toString()));
                registersEntries.remove(listIndex);
                registersEntries.add(listIndex, entry);
            }
            else
            {
                float value = Float.parseFloat(Constants.DOUBLE_FORMAT.format(register.getValor()));
                value = value - referenceValue;
                entry.setY(value);
                registersEntries.remove(listIndex);
                registersEntries.add(listIndex, entry);
            }
        }
    }

    private int getRegisterChartIndex(Register register)
    {
        //Convertir epoch a objeto Date
        Date registerDate = new Date(register.getFecha() * 1000L);

        //Crear instancia de calendario
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(registerDate);

        if (datePointer.getDateRangeType() == DatePointer.BYDAY)
        {
            return calendar.get(Calendar.HOUR_OF_DAY);
        }
        else if (datePointer.getDateRangeType() == DatePointer.BYWEEK)
        {
            int idx = (calendar.get(Calendar.DAY_OF_WEEK)) - 1;
            if (idx == 0) idx = 7;
            return idx;
        }
        else
        {
            return calendar.get(Calendar.DAY_OF_MONTH);
        }
    }
}
