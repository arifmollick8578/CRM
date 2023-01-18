package com.example.crm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.crm.databinding.ActivityLoginScreenBinding
import com.example.crm.preferences.IPreferenceHelper
import com.example.crm.preferences.PreferenceManager


class LoginScreen : AppCompatActivity() {
    private lateinit var binding: ActivityLoginScreenBinding

    // in the below line, we are creating variables.
    private val REQUEST_CODE = 101
    private var imei: String? = null
    private lateinit var loginViewModel: LoginViewModel
    private val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(applicationContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // in the below line, we are initializing our variables.
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        // in the below line, we are checking for permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // if permissions are not provided we are requesting for permissions.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_CODE
            )
        } else{
            imei = getDeviceId(applicationContext)
        }

        binding.imeiText.text = "$imei"

        // in the below line, we are setting our imei to our text view.

        if(preferenceHelper.getIsLogin()){
            val intent = Intent(this, DashBoard::class.java)
            startActivity(intent)
        }

        initalizeClickListner()


        initObserver()

        loginViewModel.shouldLogOut.observeForever {
            Log.d("BugInfo", "Observer called on login screen.")
            if (it) {
//                preferenceHelper.setUserId("")
//                preferenceHelper.setImeiNo("")
//                preferenceHelper.setIsLogin(false)
                preferenceHelper.clearPrefs()
                Log.d("BugInfo", "user: ${preferenceHelper.getUserId()}..imei: ${preferenceHelper.getImeiNo()}.. isLogIn: ${preferenceHelper.getIsLogin()}")

            }
        }

    }

    private fun initObserver() {
        loginViewModel.observeLoginResponseData().observe(this, Observer {
            if(it.AuthenticateUserResult.IMEINo == null){
                binding.idPBLoading.visibility = View.GONE
                showToast("Please use the correct mobile phone for login")
            }else if(it.AuthenticateUserResult.IMEINo == imei){
                if(it.AuthenticateUserResult.UserId != binding.editTextUserId.text.toString()){
                    binding.idPBLoading.visibility = View.GONE
                    showToast("Please enter correct user id")
                }else if(it.AuthenticateUserResult.Password != binding.editTextPassword.text.toString()){
                    binding.idPBLoading.visibility = View.GONE
                    showToast("Please enter correct password")
                }else{
                    binding.idPBLoading.visibility = View.GONE
                    preferenceHelper.setUserId(it.AuthenticateUserResult.UserId)
                    preferenceHelper.setImeiNo(it.AuthenticateUserResult.IMEINo)
                    preferenceHelper.setIsLogin(true)
                    val intent = Intent(this, DashBoard::class.java)
                    startActivity(intent)
                }
            }
        })
    }



    private fun initalizeClickListner() {
        binding.button.setOnClickListener {
            if (binding.editTextUserId.text.isNotEmpty() && binding.editTextPassword.text.isNotEmpty()) {
                binding.idPBLoading.visibility = View.VISIBLE
                val credential = CredentialRequest(UserId = "Test", Password = "test@4321")
                val loginRequest = LoginRequest(
                    credential = credential,
                    IMEINo = imei,
                    userId = binding.editTextUserId.text.toString(),
                    Password = binding.editTextPassword.text.toString()
                    )
                loginViewModel.postLogin(loginRequest)
            } else {
                showToast("Please put correct user id and password")
            }

        }
    }

    /**
     * Returns the unique identifier for the device
     *
     * @return unique identifier for the device
     */
    // in the below line, we are calling on request permission result method.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            // in the below line, we are checking if permission is granted.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // if permissions are granted we are displaying below toast message.
                showToast("Permission granted.")
                imei = getDeviceId(applicationContext)
                Log.d("TAG",imei.toString())
            } else {
                // in the below line, we are displaying toast message if permissions are not granted.
                showToast("Permission denied. Please go to setting and provide permission")
            }
        }
    }

    private fun showToast(passedString : String){
        Toast.makeText(this, passedString, Toast.LENGTH_SHORT)
            .show()
    }

    fun getDeviceId(context: Context): String? {
        val deviceId: String
        deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
            )
        } else {
            val mTelephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (mTelephony.deviceId != null) {
                mTelephony.deviceId
            } else {
                Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
                )
            }
        }
        return deviceId
    }
}