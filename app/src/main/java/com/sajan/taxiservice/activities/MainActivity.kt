package com.sajan.taxiservice.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.sajan.bktguide.storage.PrefsImageUri
import com.sajan.taxiservice.R
import com.sajan.taxiservice.adapters.*
import com.sajan.taxiservice.controller.InfoPostController
import com.sajan.taxiservice.dataModels.DriverInfoDto
import com.sajan.taxiservice.dataModels.TaxiInfoDto
import com.sajan.taxiservice.protocols.OnItemClickListener
import com.sajan.taxiservice.protocols.OnPostResponseListener
import com.sajan.taxiservice.storage.PrefLogin
import com.sajan.taxiservice.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var driverInfoDto: DriverInfoDto? = null
    private var taxiDetailDto: TaxiInfoDto? = null
    private lateinit var zoneBottomSheet: BottomSheetBehavior<View>
    private lateinit var zoneArrayList: ArrayList<String>
    private lateinit var vehicleTypeBottomSheet: BottomSheetBehavior<View>
    private lateinit var vehicleArrayList: ArrayList<String>
    private lateinit var stateBottomSheet: BottomSheetBehavior<View>
    private lateinit var stateArrayList: ArrayList<String>
    private lateinit var vehicleMgmtCodeBottomSheet: BottomSheetBehavior<View>
    private lateinit var vehicleMgmtCodeArrayList: ArrayList<String>
    private lateinit var vehicleTypeNewBottomSheet: BottomSheetBehavior<View>
    private lateinit var vehicleTypeNewArrayList: ArrayList<String>

    private var fileName: String? = null
    private var imageUriV2: Uri? = null
    private var storageCheckPermission: Int = 0
    private var cameraCheckPermission: Int = 0
    private var permissionRequestCode: Int = 345
    private val requestCodeCamera = 25
    private val requestCodeGallery = 27
    private var imageUri: Uri? = null

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        driverInfoDto = intent.getParcelableExtra("dto")!!
        taxiDetailDto = TaxiInfoDto()
        setUpActionBar()
        setUpProgressDialog()
        initView()
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
    }

    private fun setUpProgressDialog() {
        progressDialog = ProgressDialog(this@MainActivity)
        progressDialog!!.setMessage("Updating....")
        progressDialog!!.setCancelable(false)
    }

    private fun initView() {
        viewGone(btnDone, btnCancel,ivEdit)

        if (PrefsImageUri(this@MainActivity).fileName != null &&
            (PrefsImageUri(this@MainActivity).fileName)!!.isNotEmpty()
        ) {
            imageUriV2 = PrefsImageUri(this@MainActivity).imgUri!!.toUri()
            this.fileName = PrefsImageUri(this@MainActivity).fileName
            val image = imageUriV2
            var bitmap: Bitmap? = null
            val imageStream: InputStream
            try {
                imageStream = contentResolver.openInputStream(image!!)!!
                bitmap = BitmapFactory.decodeStream(imageStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ivUpdateProfile.setImageBitmap(bitmap)
        }

        taxiDetailDto = driverInfoDto!!.taxiDetailDto
        if (taxiDetailDto!!.vehicleRegisterSystem == "old") {
            if (btnDone.text == "Update") {
                disableViews(
                    etUpdateName,
                    etUpdateAddress,
                    etZone,
                    etLotNumber,
                    etVehicleType,
                    etTaxiNumber,
                    ivEdit
                )
            }
            inVisibleViews(clNew)
            visibleViews(clOld)
        } else if (taxiDetailDto!!.vehicleRegisterSystem == "new") {
            if (btnDone.text == "Update") {
                disableViews(
                    etUpdateName,
                    etUpdateAddress,
                    etState,
                    etVehicleManagementCode,
                    etLotNumberNew,
                    etVehicleTypeNew,
                    etTaxiNumberNew,
                    ivEdit
                )
            }
            inVisibleViews(clOld)
            visibleViews(clNew)
        }
        btnDone.setOnClickListener(this)
        etZone.setOnClickListener(this)
        etVehicleType.setOnClickListener(this)
        etState.setOnClickListener(this)
        etVehicleManagementCode.setOnClickListener(this)
        etVehicleTypeNew.setOnClickListener(this)
        btnCancel.setOnClickListener(this)
        ivEdit.setOnClickListener(this)

        zoneBottomSheet = BottomSheetBehavior.from(mcvZoneSheet)
        zoneBottomSheet.peekHeight = 0
        rvZone.layoutManager =
            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        setZoneList()

        vehicleTypeBottomSheet = BottomSheetBehavior.from(mcvVehicleType)
        vehicleTypeBottomSheet.peekHeight = 0
        rvVehicleType.layoutManager =
            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        setVehicleTypeList()

        stateBottomSheet = BottomSheetBehavior.from(mcvState)
        stateBottomSheet.peekHeight = 0
        rvState.layoutManager =
            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        setStateList()

        vehicleMgmtCodeBottomSheet = BottomSheetBehavior.from(mcvVehicleMgmtCode)
        vehicleMgmtCodeBottomSheet.peekHeight = 0
        rvVehicleMgmtCode.layoutManager =
            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        setVehicleMgmtCodeList()

        vehicleTypeNewBottomSheet = BottomSheetBehavior.from(mcvVehicleTypeNew)
        vehicleTypeNewBottomSheet.peekHeight = 0
        rvVehicleTypeNew.layoutManager =
            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        setVehicleTypeNewList()

        etUpdateName.setText(driverInfoDto!!.name)
        etUpdateAddress.setText(driverInfoDto!!.address)
        if (taxiDetailDto!!.vehicleRegisterSystem == "old") {
            val zc = zoneToNepali(taxiDetailDto!!.zoneCode!!)
            etZone.setText(zc)
            etLotNumber.setText(taxiDetailDto!!.lotNumber)
            val vt = vehicleTypeToNepali(taxiDetailDto!!.vehicleType!!)
            etVehicleType.setText(vt)
            etTaxiNumber.setText(taxiDetailDto!!.taxiNumber)
        } else if (taxiDetailDto!!.vehicleRegisterSystem == "new") {
            etState.setText(taxiDetailDto!!.state)
            etVehicleManagementCode.setText(taxiDetailDto!!.vehicleManagementCode)
            etLotNumberNew.setText(taxiDetailDto!!.lotNumber)
            etVehicleTypeNew.setText(taxiDetailDto!!.vehicleType)
            etTaxiNumberNew.setText(taxiDetailDto!!.taxiNumber)
        }
    }

    private fun setVehicleTypeNewList() {
        val arrayVehicleTypeNew = resources.getStringArray(R.array.vehicleTypeNew)
        vehicleTypeNewArrayList = ArrayList()
        vehicleTypeNewArrayList.clear()
        for (i in arrayVehicleTypeNew) {
            vehicleTypeNewArrayList.add(i)
        }
        rvVehicleTypeNew.adapter = VehicleTypeNewAdapter(vehicleTypeNewArrayList).apply {
            onItemClickListener = object : OnItemClickListener {
                override fun onItemClicked(position: Int) {
                    vehicleTypeNewBottomSheet.isCollapsed = true
                    val selectedPosition = vehicleTypeNewArrayList[position]
                    if (selectedPosition != etVehicleTypeNew.text.toString()) {
                        etVehicleTypeNew.setText(selectedPosition)
                    }
                }
            }
        }
    }

    private fun setVehicleMgmtCodeList() {
        val arrayVehicleMgmtCode = resources.getStringArray(R.array.vehicleMgmtCode)
        vehicleMgmtCodeArrayList = ArrayList()
        vehicleMgmtCodeArrayList.clear()
        for (i in arrayVehicleMgmtCode) {
            vehicleMgmtCodeArrayList.add(i)
        }
        rvVehicleMgmtCode.adapter = VehicleMgmtCodeAdapter(vehicleMgmtCodeArrayList).apply {
            onItemClickListener = object : OnItemClickListener {
                override fun onItemClicked(position: Int) {
                    vehicleMgmtCodeBottomSheet.isCollapsed = true
                    val selectedPosition = vehicleMgmtCodeArrayList[position]
                    if (selectedPosition != etVehicleManagementCode.text.toString()) {
                        etVehicleManagementCode.setText(selectedPosition)
                    }
                }
            }
        }
    }

    private fun setStateList() {
        val arrayState = resources.getStringArray(R.array.state)
        stateArrayList = ArrayList()
        stateArrayList.clear()
        for (i in arrayState) {
            stateArrayList.add(i)
        }
        rvState.adapter = StateAdapter(stateArrayList).apply {
            onItemClickListener = object : OnItemClickListener {
                override fun onItemClicked(position: Int) {
                    stateBottomSheet.isCollapsed = true
                    val selectedPosition = stateArrayList[position]
                    if (selectedPosition != etState.text.toString()) {
                        etState.setText(selectedPosition)
                    }
                }
            }
        }
    }

    private fun setVehicleTypeList() {
        val arrayVehicleType = resources.getStringArray(R.array.vehicleType)
        vehicleArrayList = ArrayList()
        vehicleArrayList.clear()
        for (i in arrayVehicleType) {
            vehicleArrayList.add(i)
        }
        rvVehicleType.adapter = VehicleTypeAdapter(vehicleArrayList).apply {
            onItemClickListener = object : OnItemClickListener {
                override fun onItemClicked(position: Int) {
                    vehicleTypeBottomSheet.isCollapsed = true
                    val selectedPosition = vehicleArrayList[position]
                    if (selectedPosition != etVehicleType.text.toString()) {
                        etVehicleType.setText(selectedPosition)
                    }
                }
            }
        }
    }

    private fun setZoneList() {
        val arrayZoneCode = resources.getStringArray(R.array.ZoneCode)
        zoneArrayList = ArrayList()
        zoneArrayList.clear()
        for (i in arrayZoneCode) {
            zoneArrayList.add(i)
        }
        rvZone.adapter = ZoneAdapter(zoneArrayList).apply {
            onItemClickListener = object : OnItemClickListener {
                override fun onItemClicked(position: Int) {
                    zoneBottomSheet.isCollapsed = true
                    val selectedPosition = zoneArrayList[position]
                    if (selectedPosition != etZone.text.toString()) {
                        etZone.setText(selectedPosition)
                    }
                }
            }
        }
    }

    private var driverInfoDtoV2: DriverInfoDto? = null
    private var taxiDetailDtoV2: TaxiInfoDto? = null
    private var driverInfoDtoV3: DriverInfoDto? = null
    private var taxiDetailDtoV3: TaxiInfoDto? = null

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivEdit -> {
                if (storageCheckPermission != PackageManager.PERMISSION_GRANTED ||
                    cameraCheckPermission != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                        ), permissionRequestCode
                    )
                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setItems(
                            arrayOf("Take a photo", "Choose from Gallery", "Remove Photo")
                        ) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    imageUri = getFileUri(
                                        this@MainActivity,
                                        getImageFile(
                                            this@MainActivity,
                                            UUID.randomUUID().toString()
                                        )
                                    )
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                                    cameraIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    if (cameraIntent.resolveActivity(this@MainActivity.packageManager) != null) {
                                        startActivityForResult(
                                            Intent.createChooser(cameraIntent, "Take picture"),
                                            requestCodeCamera
                                        )
                                    }
                                }
                                1 -> {
                                    val intent = Intent()
                                    intent.type = "image/*"
                                    intent.action = Intent.ACTION_GET_CONTENT
                                    startActivityForResult(
                                        Intent.createChooser(
                                            intent,
                                            "Select Picture"
                                        ), requestCodeGallery
                                    )
                                }
                                2 -> {
                                    imageUriV2 = null
                                    fileName = null
                                    ivUpdateProfile.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@MainActivity,
                                            R.drawable.ic_profile
                                        )
                                    )
                                }
                            }
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }

            R.id.btnDone -> {
                if (taxiDetailDto!!.vehicleRegisterSystem == "old") {
                    enableViews(
                        etUpdateName,
                        etUpdateAddress,
                        etZone,
                        etLotNumber,
                        etVehicleType,
                        etTaxiNumber,
                        ivEdit
                    )
                    if (allValidProfile()) {
                        if (allOldFieldValid()) {
                            val id = driverInfoDto!!.driverId
                            val name = etUpdateName.text.toString()
                            val address = etUpdateAddress.text.toString()
                            val contact = driverInfoDto!!.contact

                            val zoneCode = etZone.text.toString()
                            val zone = zoneToEnglish(zoneCode)
                            val lotNumber = etLotNumber.text.toString()
                            val vehicleType = etVehicleType.text.toString()
                            val vt = vehicleTypeToEnglish(vehicleType)
                            val taxiNumber = etTaxiNumber.text.toString()

                            taxiDetailDtoV2 = TaxiInfoDto().apply {
                                this.state = null
                                this.vehicleManagementCode = null
                                this.zoneCode = zone
                                this.lotNumber = lotNumber
                                this.vehicleType = vt
                                this.taxiNumber = taxiNumber
                                this.vehicleRegisterSystem = "old"
                            }

                            driverInfoDtoV2 = DriverInfoDto().apply {
                                this.driverId = id
                                this.name = name
                                this.address = address
                                this.contact = contact
                                this.taxiDetailDto = taxiDetailDtoV2
                            }

                            val paramsBuilder = getParamOld(driverInfoDtoV2!!)
                            updateInformation(driverInfoDtoV2!!, paramsBuilder)
                        }
                    }
                } else if (taxiDetailDto!!.vehicleRegisterSystem == "new") {
                    enableViews(
                        etUpdateName,
                        etUpdateAddress,
                        etState,
                        etVehicleManagementCode,
                        etLotNumberNew,
                        etVehicleTypeNew,
                        etTaxiNumberNew,
                        ivEdit
                    )
                    if (allValidProfile()) {
                        if (allNewFieldValid()) {
                            val id = driverInfoDto!!.driverId
                            val name = etUpdateName.text.toString()
                            val address = etUpdateAddress.text.toString()
                            val contact = driverInfoDto!!.contact

                            val state = etState.text.toString()
                            val vehicleManagementCode =
                                etVehicleManagementCode.text.toString()
                            val lotNumberNew = etLotNumberNew.text.toString()
                            val vehicleTypeNew = etVehicleTypeNew.text.toString()
                            val taxiNumberNew = etTaxiNumberNew.text.toString()

                            taxiDetailDtoV2 = TaxiInfoDto().apply {
                                this.zoneCode = null
                                this.state = state
                                this.vehicleManagementCode = vehicleManagementCode
                                this.lotNumber = lotNumberNew
                                this.vehicleType = vehicleTypeNew
                                this.taxiNumber = taxiNumberNew
                                this.vehicleRegisterSystem = "new"
                            }

                            driverInfoDtoV2 = DriverInfoDto().apply {
                                this.driverId = id
                                this.name = name
                                this.address = address
                                this.contact = contact
                                this.taxiDetailDto = taxiDetailDtoV2
                            }

                            val paramsBuilder = getParamNew(driverInfoDtoV2!!)
                            updateInformation(driverInfoDtoV2!!, paramsBuilder)
                        }
                    }
                }
            }

            R.id.etZone -> {
                zoneBottomSheet.isExpanded = true
                hideKeyboard()
            }

            R.id.etVehicleType -> {
                vehicleTypeBottomSheet.isExpanded = true
                hideKeyboard()
            }

            R.id.etState -> {
                hideKeyboard()
                stateBottomSheet.isExpanded = true
            }

            R.id.etVehicleManagementCode -> {
                hideKeyboard()
                vehicleMgmtCodeBottomSheet.isExpanded = true
            }

            R.id.etVehicleTypeNew -> {
                hideKeyboard()
                vehicleTypeNewBottomSheet.isExpanded = true
            }

            R.id.btnCancel -> {
                if (PrefsImageUri(this@MainActivity).fileName != null &&
                    (PrefsImageUri(this@MainActivity).fileName)!!.isNotEmpty()
                ) {
                    imageUriV2 = PrefsImageUri(this@MainActivity).imgUri!!.toUri()
                    this.fileName = PrefsImageUri(this@MainActivity).fileName
                    val image = imageUriV2
                    var bitmap: Bitmap? = null
                    val imageStream: InputStream
                    try {
                        imageStream = contentResolver.openInputStream(image!!)!!
                        bitmap = BitmapFactory.decodeStream(imageStream)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    ivUpdateProfile.setImageBitmap(bitmap)
                }

                viewGone(btnDone, btnCancel,ivEdit)
                etUpdateName.setText(driverInfoDto!!.name)
                etUpdateAddress.setText(driverInfoDto!!.address)
                if (taxiDetailDto!!.vehicleRegisterSystem == "old") {
                    disableViews(
                        etUpdateName,
                        etUpdateAddress,
                        etZone,
                        etLotNumber,
                        etVehicleType,
                        etTaxiNumber,
                        ivEdit
                    )
                    val zc = zoneToNepali(taxiDetailDto!!.zoneCode!!)
                    etZone.setText(zc)
                    etLotNumber.setText(taxiDetailDto!!.lotNumber)
                    val vt = vehicleTypeToNepali(taxiDetailDto!!.vehicleType!!)
                    etVehicleType.setText(vt)
                    etTaxiNumber.setText(taxiDetailDto!!.taxiNumber)
                } else {
                    disableViews(
                        etUpdateName,
                        etUpdateAddress,
                        etState,
                        etVehicleManagementCode,
                        etLotNumberNew,
                        etVehicleTypeNew,
                        etTaxiNumberNew,
                        ivEdit
                    )
                    etState.setText(taxiDetailDto!!.state)
                    etVehicleManagementCode.setText(taxiDetailDto!!.vehicleManagementCode)
                    etLotNumberNew.setText(taxiDetailDto!!.lotNumber)
                    etVehicleTypeNew.setText(taxiDetailDto!!.vehicleType)
                    etTaxiNumberNew.setText(taxiDetailDto!!.taxiNumber)
                }
            }
        }
    }

    private var imageFile: File? = null
    private fun getImageFile(context: Context, imageTag: String): File? {
        val baseDirectory = getBaseDirectory(context)
        fileName = "${imageTag}_${System.currentTimeMillis()}.png"
        return File(baseDirectory, fileName!!).also {
            this@MainActivity.imageFile = it
        }
    }

    private fun getFileUri(context: Context, file: File?): Uri? {
        return if (file != null) {
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } else {
            null
        }
    }

    private fun getBaseDirectory(context: Context): File? {
        return context.filesDir
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            permissionRequestCode -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                        ), permissionRequestCode
                    )
                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setItems(
                            arrayOf("Take a photo", "Choose from Gallery", "Remove Photo")
                        ) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    imageUri = getFileUri(
                                        this@MainActivity,
                                        getImageFile(
                                            this@MainActivity,
                                            UUID.randomUUID().toString()
                                        )
                                    )
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                                    cameraIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    if (cameraIntent.resolveActivity(this@MainActivity.packageManager) != null) {
                                        startActivityForResult(
                                            Intent.createChooser(cameraIntent, "Take picture"),
                                            requestCodeCamera
                                        )
                                    }
                                }
                                1 -> {
                                    val intent = Intent()
                                    intent.type = "image/*"
                                    intent.action = Intent.ACTION_GET_CONTENT
                                    startActivityForResult(
                                        Intent.createChooser(
                                            intent,
                                            "Select Picture"
                                        ), requestCodeGallery
                                    )
                                }
                                2 -> {
                                    imageUriV2 = null
                                    fileName = null
                                    ivUpdateProfile.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@MainActivity,
                                            R.drawable.ic_profile
                                        )
                                    )
                                }
                            }
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            requestCodeCamera -> {
//                for (files in getBaseDirectory(this@DriverInfoActivity)?.listFiles()!!) {
//                       this@DriverInfoActivity.javaClass.log("file name: ${files.name}")
//                }
                imageUriV2 = Uri.fromFile(imageFile)
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath, options)
                ivUpdateProfile.setImageBitmap(bitmap)
            }
            requestCodeGallery -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val image = data.data
                        try {
                            val imageStream = contentResolver.openInputStream(image!!)!!
                            val bm = BitmapFactory.decodeStream(imageStream)
                            getFileUri(
                                this@MainActivity,
                                getImageFile(
                                    this@MainActivity,
                                    UUID.randomUUID().toString()
                                )
                            )
                            val fos = FileOutputStream(imageFile!!)
                            imageUriV2 = Uri.fromFile(imageFile)
                            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            ivUpdateProfile.setImageBitmap(bm)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }


    private fun updateInformation(driverInfoDto: DriverInfoDto, params: StringBuilder) {
        if (isInternetConnected) {
            this@MainActivity::class.java.log("dtoData--> $driverInfoDto")
            this@MainActivity::class.java.log("paramsData--> $params")
            val touristInfoPostResponse =
                InfoPostController(URL_UPDATE, mOnPostResponseListener)
            touristInfoPostResponse.execute(params.toString())
        } else {
            AlertDialog.Builder(this@MainActivity)
                .setCancelable(false)
                .setMessage("No internet connection")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.menu_edit_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionEdit -> {
                visibleViews(btnDone,btnCancel,ivEdit)
                if (taxiDetailDto!!.vehicleRegisterSystem == "old") {
                    enableViews(
                        etUpdateName,
                        etUpdateAddress,
                        etZone,
                        etLotNumber,
                        etVehicleType,
                        etTaxiNumber,
                        ivEdit
                    )
                } else if (taxiDetailDto!!.vehicleRegisterSystem == "new") {
                    enableViews(
                        etUpdateName,
                        etUpdateAddress,
                        etState,
                        etVehicleManagementCode,
                        etLotNumberNew,
                        etVehicleTypeNew,
                        etTaxiNumberNew,
                        ivEdit
                    )
                }
            }
            R.id.actionDelete -> {

                if (isInternetConnected) {
                    AlertDialog.Builder(this@MainActivity)
                        .setCancelable(false)
                        .setMessage("Do you really want to delete account?")
                        .setPositiveButton("Delete Account") { dialog, _ ->
                            dialog.dismiss()
                            val paramsBuilder = "id=${driverInfoDto!!.driverId}"
                            val deleteAccountPostResponse =
                                InfoPostController(
                                    URL_DELETE,
                                    mOnDeleteAccountResponseListener
                                )
                            deleteAccountPostResponse.execute(paramsBuilder)
                            this@MainActivity::class.java.log("paramsData--> $paramsBuilder")
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create().show()

                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setCancelable(false)
                        .setMessage("No internet connection")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create().show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val mOnDeleteAccountResponseListener = object :
        OnPostResponseListener {
        override fun onStarted(url: String) {
            progressDialog!!.setMessage("Deleting....")
            progressDialog?.show()
            this@MainActivity::class.java.log("connecting to $url")
        }

        override fun onComplete(message: String) {
            progressDialog?.dismiss()
            val jsonArrayResponse = JSONArray(message)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@MainActivity,
                        jsonObjectResponse.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    PrefLogin(this@MainActivity).resetLoginPrefs()
                    finish()

                } else {
                    Snackbar.make(
                        clEdit,
                        jsonObjectResponse.getString("message"),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            this@MainActivity::class.java.log("server up: $message")
        }

        override fun onError(result: String?) {
            this@MainActivity::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@MainActivity)
                .setCancelable(false)
                .setMessage("Failed to delete, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    private val mOnPostResponseListener = object :
        OnPostResponseListener {

        override fun onStarted(url: String) {
            progressDialog?.show()
            this@MainActivity::class.java.log("connecting to $url")
        }

        override fun onComplete(message: String) {
            progressDialog?.dismiss()
            this@MainActivity::class.java.log("server up: $message")

            val jsonArrayResponse = JSONArray(message)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@MainActivity,
                        jsonObjectResponse.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()

                    val jsonArrayResponseData = jsonObjectResponse.getJSONArray("data")
                    for (j in 0 until jsonArrayResponseData.length()) {
                        val jsonObjectResponseData = jsonArrayResponseData.getJSONObject(j)
                        val id = jsonObjectResponseData.getInt("id")
                        val name = jsonObjectResponseData.getString("name")
                        val address = jsonObjectResponseData.getString("address")
                        val contact = jsonObjectResponseData.getString("contact")
                        val zoneCode = jsonObjectResponseData.getString("zoneCode")
                        val lotNumber = jsonObjectResponseData.getString("lotNumber")
                        val vehicleType = jsonObjectResponseData.getString("vehicleType")
                        val taxiNumber = jsonObjectResponseData.getString("taxiNumber")
                        val state = jsonObjectResponseData.getString("state")
                        val vehicleManagementCode =
                            jsonObjectResponseData.getString("vehicleManagementCode")
                        val vehicleRegisterSystem =
                            jsonObjectResponseData.getString("vehicleRegisterSystem")

                        taxiDetailDtoV3 = TaxiInfoDto().apply {
                            this.driverId = id
                            this.zoneCode = zoneCode
                            this.lotNumber = lotNumber
                            this.vehicleType = vehicleType
                            this.taxiNumber = taxiNumber
                            this.state = state
                            this.vehicleManagementCode = vehicleManagementCode
                            this.vehicleRegisterSystem = vehicleRegisterSystem
                        }
                        driverInfoDtoV3 = DriverInfoDto().apply {
                            this.driverId = id
                            this.name = name
                            this.address = address
                            this.contact = contact
                            this.imageFileName = fileName
                            this.imageUri = imageUriV2
                            this.taxiDetailDto = taxiDetailDtoV3
                        }

                        PrefLogin(this@MainActivity).isLogin = true

                        this@MainActivity::class.java.log("From server::::: $driverInfoDtoV3")

                        PrefLogin(this@MainActivity).id = driverInfoDtoV3!!.driverId
                        PrefLogin(this@MainActivity).name = driverInfoDtoV3!!.name
                        PrefLogin(this@MainActivity).address = driverInfoDtoV3!!.address
                        PrefLogin(this@MainActivity).contact = driverInfoDtoV3!!.contact

                        PrefsImageUri(this@MainActivity).fileName = driverInfoDtoV3!!.imageFileName
                        PrefsImageUri(this@MainActivity).imgUri =
                            driverInfoDtoV3!!.imageUri.toString()

                        this@MainActivity::class.java.log("PrefsData: ID-->${PrefLogin(this@MainActivity).id}")
                        this@MainActivity::class.java.log("PrefsData:  Name-->${PrefLogin(this@MainActivity).name}")
                        this@MainActivity::class.java.log("PrefsData: Address-->${PrefLogin(this@MainActivity).address}")
                        this@MainActivity::class.java.log(
                            "PrefsData: Contact-->${PrefLogin(
                                this@MainActivity
                            ).contact}"
                        )

                        if (taxiDetailDtoV3!!.vehicleRegisterSystem == "old") {
                            PrefLogin(this@MainActivity).zoneCode = taxiDetailDtoV3!!.zoneCode
                            PrefLogin(this@MainActivity).lotNumber = taxiDetailDtoV3!!.lotNumber
                            PrefLogin(this@MainActivity).vehicleType =
                                taxiDetailDtoV3!!.vehicleType
                            PrefLogin(this@MainActivity).taxiNumber =
                                taxiDetailDtoV3!!.taxiNumber

                            this@MainActivity::class.java.log(
                                "PrefsData: ZoneCode-->${PrefLogin(
                                    this@MainActivity
                                ).zoneCode}"
                            )
                            this@MainActivity::class.java.log(
                                "PrefsData:  LotNumber-->${PrefLogin(
                                    this@MainActivity
                                ).lotNumber}"
                            )
                            this@MainActivity::class.java.log(
                                "PrefsData: VehicleCategory-->${PrefLogin(
                                    this@MainActivity
                                ).vehicleType}"
                            )
                            this@MainActivity::class.java.log(
                                "PrefsData: TaxiNumber-->${PrefLogin(
                                    this@MainActivity
                                ).taxiNumber}"
                            )

                        } else {
                            PrefLogin(this@MainActivity).state = taxiDetailDtoV3!!.state
                            PrefLogin(this@MainActivity).vehicleManagementCode =
                                taxiDetailDtoV3!!.vehicleManagementCode
                            PrefLogin(this@MainActivity).lotNumber = taxiDetailDtoV3!!.lotNumber
                            PrefLogin(this@MainActivity).vehicleType =
                                taxiDetailDtoV3!!.vehicleType
                            PrefLogin(this@MainActivity).taxiNumber =
                                taxiDetailDtoV3!!.taxiNumber
                            PrefLogin(this@MainActivity).vehicleRegistrationCode =
                                taxiDetailDtoV3!!.vehicleRegisterSystem

                            this@MainActivity::class.java.log(
                                "PrefsData: State-->${PrefLogin(
                                    this@MainActivity
                                ).state}"
                            )

                            this@MainActivity::class.java.log(
                                "PrefsData: VehicleMAnagemnetCode-->${PrefLogin(
                                    this@MainActivity
                                ).vehicleManagementCode}"
                            )
                            this@MainActivity::class.java.log(
                                "PrefsData:  LotNumber-->${PrefLogin(
                                    this@MainActivity
                                ).lotNumber}"
                            )
                            this@MainActivity::class.java.log(
                                "PrefsData: VehicleCategory-->${PrefLogin(
                                    this@MainActivity
                                ).vehicleType}"
                            )
                            this@MainActivity::class.java.log(
                                "PrefsData: TaxiNumber-->${PrefLogin(
                                    this@MainActivity
                                ).taxiNumber}"
                            )
                        }
                        val intent = Intent(this@MainActivity, MainActivity::class.java)
                        intent.putExtra("dto", driverInfoDtoV3)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Snackbar.make(
                        clEdit,
                        jsonObjectResponse.getString("message"),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        override fun onError(result: String?) {
            this@MainActivity::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@MainActivity)
                .setCancelable(false)
                .setMessage("Failed in registration, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    private fun visibleViews(vararg views: View) {
        for (v in views) {
            v.visibility = View.VISIBLE
        }
    }

    private fun viewGone(vararg views: View) {
        for (v in views) {
            v.visibility = View.GONE
        }
    }

    private fun inVisibleViews(vararg views: View) {
        for (v in views) {
            v.visibility = View.INVISIBLE
        }
    }

    private fun allValidProfile(): Boolean {
        if (etUpdateName.text.toString().isEmpty()) {
            tilUpdateName.error = "Enter name."
            tilUpdateName.isErrorEnabled = true
            etUpdateName.requestFocus()
            return false
        } else {
            tilUpdateName.isErrorEnabled = false
            tilUpdateName.error = null
            etUpdateName.clearFocus()
        }

        if (etUpdateAddress.text.toString().isEmpty()) {
            tilUpdateAddress.isErrorEnabled = true
            tilUpdateAddress.error = "Enter address."
            etUpdateAddress.requestFocus()
            return false
        } else {
            tilUpdateAddress.isErrorEnabled = false
            tilUpdateAddress.error = null
            etUpdateAddress.clearFocus()
        }
        return true
    }


    private fun allOldFieldValid(): Boolean {
        if (etZone.text.toString().isEmpty()) {
            Snackbar.make(clEdit, "Please select zone", Snackbar.LENGTH_SHORT)
                .show()
            return false
        }

        if (etLotNumber.text.toString().isEmpty()) {
            tilLotNumber.isErrorEnabled = true
            tilLotNumber.error = "Please enter lot no."
            etLotNumber.requestFocus()
            return false
        } else {
            tilLotNumber.isErrorEnabled = false
            tilLotNumber.error = null
            etLotNumber.clearFocus()
        }

        if (etVehicleType.text.toString().isEmpty()) {
            Snackbar.make(clEdit, "Please select vehicle type", Snackbar.LENGTH_SHORT)
                .show()
            return false
        }

        if (etTaxiNumber.text.toString().isEmpty()) {
            tilTaxiNumber.isErrorEnabled = true
            tilTaxiNumber.error = "Please enter taxi no."
            etTaxiNumber.requestFocus()
            return false
        } else {
            tilTaxiNumber.isErrorEnabled = false
            tilTaxiNumber.error = null
            etTaxiNumber.clearFocus()
        }

        return true
    }

    private fun allNewFieldValid(): Boolean {
        if (etState.text.toString().isEmpty()) {
            Snackbar.make(clEdit, "Please select state", Snackbar.LENGTH_SHORT)
                .show()
            return false
        }

        if (etVehicleManagementCode.text.toString().isEmpty()) {
            Snackbar.make(
                clEdit,
                "Please select vehicle management code",
                Snackbar.LENGTH_SHORT
            )
                .show()
            return false
        }

        if (etLotNumberNew.text.toString().isEmpty()) {
            tilLotNumberNew.isErrorEnabled = true
            tilLotNumberNew.error = "Please enter lot no."
            etLotNumberNew.requestFocus()
            return false
        } else {
            tilLotNumberNew.isErrorEnabled = false
            tilLotNumberNew.error = null
            etLotNumberNew.clearFocus()
        }

        if (etVehicleTypeNew.text.toString().isEmpty()) {
            Snackbar.make(clEdit, "Please select vehicle type", Snackbar.LENGTH_SHORT)
                .show()
            return false
        }

        if (etTaxiNumberNew.text.toString().isEmpty()) {
            tilTaxiNumberNew.isErrorEnabled = true
            tilTaxiNumberNew.error = "Please enter taxi no."
            etTaxiNumberNew.requestFocus()
            return false
        } else {
            tilTaxiNumberNew.isErrorEnabled = false
            tilTaxiNumberNew.error = null
            etTaxiNumberNew.clearFocus()
        }

        return true
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun onBackPressed() {
        when {
            zoneBottomSheet.isExpanded -> zoneBottomSheet.isCollapsed = true
            vehicleTypeBottomSheet.isExpanded -> vehicleTypeBottomSheet.isCollapsed = true
            stateBottomSheet.isExpanded -> stateBottomSheet.isCollapsed = true
            vehicleMgmtCodeBottomSheet.isExpanded -> vehicleMgmtCodeBottomSheet.isCollapsed =
                true
            vehicleTypeNewBottomSheet.isExpanded -> vehicleTypeNewBottomSheet.isCollapsed = true
            else ->
                finish()
        }
    }
}