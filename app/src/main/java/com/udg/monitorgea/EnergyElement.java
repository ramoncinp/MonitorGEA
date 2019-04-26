package com.udg.monitorgea;

public class EnergyElement
{
    private String name;
    private Object value = "--";
    private boolean setting = true;

    public EnergyElement()
    {
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }

    public boolean isSetting()
    {
        return setting;
    }

    public void setSetting(boolean setting)
    {
        this.setting = setting;
    }
}
