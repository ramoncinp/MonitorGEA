package com.udg.monitorgea;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class ChartTimeAxisFormatter implements IAxisValueFormatter
{
    private String[] formatData;

    @Override
    public String getFormattedValue(float value, AxisBase axis)
    {
        return formatData[(int)value];
    }

    public void setFormatData(String[] formatData)
    {
        this.formatData = formatData;
    }

    public String[] formatData()
    {
        return formatData;
    }
}
