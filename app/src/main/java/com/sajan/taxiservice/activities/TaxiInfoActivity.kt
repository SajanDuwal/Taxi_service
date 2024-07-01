package com.sajan.taxiservice.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.sajan.taxiservice.R
import com.sajan.taxiservice.adapters.*
import com.sajan.taxiservice.controller.InfoPostController
import com.sajan.taxiservice.dataModels.DriverInfoDto
import com.sajan.taxiservice.dataModels.TaxiInfoDto
import com.sajan.taxiservice.protocols.OnItemClickListener
import com.sajan.taxiservice.protocols.OnPostResponseListener
import com.sajan.taxiservice.storage.PrefLogin
import com.sajan.taxiservice.utils.*
import kotlinx.android.synthetic.main.activity_taxi_info_activity.*
import org.json.JSONArray

class TaxiInfoActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

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

    private var taxiRegisterSystem: String = "old"

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taxi_info_activity)
        driverInfoDto = intent.getParcelableExtra("dto")!!
        taxiDetailDto = TaxiInfoDto()
        setUpActionBar()
        setUpProgressDialog()
        initView()
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpProgressDialog() {
        progressDialog = ProgressDialog(this@TaxiInfoActivity)
        progressDialog!!.setMessage("Registering....")
        progressDialog!!.setCancelable(false)
    }

    private fun initView() {
        btnStart.setOnClickListener(this)
        swtTaxiState.setOnCheckedChangeListener(this)
        etZone.setOnClickListener(this)
        etVehicleType.setOnClickListener(this)
        etState.setOnClickListener(this)
        etVehicleManagementCode.setOnClickListener(this)
        etVehicleTypeNew.setOnClickListener(this)

        zoneBottomSheet = BottomSheetBehavior.from(mcvZoneSheet)
        zoneBottomSheet.peekHeight = 0
        rvZone.layoutManager =
            LinearLayoutManager(this@TaxiInfoActivity, RecyclerView.VERTICAL, false)
        setZoneList()

        vehicleTypeBottomSheet = BottomSheetBehavior.from(mcvVehicleType)
        vehicleTypeBottomSheet.peekHeight = 0
        rvVehicleType.layoutManager =
            LinearLayoutManager(this@TaxiInfoActivity, RecyclerView.VERTICAL, false)
        setVehicleTypeList()

        stateBottomSheet = BottomSheetBehavior.from(mcvState)
        stateBottomSheet.peekHeight = 0
        rvState.layoutManager =
            LinearLayoutManager(this@TaxiInfoActivity, RecyclerView.VERTICAL, false)
        setStateList()

        vehicleMgmtCodeBottomSheet = BottomSheetBehavior.from(mcvVehicleMgmtCode)
        vehicleMgmtCodeBottomSheet.peekHeight = 0
        rvVehicleMgmtCode.layoutManager =
            LinearLayoutManager(this@TaxiInfoActivity, RecyclerView.VERTICAL, false)
        setVehicleMgmtCodeList()

        vehicleTypeNewBottomSheet = BottomSheetBehavior.from(mcvVehicleTypeNew)
        vehicleTypeNewBottomSheet.peekHeight = 0
        rvVehicleTypeNew.layoutManager =
            LinearLayoutManager(this@TaxiInfoActivity, RecyclerView.VERTICAL, false)
        setVehicleTypeNewList()
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

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.swtTaxiState -> {
                if (isChecked) {
                    if (clNew.visibility == View.INVISIBLE) {
                        clNew.visibility = View.VISIBLE
                        clOld.visibility = View.INVISIBLE
                        etZone.text = null
                        etLotNumber.text = null
                        etVehicleType.text = null
                        etTaxiNumber.text = null
                        taxiRegisterSystem = "new"
                    }
                } else {
                    clOld.visibility = View.VISIBLE
                    clNew.visibility = View.INVISIBLE
                    etState.text = null
                    etVehicleManagementCode.text = null
                    etLotNumberNew.text = null
                    etVehicleTypeNew.text = null
                    etTaxiNumberNew.text = null
                    taxiRegisterSystem = "old"
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
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

            R.id.btnStart -> {
                if (taxiRegisterSystem == "old") {
                    if (allOldFieldValid()) {
                        val zoneCode = etZone.text.toString()
                        val lotNumber = etLotNumber.text.toString()
                        val vehicleType = etVehicleType.text.toString()
                        val taxiNumber = etTaxiNumber.text.toString()
                        val zone = zoneToEnglish(zoneCode)
                        taxiDetailDto!!.zoneCode = zone
                        taxiDetailDto!!.lotNumber = lotNumber
                        val vt = vehicleTypeToEnglish(vehicleType)
                        taxiDetailDto!!.vehicleType = vt
                        taxiDetailDto!!.taxiNumber = taxiNumber
                        taxiDetailDto!!.vehicleRegisterSystem = taxiRegisterSystem
                        taxiDetailDto!!.state = null
                        taxiDetailDto!!.vehicleManagementCode = null
                        driverInfoDto!!.taxiDetailDto = taxiDetailDto
                        saveInformation(driverInfoDto!!)
                    }
                } else {
                    if (allNewFieldValid()) {
                        val state = etState.text.toString()
                        val vehicleManagementCode = etVehicleManagementCode.text.toString()
                        val lotNumberNew = etLotNumberNew.text.toString()
                        val vehicleTypeNew = etVehicleTypeNew.text.toString()
                        val taxiNumberNew = etTaxiNumberNew.text.toString()
                        taxiDetailDto!!.state = state
                        taxiDetailDto!!.vehicleManagementCode = vehicleManagementCode
                        taxiDetailDto!!.lotNumber = lotNumberNew
                        taxiDetailDto!!.vehicleType = vehicleTypeNew
                        taxiDetailDto!!.taxiNumber = taxiNumberNew
                        taxiDetailDto!!.vehicleRegisterSystem = taxiRegisterSystem
                        taxiDetailDto!!.zoneCode = null
                        driverInfoDto!!.taxiDetailDto = taxiDetailDto
                        saveInformation(driverInfoDto!!)
                    }
                }
            }
        }
    }

    private fun saveInformation(driverInfoDto: DriverInfoDto) {
        if (isInternetConnected) {
            val paramsBuilder = getParam(driverInfoDto)
            this@TaxiInfoActivity::class.java.log("dtoData--> $driverInfoDto")
            this@TaxiInfoActivity::class.java.log("paramsData--> $paramsBuilder")
            val touristInfoPostResponse =
                InfoPostController(URL_REGISTER, mOnPostResponseListener)
            touristInfoPostResponse.execute(paramsBuilder.toString())
        } else {
            AlertDialog.Builder(this@TaxiInfoActivity)
                .setCancelable(false)
                .setMessage("No internet connection")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    private var driverInfoDtoV2: DriverInfoDto? = null
    private var taxiDetailDtoV2: TaxiInfoDto? = null

    private val mOnPostResponseListener = object :
        OnPostResponseListener {

        override fun onStarted(url: String) {
            progressDialog?.show()
            this@TaxiInfoActivity::class.java.log("connecting to $url")
        }

        override fun onComplete(response: String) {
            progressDialog?.dismiss()
            this@TaxiInfoActivity::class.java.log("server up: $response")

            val jsonArrayResponse = JSONArray(response)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@TaxiInfoActivity,
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

                        taxiDetailDtoV2 = TaxiInfoDto().apply {
                            this.driverId = id
                            this.zoneCode = zoneCode
                            this.lotNumber = lotNumber
                            this.vehicleType = vehicleType
                            this.taxiNumber = taxiNumber
                            this.state = state
                            this.vehicleManagementCode = vehicleManagementCode
                            this.vehicleRegisterSystem = vehicleRegisterSystem
                        }
                        driverInfoDtoV2 = DriverInfoDto().apply {
                            this.driverId = id
                            this.name = name
                            this.address = address
                            this.contact = contact
                            this.taxiDetailDto = taxiDetailDtoV2
                        }

                        PrefLogin(this@TaxiInfoActivity).isLogin = true

                        this@TaxiInfoActivity::class.java.log("From server::::: $driverInfoDtoV2 ")

                        PrefLogin(this@TaxiInfoActivity).id = driverInfoDtoV2!!.driverId
                        PrefLogin(this@TaxiInfoActivity).name = driverInfoDtoV2!!.name
                        PrefLogin(this@TaxiInfoActivity).address = driverInfoDtoV2!!.address
                        PrefLogin(this@TaxiInfoActivity).contact = driverInfoDtoV2!!.contact

                        this@TaxiInfoActivity::class.java.log("PrefsData: ID-->${PrefLogin(this@TaxiInfoActivity).id}")
                        this@TaxiInfoActivity::class.java.log("PrefsData:  Name-->${PrefLogin(this@TaxiInfoActivity).name}")
                        this@TaxiInfoActivity::class.java.log("PrefsData: Address-->${PrefLogin(this@TaxiInfoActivity).address}")
                        this@TaxiInfoActivity::class.java.log(
                            "PrefsData: Contact-->${PrefLogin(
                                this@TaxiInfoActivity
                            ).contact}"
                        )

                        if (taxiRegisterSystem == "old") {
                            PrefLogin(this@TaxiInfoActivity).zoneCode = taxiDetailDtoV2!!.zoneCode
                            PrefLogin(this@TaxiInfoActivity).lotNumber = taxiDetailDtoV2!!.lotNumber
                            PrefLogin(this@TaxiInfoActivity).vehicleType =
                                taxiDetailDtoV2!!.vehicleType
                            PrefLogin(this@TaxiInfoActivity).taxiNumber =
                                taxiDetailDtoV2!!.taxiNumber
                            PrefLogin(this@TaxiInfoActivity).vehicleRegistrationCode =
                                taxiDetailDtoV2!!.vehicleRegisterSystem

                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData: ZoneCode-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).zoneCode}"
                            )
                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData:  LotNumber-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).lotNumber}"
                            )
                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData: VehicleCategory-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).vehicleType}"
                            )
                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData: TaxiNumber-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).taxiNumber}"
                            )
                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData: VehicleRegistrationSystem-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).vehicleRegistrationCode}"
                            )

                        } else {
                            PrefLogin(this@TaxiInfoActivity).state = taxiDetailDtoV2!!.state
                            PrefLogin(this@TaxiInfoActivity).vehicleManagementCode =
                                taxiDetailDtoV2!!.vehicleManagementCode
                            PrefLogin(this@TaxiInfoActivity).lotNumber = taxiDetailDtoV2!!.lotNumber
                            PrefLogin(this@TaxiInfoActivity).vehicleType =
                                taxiDetailDtoV2!!.vehicleType
                            PrefLogin(this@TaxiInfoActivity).taxiNumber =
                                taxiDetailDtoV2!!.taxiNumber
                            PrefLogin(this@TaxiInfoActivity).vehicleRegistrationCode =
                                taxiDetailDtoV2!!.vehicleRegisterSystem

                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData: State-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).state}"
                            )

                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData: VehicleMAnagemnetCode-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).vehicleManagementCode}"
                            )
                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData:  LotNumber-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).lotNumber}"
                            )
                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData: VehicleCategory-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).vehicleType}"
                            )
                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData: TaxiNumber-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).taxiNumber}"
                            )
                            this@TaxiInfoActivity::class.java.log(
                                "PrefsData: VehicleRegistrationSystem-->${PrefLogin(
                                    this@TaxiInfoActivity
                                ).vehicleRegistrationCode}"
                            )
                        }
                    }
                    val intent = Intent(this@TaxiInfoActivity, MainActivity::class.java)
                    intent.putExtra("dto", driverInfoDtoV2)
                    startActivity(intent)
                    finish()
                } else {
                    Snackbar.make(
                        nsv,
                        jsonObjectResponse.getString("message"),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        override fun onError(result: String?) {
            this@TaxiInfoActivity::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@TaxiInfoActivity)
                .setCancelable(false)
                .setMessage("Failed in registration, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }


    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }


    private fun allOldFieldValid(): Boolean {
        if (etZone.text.toString().isEmpty()) {
            Snackbar.make(clTaxiInfo, "Please select zone", Snackbar.LENGTH_SHORT)
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
            Snackbar.make(clTaxiInfo, "Please select vehicle type", Snackbar.LENGTH_SHORT)
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
            Snackbar.make(clTaxiInfo, "Please select state", Snackbar.LENGTH_SHORT)
                .show()
            return false
        }

        if (etVehicleManagementCode.text.toString().isEmpty()) {
            Snackbar.make(
                clTaxiInfo,
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
            Snackbar.make(clTaxiInfo, "Please select vehicle type", Snackbar.LENGTH_SHORT)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                when {
                    zoneBottomSheet.isExpanded -> zoneBottomSheet.isCollapsed = true
                    vehicleTypeBottomSheet.isExpanded -> vehicleTypeBottomSheet.isCollapsed = true
                    stateBottomSheet.isExpanded -> stateBottomSheet.isCollapsed = true
                    vehicleMgmtCodeBottomSheet.isExpanded -> vehicleMgmtCodeBottomSheet.isCollapsed =
                        true
                    vehicleTypeNewBottomSheet.isExpanded -> vehicleTypeNewBottomSheet.isCollapsed =
                        true
                    else -> {
                        val contactInfoIntent =
                            Intent(
                                this@TaxiInfoActivity,
                                DriverInfoActivity::class.java
                            )
                        contactInfoIntent.putExtra("dto", driverInfoDto)
                        startActivity(contactInfoIntent)
                        finish()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when {
            zoneBottomSheet.isExpanded -> zoneBottomSheet.isCollapsed = true
            vehicleTypeBottomSheet.isExpanded -> vehicleTypeBottomSheet.isCollapsed = true
            stateBottomSheet.isExpanded -> stateBottomSheet.isCollapsed = true
            vehicleMgmtCodeBottomSheet.isExpanded -> vehicleMgmtCodeBottomSheet.isCollapsed =
                true
            vehicleTypeNewBottomSheet.isExpanded -> vehicleTypeNewBottomSheet.isCollapsed = true
            else -> {
                val contactInfoIntent =
                    Intent(
                        this@TaxiInfoActivity,
                        DriverInfoActivity::class.java
                    )
                contactInfoIntent.putExtra("dto", driverInfoDto)
                startActivity(contactInfoIntent)
                finish()
            }
        }
    }
}

