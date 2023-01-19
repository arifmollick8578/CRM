package com.example.crm

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crm.databinding.ActivityDraftBinding
import com.example.crm.model.DraftListModel
import com.example.crm.pending.PendingAdapter
import com.example.crm.pending.PendingViewModel
import com.example.crm.preferences.IPreferenceHelper
import com.example.crm.preferences.PreferenceManager
import com.example.crm.utility.Urls
import com.google.android.material.snackbar.Snackbar

class DraftActivity : AppCompatActivity(), PendingAdapter.ItemClickListner {
    private lateinit var binding: ActivityDraftBinding
    private val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(applicationContext) }
    private lateinit var pendingViewModel: PendingViewModel
    private var pendingList = ArrayList<DraftListModel>()
    private lateinit var pendingAdapter: PendingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Draft"
        pendingViewModel = ViewModelProvider(this)[PendingViewModel::class.java]

        pendingViewModel.postingStatus.observeForever {
            Log.d("BugInfo", "posting status: ${it.first}")
            onPostingStatusChanged(it)
        }
        Log.d("BugInfo", "DraftActivity onCreate")

        pendingList = ArrayList()
        binding.pendingRcv.layoutManager = LinearLayoutManager(applicationContext)
        pendingAdapter = PendingAdapter(this,pendingList)
        binding.pendingRcv.adapter = pendingAdapter
        getAllData()
    }

    override fun onItemClicked(draftList: DraftListModel) {
        val intent = Intent(this, FormActivity::class.java)
        intent.putExtra("dto", draftList)
        startActivity(intent)
    }

    override fun onMapButtonClicked(item: DraftListModel) {
        if ((item.Lat != null && item.Long != null) || (item.Lat != 0.0 && item.Long != 0.0)) {
            val mapUrl = Urls.getMapLink(this, item.Lat!!, item.Long!!)
            val uri = Uri.parse(mapUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } else {
            Snackbar.make(binding.root, "Latitude or Longitude not found", Snackbar.LENGTH_LONG).show()
        }
    }

    fun getAllData(){
        pendingViewModel.getAllData.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                pendingList.clear()
                pendingList.addAll(it.filter { !it.isPending })
                Log.d("TAG", "List : $pendingList")
                pendingAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onPostingStatusChanged(status: Pair<Int, Int>) {
        Log.d("BugInfo", "Posting status changed: ${status.first} out of ${status.second}")
        if (status.first != status.second || status.second != 0) {
            binding.postingProgressContainer.visibility = View.VISIBLE
            binding.postingProgressText.text = "Posting ${status.first} out of ${status.second}"
        } else {
            binding.postingProgressContainer.visibility = View.GONE
            binding.postingProgressText.text = ""
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.d("BugInfo", "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("BugInfo", "Permission granted")
                }
                else -> {
                    Log.d("BugInfo", "Permission denied.")
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_CODE = 2
    }
}