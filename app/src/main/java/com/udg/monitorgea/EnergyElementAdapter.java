package com.udg.monitorgea;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class EnergyElementAdapter extends RecyclerView.Adapter<EnergyElementAdapter.EnergyElementViewHolder> implements View.OnClickListener
{
    private ArrayList<EnergyElement> elements;
    private View.OnClickListener listener;

    public EnergyElementAdapter(ArrayList<EnergyElement> elements)
    {
        this.elements = elements;
    }

    @Override
    public int getItemCount()
    {
        return elements.size();
    }

    @NonNull
    @Override
    public EnergyElementViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_card, viewGroup, false);

        EnergyElementViewHolder energyElementViewHolder = new EnergyElementViewHolder(v);
        v.setOnClickListener(this);

        return energyElementViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EnergyElementViewHolder energyElementViewHolder, int i)
    {
        EnergyElement energyElement = elements.get(i);
        energyElementViewHolder.name.setText(energyElement.getName());

        String unidades = "";
        Integer resource = null;

        switch (i)
        {
            case MainActivity.GAS_IDX:
                energyElementViewHolder.value.setText(energyElement.getValue().toString());
                unidades = "%";
                resource = R.drawable.gas_icon;
                break;

            case MainActivity.ELEC_IDX:
                if (energyElement.getValue() != "--") energyElementViewHolder.value.setText(Constants.DOUBLE_FORMAT.format(energyElement.getValue()));
                else energyElementViewHolder.value.setText(energyElement.getValue().toString());
                unidades = " kW/h";
                resource = R.drawable.power_icon;
                break;

            case MainActivity.AGUA_IDX:
                if (energyElement.getValue() != "--") energyElementViewHolder.value.setText(Constants.DOUBLE_FORMAT.format(energyElement.getValue()));
                else energyElementViewHolder.value.setText(energyElement.getValue().toString());
                unidades = " mL/m";
                resource = R.drawable.water_icon_3;
                break;
        }

        if (energyElement.isSetting())
        {
            energyElementViewHolder.progressBar.setVisibility(View.VISIBLE);
            energyElementViewHolder.name.setVisibility(View.INVISIBLE);
            energyElementViewHolder.value.setVisibility(View.INVISIBLE);
            energyElementViewHolder.icon.setVisibility(View.INVISIBLE);
        }
        else
        {
            energyElementViewHolder.value.append(unidades);
            if (resource != null) energyElementViewHolder.icon.setImageResource(resource);

            energyElementViewHolder.name.setVisibility(View.VISIBLE);
            energyElementViewHolder.value.setVisibility(View.VISIBLE);
            energyElementViewHolder.icon.setVisibility(View.VISIBLE);
            energyElementViewHolder.progressBar.setVisibility(View.GONE);
        }
    }

    public void setListener(View.OnClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onClick(View v)
    {
        if (listener != null)
        {
            listener.onClick(v);
        }
    }

    static class EnergyElementViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView icon;
        private TextView value;
        private TextView name;
        private ProgressBar progressBar;

        EnergyElementViewHolder(@NonNull View itemView)
        {
            super(itemView);

            name = itemView.findViewById(R.id.element_name);
            value = itemView.findViewById(R.id.value);
            progressBar = itemView.findViewById(R.id.progress_bar);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
