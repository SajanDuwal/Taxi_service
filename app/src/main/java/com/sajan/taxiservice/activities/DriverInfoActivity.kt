package com.sajan.taxiservice.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.sajan.bktguide.storage.PrefsImageUri
import com.sajan.taxiservice.R
import com.sajan.taxiservice.dataModels.DriverInfoDto
import com.sajan.taxiservice.utils.isCollapsed
import com.sajan.taxiservice.utils.isExpanded
import kotlinx.android.synthetic.main.activity_driver_info.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

class DriverInfoActivity : AppCompatActivity(), View.OnClickListener {

    private var driverInfoDto: DriverInfoDto? = null
    private var storageCheckPermission: Int = 0
    private var cameraCheckPermission: Int = 0
    private var permissionRequestCode: Int = 345
    private val requestCodeCamera = 25
    private val requestCodeGallery = 27
    private var imageUri: Uri? = null
    private var imageUriV2: Uri? = null
    private var fileName: String? = null
//    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_info)
        driverInfoDto = intent.getParcelableExtra("dto")!!
        setUpActionBar()
//        auth = FirebaseAuth.getInstance()
        initViews()
        storageCheckPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        cameraCheckPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initViews() {
        etName.setText(driverInfoDto!!.name)
        etAddress.setText(driverInfoDto!!.address)
        btnNext.setOnClickListener(this)
        ivEdit.setOnClickListener(this)
    }

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
                    AlertDialog.Builder(this@DriverInfoActivity)
                        .setItems(
                            arrayOf("Take a photo", "Choose from Gallery", "Remove Photo")
                        ) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    imageUri = getFileUri(
                                        this@DriverInfoActivity,
                                        getImageFile(
                                            this@DriverInfoActivity,
                                            UUID.randomUUID().toString()
                                        )
                                    )
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                                    cameraIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    if (cameraIntent.resolveActivity(this@DriverInfoActivity.packageManager) != null) {
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
                                    ivProfile.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@DriverInfoActivity,
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

            R.id.btnNext -> {
                if (allFieldValid()) {
                    val name = etName.text.toString()
                    val address = etAddress.text.toString()

                    driverInfoDto.apply {
                        this!!.name = name
                        this.address = address
                        this.imageUri = imageUriV2
                        this.imageFileName = fileName
                    }
                    PrefsImageUri(this@DriverInfoActivity).fileName = fileName
                    PrefsImageUri(this@DriverInfoActivity).imgUri = imageUriV2.toString()

                    val intent = Intent(this@DriverInfoActivity, TaxiInfoActivity::class.java)
                    intent.putExtra("dto", driverInfoDto)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private var imageFile: File? = null
    private fun getImageFile(context: Context, imageTag: String): File? {
        val baseDirectory = getBaseDirectory(context)
        fileName = "${imageTag}_${System.currentTimeMillis()}.png"
        return File(baseDirectory, fileName!!).also {
            this@DriverInfoActivity.imageFile = it
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
                    AlertDialog.Builder(this@DriverInfoActivity)
                        .setItems(
                            arrayOf("Take a photo", "Choose from Gallery", "Remove Photo")
                        ) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    imageUri = getFileUri(
                                        this@DriverInfoActivity,
                                        getImageFile(
                                            this@DriverInfoActivity,
                                            UUID.randomUUID().toString()
                                        )
                                    )
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                                    cameraIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    if (cameraIntent.resolveActivity(this@DriverInfoActivity.packageManager) != null) {
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
                                    ivProfile.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@DriverInfoActivity,
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
                ivProfile.setImageBitmap(bitmap)
            }
            requestCodeGallery -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val image = data.data
                        try {
                            val imageStream = contentResolver.openInputStream(image!!)!!
                            val bm = BitmapFactory.decodeStream(imageStream)
                            getFileUri(
                                this@DriverInfoActivity,
                                getImageFile(
                                    this@DriverInfoActivity,
                                    UUID.randomUUID().toString()
                                )
                            )
                            val fos = FileOutputStream(imageFile!!)
                            imageUriV2 = Uri.fromFile(imageFile)
                            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            ivProfile.setImageBitmap(bm)
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

    private fun allFieldValid(): Boolean {
        if (etName.text.toString().isEmpty()) {
            tilName.error = "Enter name."
            tilName.isErrorEnabled = true
            etName.requestFocus()
            return false
        } else {
            tilName.isErrorEnabled = false
            tilName.error = null
            etName.clearFocus()
        }

        if (etAddress.text.toString().isEmpty()) {
            tilAddress.isErrorEnabled = true
            tilAddress.error = "Enter address."
            etAddress.requestFocus()
            return false
        } else {
            tilAddress.isErrorEnabled = false
            tilAddress.error = null
            etAddress.clearFocus()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                AlertDialog.Builder(this@DriverInfoActivity)
                    .setMessage("Are you sure you want to exit? All saved details will be deleted.")
                    .setPositiveButton("Exit") { dialog, _ ->
                        dialog.dismiss()
//                        auth.signOut()
                        startActivity(Intent(this@DriverInfoActivity, LoginActivity::class.java))
                        finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this@DriverInfoActivity)
            .setMessage("Are you sure you want to exit? All saved details will be deleted.")
            .setPositiveButton("Exit") { dialog, _ ->
                dialog.dismiss()
//                auth.signOut()
                startActivity(Intent(this@DriverInfoActivity, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }
}
