package com.udg.monitorgea;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    //Constantes
    public static final int GAS_IDX = 0;
    public static final int ELEC_IDX = 1;
    public static final int AGUA_IDX = 2;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setClickListeners();
    }

    private void setClickListeners()
    {
        CardView gasCv = findViewById(R.id.gas_cv);
        CardView elecCv = findViewById(R.id.elec_cv);
        CardView aguaCv = findViewById(R.id.waterCv);

        gasCv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                progressDialog = ProgressDialog.show(MainActivity.this, "", "Cargando...", true);

                Intent gasIntent = new Intent(MainActivity.this, DetailActivity.class);
                gasIntent.putExtra("element", GAS_IDX);
                startActivity(gasIntent);
            }
        });

        elecCv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                progressDialog = ProgressDialog.show(MainActivity.this, "", "Cargando...", true);

                Intent elecIntent = new Intent(MainActivity.this, DetailActivity.class);
                elecIntent.putExtra("element", ELEC_IDX);
                startActivity(elecIntent);
            }
        });

        aguaCv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                progressDialog = ProgressDialog.show(MainActivity.this, "", "Cargando...", true);

                Intent aguaIntent = new Intent(MainActivity.this, DetailActivity.class);
                aguaIntent.putExtra("element", AGUA_IDX);
                startActivity(aguaIntent);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (progressDialog != null)
        {
            if (progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
        }
    }
}
