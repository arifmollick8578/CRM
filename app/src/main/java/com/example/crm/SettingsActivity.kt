package com.example.crm

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import com.example.crm.databinding.ActivitySettingsBinding
import com.example.crm.pending.PendingViewModel
import com.example.crm.preferences.IPreferenceHelper
import com.example.crm.preferences.PreferenceManager
import com.example.crm.utility.Urls
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var pendingViewModel: PendingViewModel
    private lateinit var loginViewModel: LoginViewModel
    private val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pendingViewModel = ViewModelProvider(this)[PendingViewModel::class.java]
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java].apply {
            shouldLogOut.observe(this@SettingsActivity) {
                if (it) {
                    Log.d("BugInfo", "Is login: ${preferenceHelper.getIsLogin()}")
                    preferenceHelper.setImeiNo("")
                    preferenceHelper.setIsLogin(false)
                    preferenceHelper.setImeiNo("")
                    preferenceHelper.setUserId("")
                }
            }
        }
        supportActionBar?.title = "Settings"
        binding.switchOff.isChecked = true
        binding.seekbarNearby.setOnSeekBarChangeListener(
            object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                    binding.nearbyNumber.setText("$progress")
                }

                override fun onStartTrackingTouch(seekbar: SeekBar?) {
                    Log.d("BugInfo", "SeekbarProgress: ${seekbar?.progress}. max: ${seekbar?.max}")
                }

                override fun onStopTrackingTouch(seekbar: SeekBar?) {
                    Log.d("BugInfo", "tracking touch: ${seekbar?.progress}. max: ${seekbar?.max}")
                }

            }
        )

        binding.changePassword.setOnClickListener {
            val uri = Uri.parse(Urls.getChangePasswordUrl())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        Log.d("BugInfo", "Setting activity created. $pendingViewModel")

        binding.resetApplication.setOnClickListener {
            pendingViewModel.resetApplication()
            Snackbar.make(binding.resetApplication, "Data reset done.", Snackbar.LENGTH_LONG).show()
            startActivity(Intent(this, DashBoard::class.java))
        }

        binding.logOut.setOnClickListener {
            loginViewModel.logOutAccount()
            startActivity(
                Intent(this, LoginScreen::class.java)
            )
        }
    }
}