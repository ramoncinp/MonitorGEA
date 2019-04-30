package com.udg.monitorgea;

public class Register
{
    private Object valor;
    private long fecha;

    public Register(Object valor, long fecha)
    {
        this.valor = valor;
        this.fecha = fecha;
    }

    public Object getValor()
    {
        return valor;
    }

    public long getFecha()
    {
        return fecha;
    }
}
