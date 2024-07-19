package com.malicankaya.yemekkitabi.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Database
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.malicankaya.yemekkitabi.databinding.FragmentTarifBinding
import com.malicankaya.yemekkitabi.model.Tarif
import com.malicankaya.yemekkitabi.roomdb.TarifDAO
import com.malicankaya.yemekkitabi.roomdb.TarifDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class TarifFragment : Fragment() {

    private var _binding : FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel : Uri? = null
    private var secilenBitmap : Bitmap? = null
    private var secilenTarif : Tarif? = null

    private lateinit var db : TarifDatabase
    private lateinit var tarifDAO: TarifDAO

    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
        db = Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler").build()
        tarifDAO = db.tarifDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener { gorselSec(it) }
        binding.buttonSil.setOnClickListener { sil(it) }
        binding.buttonKaydet.setOnClickListener { kaydet(it) }

        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).eskiMi

            if(bilgi){
                //eski tarif gösterilecek
                binding.buttonSil.isEnabled = true
                binding.buttonKaydet.isEnabled = false
                binding.yemekIsimText.isFocusable = false
                binding.yemekMalzemeText.isFocusable = false
                val id = TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    tarifDAO.getById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponseForGetById)
                )

            }else{
                //yeni tarif eklenecek
                secilenTarif = null
                binding.buttonSil.isEnabled = false
                binding.buttonKaydet.isEnabled = true
                binding.yemekIsimText.setText("")
                binding.yemekMalzemeText.setText("")
            }
        }
    }

    private fun handleResponseForGetById(tarif:Tarif){
        binding.yemekIsimText.setText(tarif.isim)
        binding.yemekMalzemeText.setText(tarif.malzeme)
        val bitmap = BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)
        secilenTarif = tarif
    }

    fun kaydet(view : View) {
        val isim = binding.yemekIsimText.text.toString()
        val malzeme = binding.yemekMalzemeText.text.toString()

        if (secilenBitmap != null){
            val kucukBitmap = kucukGorselOlustur(secilenBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val gorselByteDizisi = outputStream.toByteArray()

            val tarif = Tarif(isim,malzeme,gorselByteDizisi)

            mDisposable.add(
                tarifDAO.insert(tarif)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }

    }

    fun handleResponseForInsert(){
        //önceki fragmenta git
        val intent = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(intent)
    }

    fun sil(view : View){
        if(secilenTarif != null){
            mDisposable.add(
                tarifDAO.delete(secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }
    }

    fun gorselSec (view : View){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş, izin istenecek
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Galeriden görselin seçilmesi için izne gerek duyuluyor.",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",
                        View.OnClickListener {
                            //izin istenecek
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                }else {
                    //izin istenecek
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                //izin verilmiş, galeriye gidilecek
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }else{
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş, izin istenecek
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Galeriden görselin seçilmesi için izne gerek duyuluyor.",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",
                        View.OnClickListener {
                            //izin istenecek
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                }else {
                    //izin istenecek
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                //izin verilmiş, galeriye gidilecek
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }

    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

            if (result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    secilenGorsel = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    }catch (e: Exception){
                        print(e.localizedMessage)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if (result){
                //izin verildi, galeriye gidilebilir
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //izin verilmedi
                Toast.makeText(requireContext(),"İzin verilmedi",Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun kucukGorselOlustur(kullanicininSectigiBitmap : Bitmap, maxBoyut : Int) : Bitmap{
        var gorselWidth = kullanicininSectigiBitmap.width
        var gorselHeight = kullanicininSectigiBitmap.height

        var gorselOrani = gorselWidth.toDouble() / gorselHeight.toDouble()

        if (gorselOrani > 1){
            //yatay
            gorselWidth = maxBoyut
            val degisenYukseklik = gorselWidth / gorselOrani
            gorselHeight = degisenYukseklik.toInt()
        }else{
            //dikey
            gorselHeight = maxBoyut
            val degisenGenislik = gorselHeight * gorselOrani
            gorselWidth = degisenGenislik.toInt()
        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,gorselWidth,gorselHeight,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}