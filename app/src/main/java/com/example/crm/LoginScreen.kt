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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.crm.databinding.ActivityLoginScreenBinding
import com.example.crm.preferences.IPreferenceHelper
import com.example.crm.preferences.PreferenceManager
import com.example.crm.utility.getDeviceId
import com.google.android.material.snackbar.Snackbar


class LoginScreen : AppCompatActivity() {
    private lateinit var binding: ActivityLoginScreenBinding

    // in the below line, we are creating variables.
    private var imei: String? = null
    private lateinit var loginViewModel: LoginViewModel
    private val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(applicationContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
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
        } else {
            imei = applicationContext.getDeviceId()
        }
        binding.imeiText.text = "$imei"
        // in the below line, we are setting our imei to our text view.
        if(preferenceHelper.getIsLogin()){
            val intent = Intent(this, DashBoard::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener { onLogInButtonClicked() }

        loginViewModel.loginResponse.observe(this) {
            onLogInResponseDataChanged(it)
        }

        loginViewModel.shouldLogOut.observeForever {
            Log.d("TAG", "Observer called to logout session.")
            if (it) {
                preferenceHelper.resetLogInData()
            }
        }
    }

    private fun onLogInResponseDataChanged(loginResponse: LoginResponse) {
            if(loginResponse.authenticateUserResult.iMEINo == null){
                binding.idPBLoading.visibility = View.GONE
                Snackbar.make(binding.root, "Please use the correct mobile phone for login", Snackbar.LENGTH_LONG).show()
            }else if(loginResponse.authenticateUserResult.iMEINo == imei){
                if(loginResponse.authenticateUserResult.userId != binding.editTextUserId.text.toString()){
                    binding.idPBLoading.visibility = View.GONE
                    Snackbar.make(binding.root, "Please enter correct user id", Snackbar.LENGTH_LONG).show()
                }else if(loginResponse.authenticateUserResult.password != binding.editTextPassword.text.toString()){
                    binding.idPBLoading.visibility = View.GONE
                    Snackbar.make(binding.root, "Please enter correct password", Snackbar.LENGTH_LONG).show()
                }else{
                    binding.idPBLoading.visibility = View.GONE
                    preferenceHelper.setUserId(loginResponse.authenticateUserResult.userId)
                    preferenceHelper.setImeiNo(loginResponse.authenticateUserResult.iMEINo)
                    preferenceHelper.setIsLogin(true)
                    val intent = Intent(this, DashBoard::class.java)
                    startActivity(intent)
                }
            }
    }

    private fun onLogInButtonClicked() {
            if (binding.editTextUserId.text.isNotEmpty() && binding.editTextPassword.text.isNotEmpty()) {
                binding.idPBLoading.visibility = View.VISIBLE
                val credential = CredentialRequest(userId = "Test", password = "test@4321")
                val loginRequest = LoginRequest(
                    credential = credential,
                    imeiNo = imei,
                    userId = binding.editTextUserId.text.toString(),
                    password = binding.editTextPassword.text.toString()
                    )
                loginViewModel.postLoginRequest(loginRequest)
            } else {
                Snackbar.make(binding.root, "Please put correct user id and password", Snackbar.LENGTH_LONG).show()
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
                Log.d("TAG", "Reading phone status permission granted.")
                imei = applicationContext.getDeviceId()
                Log.d("TAG", imei.toString())
            } else {
                // in the below line, we are displaying toast message if permissions are not granted.
                Log.d("TAG", "Reading phone status permission denied. Please go to setting and provide permission")
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 101
    }
}