package com.malicankaya.yemekkitabi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.malicankaya.yemekkitabi.databinding.RecyclerRowBinding
import com.malicankaya.yemekkitabi.model.Tarif
import com.malicankaya.yemekkitabi.view.ListeFragmentDirections

class TarifAdapter(val tarifler : List<Tarif>) : RecyclerView.Adapter<TarifAdapter.TarifHolder>() {

    class TarifHolder (val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarifHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TarifHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return tarifler.size
    }

    override fun onBindViewHolder(holder: TarifHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = tarifler[position].isim
        holder.itemView.setOnClickListener {
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(true , tarifler[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }
}