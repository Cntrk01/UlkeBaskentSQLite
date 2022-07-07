package com.example.ulkelerinbaskenti;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ulkelerinbaskenti.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class UlkeAdapter extends RecyclerView.Adapter<UlkeAdapter.UlkeViewHolder> {

    ArrayList<Ulke> arrayList=new ArrayList<>();
    public UlkeAdapter( ArrayList<Ulke> arrayList){ //Main İçerisinden arrayListe eklenen verileri bu fonksiyona verilerimizi göndericez
        this.arrayList=arrayList;
        notifyDataSetChanged();
    }

    public class UlkeViewHolder extends RecyclerView.ViewHolder {
        private RecyclerRowBinding binding;
        public UlkeViewHolder(RecyclerRowBinding binding) { //xml gorunumunu alıyor
            super(binding.getRoot()); //görünümü alıyoruz
            this.binding=binding; //yukarıdaki parametre ile fonksiyon parametresi olan bindingleri eşitledim
        }
    }

    @NonNull
    @Override
    public UlkeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding rw=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new UlkeViewHolder(rw);
    }

    @Override
    public void onBindViewHolder(UlkeViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.recyclerRow.setText(arrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.itemView.getContext(),DetailActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("ulkeId",arrayList.get(position).id); //id gönderiyoruz
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


}
