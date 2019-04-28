package com.udg.monitorgea;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Consumption extends AppCompatActivity
{
    //Views
    private ConstraintLayout chartLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);
    }
}
