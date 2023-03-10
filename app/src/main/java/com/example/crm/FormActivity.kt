package com.example.crm

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crm.databinding.ActivityFormBinding
import com.example.crm.model.ProjectData
import com.example.crm.model.ImageDetails
import com.example.crm.model.ImageRequestDetails
import com.example.crm.model.UpdateSurveyImageDetailResult
import com.example.crm.pending.PendingViewModel
import com.example.crm.permission.CheckPermission
import com.example.crm.permission.CheckPermission.Companion.requestLocationPermissions
import com.example.crm.utility.CurrentDateTime
import com.example.crm.utility.FileTypeConverter
import retrofit.RetrofitInstance
import retrofit2.Call
import com.google.android.material.snackbar.Snackbar
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*


class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private var projectData: ProjectData? = null
    private lateinit var pendingViewModel: PendingViewModel
    private lateinit var filePhoto: File
    private val fileTypeConverter = FileTypeConverter()
    private var imagesListBitmap = ArrayList<Bitmap>()
    private var imageDetailsList = ArrayList<ImageDetails>()
    private var imageIdWithBitmapArray = ArrayList<Int>()
    private lateinit var adapter: ImageAdapter
    private var locationInfo: Pair<Double, Double>? = null
    get() {
        val latitude = pendingViewModel.currentLocation.value?.latitude
        val longitude = pendingViewModel.currentLocation.value?.longitude
        return if (latitude != null && longitude != null) {
            Pair(latitude, longitude)
        } else {
            field
        }
    }
    private var isSaveButtonClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pendingViewModel = ViewModelProvider(this)[PendingViewModel::class.java].apply {
            byteArrayForProjectId.observe(this@FormActivity) { setImage(it) }
            postImageDetails.observe(this@FormActivity) {
                postImageRequest(adapter.getImageDetailsList())
            }
        }
        val intent = intent
        if (intent != null) {
            projectData = intent.extras?.get(FORM_ACTIVITY_EXTRA) as ProjectData
        }
        Log.v("TAG", "Draft data : $projectData")
        binding.rcv.layoutManager = LinearLayoutManager(this)
        adapter = ImageAdapter(imagesListBitmap, this)
        adapter.setImageDetailsList(imageDetailsList)
        binding.rcv.adapter = adapter
        binding.progressBar.visibility = View.VISIBLE
        projectData.apply {
            this?.projectName.also { binding.txtTitle.text = it }
            this?.projectId.also { binding.txtDescription.text = it }
            this?.projectSubType.also {
                binding.txtResidental.text = "Residental $it"
            }
            this?.region.also { binding.txtAddress.text = it }
            this?.builder?.trim().also {
                binding.txtBuilder.text = "builder : $it"
            }
            this?.compDate.also {
                binding.txtComdate.text = "Compdate : $it"
            }
           this?.launchDate.also {
                binding.txtLaunchDate.text =  "Launch Date : $it"
            }
            this?.launchUnit.also {
                binding.txtLaunchUnit.text = "Launch Unit : $it"
            }
            this?.launchSqFt.also {
                binding.txtSqft.text = "Sq Ft : $it"
            }
            this?.address.also { binding.txtFullAddress.text = it }
            this?.compDate.also { setCompDate(it) }
            this?.townshipName?.also { binding.etxtTownshipName.setText(it) }
            this?.constructionSlab.also { setConstructionSlab(it) }
            pendingViewModel.updatesAllImagesByProjectId(this?.projectId!!)
        }
        binding.btnSave.setOnClickListener {
            isSaveButtonClicked = true
            onSaveButtonClicked()
        }
        binding.btnAddImage.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Select From Camera",
                    DialogInterface.OnClickListener { dialog, id ->
                        checkPermission(
                            Manifest.permission.CAMERA,
                            CAMERA_PERMISSION_CODE
                        )
                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id -> dialog.dismiss() })
            val alert: AlertDialog = builder.create()
            alert.show()
        }
    }

    private fun onSaveButtonClicked() {
        when (projectData?.isPending) {
            true -> {
                adapter.getImageDetailsList().forEach {
                    if (it.latitude == 0.0 || it.longitudes == 0.0) {
                        if (locationInfo?.first != 0.0 && locationInfo?.second != 0.0) {
                            it.latitude = locationInfo?.first
                            it.longitudes = locationInfo?.second
                        }
                    }
                    pendingViewModel.uploadImage(it)
                }

                projectData?.projectId?.let { it1 ->
                    pendingViewModel.updateDraft(
                        isPending = false,
                        projectId = it1,
                        townshipName = binding.etxtTownshipName.text.toString(),
                        constructionSlab = binding.constructionSlabDropDown.selectedItem.toString(),
                        compDate = "${binding.combDateMonth.selectedItem}-${binding.combDateYear.selectedItem}",
                        latitude = locationInfo?.first,
                        longitude = locationInfo?.second
                    )
                }
            }
            false -> {
                projectData?.projectId?.let { it1 ->
                    pendingViewModel.updateDraft(
                        isPending = false,
                        projectId = it1,
                        townshipName = binding.etxtTownshipName.text.toString(),
                        constructionSlab = binding.constructionSlabDropDown.selectedItem.toString(),
                        compDate = "${binding.combDateMonth}-${binding.combDateYear}",
                        latitude = locationInfo?.first,
                        longitude = locationInfo?.second
                    )
                }
                pendingViewModel.postPendingDetails(projectData?.projectId!!)
                // API call and send data to client
            }
            null -> {
                // Check if projectId is available or not.
            }
        }
        Snackbar.make(binding.root, "Data saved to local database.", Snackbar.LENGTH_LONG).show()
        finish()
    }

    /** Set image to the adapter. */
    private fun setImage(images: List<ImageDetails>) {
        Log.d("TAG", "${images.size} image setting to the adapter.")
        if (images.isNotEmpty()) {
            images.forEach {
                if (it.images != null) {
                    imagesListBitmap.add(fileTypeConverter.toBitmap(it.images))
                }
                addToImageDetailsList(it)
            }
        } else {
            imagesListBitmap.clear()
            imageDetailsList.clear()
            adapter.setImageDetailsList(imageDetailsList)
        }
        adapter.notifyDataSetChanged()
        binding.progressBar.visibility = View.GONE
    }

    private fun postImageRequest(images: List<ImageDetails>) {
        Log.d("TAG", "Posting ${images.size} images.")
        if (images.isNotEmpty()) {
            Toast.makeText(this, "Requesting with ${images.size} no of images", Toast.LENGTH_SHORT)
                .show()
            images.forEach {
                val byteArray = it.images
                val base64 = String(Base64.getEncoder().encode(byteArray))

                var lat = it.latitude
                var long = it.longitudes
                if (locationInfo?.first != 0.0 && locationInfo?.second != 0.0) {
                    lat = locationInfo?.first
                    long = locationInfo?.second
                }

                val imageRequestDetails = ImageRequestDetails(
                    imageId = it.imageId,
                    imageName = it.imageName,
                    imageType = it.imageType,
                    transactionId = it.transactionId,
                    projectId = it.projectId,
                    imageDate = it.imageDate,
                    userId = "user",
                    latitude = lat,
                    longitudes = long,
                    image = base64
                )

                RetrofitInstance.api.updateImageDetails(imageRequestDetails).enqueue(
                    object : Callback<UpdateSurveyImageDetailResult> {
                        override fun onResponse(
                            call: Call<UpdateSurveyImageDetailResult>,
                            response: Response<UpdateSurveyImageDetailResult>
                        ) {
                            Toast.makeText(
                                this@FormActivity,
                                "Request passed for ${imageRequestDetails.imageId}",
                                Toast.LENGTH_SHORT
                            ).show()
                            removeDetailsFromDatabase(it)
                        }

                        override fun onFailure(
                            call: Call<UpdateSurveyImageDetailResult>,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                this@FormActivity,
                                "Requesting failed for ${imageRequestDetails.imageId}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )

            }
        }
        pendingViewModel.updatePosting()
    }

    private fun setCompDate(date: String?) {
        // Month adapter set
        val monthList = ArrayList<String>()
        monthList.add("Jan")
        monthList.add("Feb")
        monthList.add("Mar")
        monthList.add("Apr")
        monthList.add("May")
        monthList.add("Jun")
        monthList.add("July")
        monthList.add("Aug")
        monthList.add("Sept")
        monthList.add("Oct")
        monthList.add("Nov")
        monthList.add("Dec")
        val arrAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthList)
        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.combDateMonth.adapter = arrAdapter

        // Year adapter set
        val yearList = ArrayList<Int>()
        yearList.add(2019)
        yearList.add(2020)
        yearList.add(2021)
        yearList.add(2022)
        yearList.add(2023)
        yearList.add(2024)
        yearList.add(2025)
        yearList.add(2026)

        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearList)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.combDateYear.adapter = yearAdapter


        Log.d("TAG", "$date")
        val year = date?.split("-")?.get(1)?.let { Integer.valueOf(it) } ?: 2023
        val month = date?.split("-")?.get(0) ?: "Jan"

        val yearPosition = yearList.indexOf(year)
        val monthPosition = monthList.indexOf(month)

        binding.combDateMonth.setSelection(monthPosition)
        binding.combDateYear.setSelection(yearPosition)
    }

    private fun setConstructionSlab(constructionSlab: String?) {
        val slabList = ArrayList<String>()
        (1..110).forEach {
            slabList.add("$it")
        }
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, slabList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.constructionSlabDropDown.adapter = arrayAdapter
        val position = slabList.indexOf(constructionSlab)
        binding.constructionSlabDropDown.setSelection(position)
    }

    private fun removeDetailsFromDatabase(imageDetails: ImageDetails) {
        pendingViewModel.deleteImageDetails(imageDetails)
        // TODO remove draft list model
        adapter.notifyDataSetChanged()
    }

    private fun getImageDetailsFromByteArray(byteArray: ByteArray): ImageDetails? {
        if (projectData == null) {
            Log.d("TAG", "No data found on projectListData.")
            return null
        }

        val projectId = projectData?.projectId!!
        val imageDetails = ImageDetails(
            transactionId = projectData?.transactionId,
            projectId = projectId,
            imageName = "$projectId.png",
            imageString = "$projectId",
            imageType = "P",
            latitude = locationInfo?.first,
            longitudes = locationInfo?.second,
            imageTime = CurrentDateTime.currentTime,
            userId = "user",
            imageDate = CurrentDateTime.currentDate,
            createdBy = "Test user",
            images = byteArray
        )
        imageIdWithBitmapArray.add(imageDetails.imageId)
        return imageDetails
    }

    /**
     * This method check for the permission for camera access
     *
     * @author Nishikant
     */
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this@FormActivity,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@FormActivity, arrayOf(permission), requestCode)
        } else {
            selectFromCamera()
        }
    }

    /**
     * This ovverride method returns if the permission is granted by user or not
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @author Nishikant
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFromCamera()
            } else {
                Toast.makeText(this@FormActivity, "Camera Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@FormActivity,
                    "Storage Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this@FormActivity, "Storage Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun selectFromCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        filePhoto = getPhotoFile(FILE_NAME)

        val providerFile =
            FileProvider.getUriForFile(
                Objects.requireNonNull(applicationContext),
                BuildConfig.APPLICATION_ID + ".provider", filePhoto
            )
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (takePhotoIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_CODE)
        } else {
            Toast.makeText(this, "Camera could not open", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * This method change image to file
     *
     * @param fileName
     * @return
     * @author Nishikanta
     */
    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    private fun createDialogWithoutDateField(): DatePickerDialog {
        val dpd = DatePickerDialog(this, null, 2014, 1, 24)
        try {
            val datePickerDialogFields = dpd.javaClass.declaredFields
            for (datePickerDialogField in datePickerDialogFields) {
                if (datePickerDialogField.name == "mDatePicker") {
                    datePickerDialogField.isAccessible = true
                    val datePicker = datePickerDialogField[dpd] as DatePicker
                    val datePickerFields = datePickerDialogField.type.declaredFields
                    for (datePickerField in datePickerFields) {
                        Log.i("test", datePickerField.name)
                        if ("mDaySpinner" == datePickerField.name) {
                            datePickerField.isAccessible = true
                            val dayPicker = datePickerField[datePicker]
                            (dayPicker as View).visibility = View.GONE
                        }
                    }
                }
            }
        } catch (ex: Exception) {
        }
        return dpd
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val takenPhoto = BitmapFactory.decodeFile(filePhoto.absolutePath)
            //binding.imgView.setImageBitmap(takenPhoto)
            // add images to the imageList
            this.saveImage(takenPhoto)
            imagesListBitmap.add(takenPhoto)
            getImageDetailsFromByteArray(fileTypeConverter.toByteArray(takenPhoto))?.let {
                addToImageDetailsList(it)
            }
            adapter.notifyDataSetChanged()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //binding.imgView.setImageURI(data?.data)

        }
    }

    private fun addToImageDetailsList(imageDetails: ImageDetails) {
        imageDetailsList.add(imageDetails)
        adapter.setImageDetailsList(imageDetailsList)
    }

    /**
     * This method return Uri when bitmap image is passed
     *
     * @param bitmap
     * @return URI
     * @author Nishikanta
     */
    private fun Context.saveImage(bitmap: Bitmap): Uri? {
        var uri: Uri? = null
        try {
            val fileName = System.nanoTime().toString() + ".png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                } else {
                    val directory =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    val file = File(directory, fileName)
                    put(MediaStore.MediaColumns.DATA, file.absolutePath)
                }
            }

            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                contentResolver.openOutputStream(it).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.apply {
                        clear()
                        put(MediaStore.Audio.Media.IS_PENDING, 0)
                    }
                    contentResolver.update(uri, values, null, null)
                }
            }
            return uri
        } catch (e: java.lang.Exception) {
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                contentResolver.delete(uri, null, null)
            }
            throw e
        }
    }

    companion object {
        private const val pic_id = 123
        private const val FILE_NAME = "photo.jpg"
        private const val REQUEST_CODE = 13
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
        const val FORM_ACTIVITY_EXTRA = "project_data_extra"
    }
}