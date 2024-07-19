package com.malicankaya.yemekkitabi.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.malicankaya.yemekkitabi.adapter.TarifAdapter
import com.malicankaya.yemekkitabi.databinding.FragmentListeBinding
import com.malicankaya.yemekkitabi.model.Tarif
import com.malicankaya.yemekkitabi.roomdb.TarifDAO
import com.malicankaya.yemekkitabi.roomdb.TarifDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ListeFragment : Fragment() {

    private var _binding : FragmentListeBinding? = null
    private val binding get() = _binding!!

    private lateinit var db : TarifDatabase
    private lateinit var tarifDAO: TarifDAO

    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler").build()
        tarifDAO = db.tarifDAO()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListeBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { tarifEkle(it) }
        binding.yemekTarifiRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        verileriAl()
    }

    fun tarifEkle(view : View){
        val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(false,0)
        Navigation.findNavController(view).navigate(action)
    }

    fun verileriAl(){
        mDisposable.add(
            tarifDAO.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForGetAll)
        )
    }

    fun handleResponseForGetAll(tarifler : List<Tarif>){

        val adapter = TarifAdapter(tarifler)
        binding.yemekTarifiRecyclerView.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}