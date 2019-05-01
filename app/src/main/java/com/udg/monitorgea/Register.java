package com.udg.monitorgea;

public class Register
{
    private Object valor;
    private long fecha;
    private String uuid;

    public Register(Object valor, long fecha, String uuid)
    {
        this.valor = valor;
        this.fecha = fecha;
        this.uuid = uuid;
    }

    public Object getValor()
    {
        return valor;
    }

    public long getFecha()
    {
        return fecha;
    }

    public String getUuid()
    {
        return uuid;
    }
}
