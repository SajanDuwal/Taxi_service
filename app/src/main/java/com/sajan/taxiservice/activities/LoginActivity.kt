package com.sajan.taxiservice.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.sajan.taxiservice.R
import com.sajan.taxiservice.controller.InfoPostController
import com.sajan.taxiservice.dataModels.DriverInfoDto
import com.sajan.taxiservice.dataModels.TaxiInfoDto
import com.sajan.taxiservice.protocols.OnPostResponseListener
import com.sajan.taxiservice.storage.PrefLogin
import com.sajan.taxiservice.utils.URL_LOGIN
import com.sajan.taxiservice.utils.isInternetConnected
import com.sajan.taxiservice.utils.log
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var countryCode: String? = null
    private lateinit var auth: FirebaseAuth
    private var driverInfoDto: DriverInfoDto? = null
    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var contact: String? = null
    private var progressDialog: ProgressDialog? = null

    companion object {
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PrefLogin(this@LoginActivity).isLogin) {
            val id = PrefLogin(this@LoginActivity).id
            val name = PrefLogin(this@LoginActivity).name
            val address = PrefLogin(this@LoginActivity).address
            val contact = PrefLogin(this@LoginActivity).contact
            if (PrefLogin(this@LoginActivity).vehicleRegistrationCode == "old") {
                val zoneCode = PrefLogin(this@LoginActivity).zoneCode
                val lotNumber = PrefLogin(this@LoginActivity).lotNumber
                val vehicleType = PrefLogin(this@LoginActivity).vehicleType
                val taxiNumber = PrefLogin(this@LoginActivity).taxiNumber
                val vehicleRegistrationSystem =
                    PrefLogin(this@LoginActivity).vehicleRegistrationCode

                val taxiDetailDto = TaxiInfoDto().apply {
                    this.zoneCode = zoneCode
                    this.lotNumber = lotNumber
                    this.vehicleType = vehicleType
                    this.taxiNumber = taxiNumber
                    this.vehicleRegisterSystem = vehicleRegistrationSystem
                }

                val driverInfoDto = DriverInfoDto().apply {
                    this.driverId = id
                    this.name = name
                    this.address = address
                    this.contact = contact
                    this.taxiDetailDto = taxiDetailDto
                }
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("dto", driverInfoDto)
                startActivity(intent)
                finish()
            } else {
                val state = PrefLogin(this@LoginActivity).state
                val vehicleManagementCode = PrefLogin(this@LoginActivity).vehicleManagementCode
                val lotNumber = PrefLogin(this@LoginActivity).lotNumber
                val vehicleType = PrefLogin(this@LoginActivity).vehicleType
                val taxiNumbet = PrefLogin(this@LoginActivity).taxiNumber
                val vehicleRegistrationSystem =
                    PrefLogin(this@LoginActivity).vehicleRegistrationCode

                val taxiDetailDto = TaxiInfoDto().apply {
                    this.state = state
                    this.vehicleManagementCode = vehicleManagementCode
                    this.lotNumber = lotNumber
                    this.vehicleType = vehicleType
                    this.taxiNumber = taxiNumbet
                    this.vehicleRegisterSystem = vehicleRegistrationSystem
                }

                val driverInfoDto = DriverInfoDto().apply {
                    this.driverId = id
                    this.name = name
                    this.address = address
                    this.contact = contact
                    this.taxiDetailDto = taxiDetailDto
                }
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("dto", driverInfoDto)
                startActivity(intent)
                finish()
            }
        } else {
            setContentView(R.layout.activity_login)
            if (savedInstanceState != null) {
                onRestoreInstanceState(savedInstanceState)
            }
            countryCode = getCountryDialCode()
            auth = FirebaseAuth.getInstance()
            setUpProgressDialog()
            initView()
        }
    }

    private fun setUpProgressDialog() {
        progressDialog = ProgressDialog(this@LoginActivity)
        progressDialog!!.setMessage("Verifying...")
        progressDialog!!.setCancelable(false)
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        tvCountryCode.text = "+$countryCode"
        btnLogin.setOnClickListener(this)
        btnEditContact.setOnClickListener(this)
        btnResend.setOnClickListener(this)
        btnVerify.setOnClickListener(this)
    }

    @SuppressLint("DefaultLocale")
    private fun getCountryDialCode(): String {
        val countryId: String
        var countryDialCode: String? = null
        val telephonyManger = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        countryId = telephonyManger.simCountryIso.toUpperCase()
        val arrayCountryCode = resources.getStringArray(R.array.DialingCountryCode)
        for (element in arrayCountryCode) {
            val arrDial = element.split(",")
            if (arrDial[1].trim() == countryId.trim()) {
                countryDialCode = arrDial[0]
                break
            }
        }
        return countryDialCode!!
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnLogin -> {
                if (validatePhoneNumber()) {
                    val contact = etContact.text.toString()
                    driverInfoDto = DriverInfoDto().apply {
                        this.contact = contact
                    }
                    if (isInternetConnected) {
                        val paramsBuilder = "contact=${driverInfoDto!!.contact}"
                        this@LoginActivity::class.java.log("dtoData--> $driverInfoDto")
                        this@LoginActivity::class.java.log("paramsData--> $paramsBuilder")
                        val touristInfoPostResponse =
                            InfoPostController(URL_LOGIN, mOnPostResponseListener)
                        touristInfoPostResponse.execute(paramsBuilder)
                    } else {
                        AlertDialog.Builder(this@LoginActivity)
                            .setCancelable(false)
                            .setMessage("No internet connection")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create().show()
                    }
//                    startPhoneNumberVerification(etContact.text.toString())
                }
            }

            R.id.btnVerify -> {
                val code = etCode.text.toString()
                if (TextUtils.isEmpty(code)) {
                    tilCode.isErrorEnabled = true
                    etCode.requestFocus()
                    etCode.error = "Cannot be empty."
                    return
                } else {
                    tilCode.isErrorEnabled = false
                    tilCode.error = null
                    etCode.clearFocus()
                }
                verifyPhoneNumberWithCode(storedVerificationId, code)
            }

            R.id.btnEditContact -> {

            }

            R.id.btnResend -> {
                resendVerificationCode(
                    contact!!,
                    resendToken
                )
            }
        }
    }

    var driverInfoDtoV2: DriverInfoDto? = null

    private val mOnPostResponseListener = object :
        OnPostResponseListener {

        override fun onStarted(url: String) {
            progressDialog?.show()
            this@LoginActivity::class.java.log("connecting to $url")
        }

        override fun onComplete(message: String) {
            progressDialog?.dismiss()
            this@LoginActivity::class.java.log("server up: $message")

            val jsonArrayResponse = JSONArray(message)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@LoginActivity,
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

                        val taxiDetailDtoV2 = TaxiInfoDto().apply {
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

                        PrefLogin(this@LoginActivity).isLogin = true

                        this@LoginActivity::class.java.log("From server::::: $driverInfoDtoV2 ")

                        PrefLogin(this@LoginActivity).id = driverInfoDtoV2!!.driverId
                        PrefLogin(this@LoginActivity).name = driverInfoDtoV2!!.name
                        PrefLogin(this@LoginActivity).address = driverInfoDtoV2!!.address
                        PrefLogin(this@LoginActivity).contact = driverInfoDtoV2!!.contact

                        this@LoginActivity::class.java.log("PrefsData: ID-->${PrefLogin(this@LoginActivity).id}")
                        this@LoginActivity::class.java.log("PrefsData:  Name-->${PrefLogin(this@LoginActivity).name}")
                        this@LoginActivity::class.java.log("PrefsData: Address-->${PrefLogin(this@LoginActivity).address}")
                        this@LoginActivity::class.java.log(
                            "PrefsData: Contact-->${PrefLogin(
                                this@LoginActivity
                            ).contact}"
                        )

                        if (vehicleRegisterSystem == "old") {
                            PrefLogin(this@LoginActivity).zoneCode = taxiDetailDtoV2.zoneCode
                            PrefLogin(this@LoginActivity).lotNumber = taxiDetailDtoV2.lotNumber
                            PrefLogin(this@LoginActivity).vehicleType =
                                taxiDetailDtoV2.vehicleType
                            PrefLogin(this@LoginActivity).taxiNumber =
                                taxiDetailDtoV2.taxiNumber
                            PrefLogin(this@LoginActivity).vehicleRegistrationCode =
                                taxiDetailDtoV2.vehicleRegisterSystem

                            this@LoginActivity::class.java.log(
                                "PrefsData: ZoneCode-->${PrefLogin(
                                    this@LoginActivity
                                ).zoneCode}"
                            )
                            this@LoginActivity::class.java.log(
                                "PrefsData:  LotNumber-->${PrefLogin(
                                    this@LoginActivity
                                ).lotNumber}"
                            )
                            this@LoginActivity::class.java.log(
                                "PrefsData: VehicleCategory-->${PrefLogin(
                                    this@LoginActivity
                                ).vehicleType}"
                            )
                            this@LoginActivity::class.java.log(
                                "PrefsData: TaxiNumber-->${PrefLogin(
                                    this@LoginActivity
                                ).taxiNumber}"
                            )
                            this@LoginActivity::class.java.log(
                                "PrefsData: VehicleRegistrationSystem-->${PrefLogin(
                                    this@LoginActivity
                                ).vehicleRegistrationCode}"
                            )

                        } else {
                            PrefLogin(this@LoginActivity).state = taxiDetailDtoV2.state
                            PrefLogin(this@LoginActivity).vehicleManagementCode =
                                taxiDetailDtoV2.vehicleManagementCode
                            PrefLogin(this@LoginActivity).lotNumber = taxiDetailDtoV2.lotNumber
                            PrefLogin(this@LoginActivity).vehicleType =
                                taxiDetailDtoV2.vehicleType
                            PrefLogin(this@LoginActivity).taxiNumber =
                                taxiDetailDtoV2.taxiNumber
                            PrefLogin(this@LoginActivity).vehicleRegistrationCode =
                                taxiDetailDtoV2.vehicleRegisterSystem

                            this@LoginActivity::class.java.log(
                                "PrefsData: State-->${PrefLogin(
                                    this@LoginActivity
                                ).state}"
                            )

                            this@LoginActivity::class.java.log(
                                "PrefsData: VehicleMAnagemnetCode-->${PrefLogin(
                                    this@LoginActivity
                                ).vehicleManagementCode}"
                            )
                            this@LoginActivity::class.java.log(
                                "PrefsData:  LotNumber-->${PrefLogin(
                                    this@LoginActivity
                                ).lotNumber}"
                            )
                            this@LoginActivity::class.java.log(
                                "PrefsData: VehicleCategory-->${PrefLogin(
                                    this@LoginActivity
                                ).vehicleType}"
                            )
                            this@LoginActivity::class.java.log(
                                "PrefsData: TaxiNumber-->${PrefLogin(
                                    this@LoginActivity
                                ).taxiNumber}"
                            )
                            this@LoginActivity::class.java.log(
                                "PrefsData: VehicleRegistrationSystem-->${PrefLogin(
                                    this@LoginActivity
                                ).vehicleRegistrationCode}"
                            )
                        }
                    }
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("dto", driverInfoDtoV2)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@LoginActivity, DriverInfoActivity::class.java)
                    intent.putExtra("dto", driverInfoDto)
                    startActivity(intent)
                    finish()
                }
            }
        }

        override fun onError(result: String?) {
            this@LoginActivity::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@LoginActivity)
                .setCancelable(false)
                .setMessage("Failed in registration, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }


    private val callBacks = object :
        PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            verificationInProgress = false
            updateUI(STATE_VERIFY_SUCCESS, credential)
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            this@LoginActivity::class.java.log("$e")
            verificationInProgress = false
            if (e is FirebaseAuthInvalidCredentialsException) {
                Snackbar.make(clRoot, "Invalid Phone number", Snackbar.LENGTH_SHORT)
                    .show()
            } else if (e is FirebaseTooManyRequestsException) {
                Snackbar.make(clRoot, "Quota exceeded.", Snackbar.LENGTH_SHORT)
                    .show()
            }
            updateUI(STATE_VERIFY_FAILED)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            storedVerificationId = verificationId
            resendToken = token
            updateUI(STATE_CODE_SENT)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)

        if (verificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(etContact.text.toString())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        contact = "+$countryCode$phoneNumber"
        this@LoginActivity::class.java.log("startPhoneVerification--> contact  $contact")
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+9779860585307", // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callBacks
        ) // OnVerificationStateChangedCallbacks

        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+9779860585307", // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callBacks, // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    updateUI(STATE_SIGNIN_SUCCESS, user)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Snackbar.make(clRoot, "Invalid Code", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                    updateUI(STATE_SIGNIN_FAILED)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(
        uiState: Int,
        user: FirebaseUser? = auth.currentUser,
        cred: PhoneAuthCredential? = null
    ) {
        when (uiState) {
            STATE_INITIALIZED -> {
                visibleViews(loginNsv)
                inVisibleViews(codeVerifyNsv)
            }
            STATE_CODE_SENT -> {
                Toast.makeText(this@LoginActivity, "Code sent,", Toast.LENGTH_SHORT).show()
                if (cred != null) {
                    if (cred.smsCode != null) {
                        etCode.setText(cred.smsCode)
                    }
                }
                visibleViews(codeVerifyNsv)
                inVisibleViews(loginNsv)
            }
            STATE_VERIFY_FAILED -> {
                Toast.makeText(this@LoginActivity, "Verified Failed,", Toast.LENGTH_SHORT).show()
//                inVisibleViews(loginNsv)
//                visibleViews(codeVerifyNsv)
            }
            STATE_VERIFY_SUCCESS -> {
                Toast.makeText(this@LoginActivity, "Verified completed,", Toast.LENGTH_SHORT).show()
                if (cred != null) {
                    if (cred.smsCode != null) {
                        etCode.setText(cred.smsCode)
                    }
                }
            }
            STATE_SIGNIN_FAILED ->
                Snackbar.make(clRoot, "Sign-in Failed", Snackbar.LENGTH_SHORT)
                    .show()
            STATE_SIGNIN_SUCCESS -> {
                val intent = Intent(this@LoginActivity, DriverInfoActivity::class.java)
                intent.putExtra("dto", driverInfoDto)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun visibleViews(vararg views: View) {
        for (v in views) {
            v.visibility = View.VISIBLE
        }
    }

    private fun inVisibleViews(vararg views: View) {
        for (v in views) {
            v.visibility = View.INVISIBLE
        }
    }

    private fun validatePhoneNumber(): Boolean {
        return if (etContact.text.toString().isEmpty() ||
            etContact.text.toString().length > 10 ||
            etContact.text.toString().length < 10
        ) {
            tilContact.isErrorEnabled = true
            etContact.requestFocus()
            tilContact.error = "Wrong Contact."
            false
        } else {
            tilContact.isErrorEnabled = false
            tilContact.error = null
            etContact.clearFocus()
            true
        }
    }
}
