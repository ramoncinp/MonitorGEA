package com.udg.monitorgea;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    //Constantes
    public static final int GAS_IDX = 0;
    public static final int ELEC_IDX = 1;
    public static final int AGUA_IDX = 2;

    //Views
    private RecyclerView energyElementsList;

    //Objetos
    private ArrayList<EnergyElement> energyElements;
    private EnergyElementAdapter elementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        energyElementsList = findViewById(R.id.energy_elements_list);
        showElements();
        initDatabase();
    }

    private void showElements()
    {
        //Crear lista
        energyElements = new ArrayList<>();

        //Crear elementos
        EnergyElement gas = new EnergyElement();
        gas.setName("Gas");

        EnergyElement electricidad = new EnergyElement();
        electricidad.setName("Electricidad");

        EnergyElement agua = new EnergyElement();
        agua.setName("Agua");

        energyElements.add(gas);
        energyElements.add(electricidad);
        energyElements.add(agua);

        //Crear adaptador
        elementAdapter = new EnergyElementAdapter(energyElements);
        elementAdapter.setListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EnergyElement energyElement = energyElements.get(energyElementsList.getChildAdapterPosition(v));

                String message = energyElement.getName() + " seleccionado";
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

        //Definir a lista
        energyElementsList.setAdapter(elementAdapter);
        energyElementsList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initDatabase()
    {
        DatabaseReference sensors = FirebaseDatabase.getInstance().getReference().child("sensores");
        sensors.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                parseSensorsData(dataSnapshot);
                refreshData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.d("Datos", "Error al obtener datos", databaseError.toException());
                refreshData();
            }
        });
    }

    private void parseSensorsData(DataSnapshot dataSnapshot)
    {
        //Obtener valores actuales de sensores
        Integer gas;
        Double elec, agua;

        if ((gas = dataSnapshot.child("gas/valor").getValue(Integer.class)) != null)
        {
            energyElements.get(GAS_IDX).setValue(gas);
        }

        if ((elec = dataSnapshot.child("electricidad/valor").getValue(Double.class)) != null)
        {
            energyElements.get(ELEC_IDX).setValue(elec);
        }

        if ((agua = dataSnapshot.child("agua/valor").getValue(Double.class)) != null)
        {
            energyElements.get(AGUA_IDX).setValue(agua);
        }
    }

    private void refreshData()
    {
        energyElements.get(GAS_IDX).setSetting(false);
        energyElements.get(ELEC_IDX).setSetting(false);
        energyElements.get(AGUA_IDX).setSetting(false);

        //Actualziar lista
        elementAdapter.notifyDataSetChanged();
    }
}
