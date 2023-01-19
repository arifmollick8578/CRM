package com.example.crm.pending

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crm.CredentialRequest
import com.example.crm.DraftActivity
import com.example.crm.FormActivity
import com.example.crm.FormActivity.Companion.FORM_ACTIVITY_EXTRA
import com.example.crm.databinding.FragmentPendingBinding
import com.example.crm.model.ProjectData
import com.example.crm.preferences.IPreferenceHelper
import com.example.crm.preferences.PreferenceManager
import com.example.crm.utility.CurrentDateTime
import com.example.crm.utility.Urls
import com.example.crm.utility.getDeviceId
import com.google.android.material.snackbar.Snackbar
import java.util.*

class PendingFragment : Fragment(), PendingListAdapter.ItemClickListener {
    private var _binding: FragmentPendingBinding? = null
    private val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(activity!!.applicationContext) }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var pendingViewModel: PendingViewModel
    private var pendingList = ArrayList<ProjectData>()
    private lateinit var pendingListAdapter: PendingListAdapter
    private var imei: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pendingViewModel = ViewModelProvider(this)[PendingViewModel::class.java].apply {
            pendingListLiveData.observe(viewLifecycleOwner) {
                onPendingListResponseUpdated(it)
                getAllData()
            }
        }

        pendingList = ArrayList()
        binding.pendingRcv.layoutManager = LinearLayoutManager(activity)
        pendingListAdapter = PendingListAdapter(this, pendingList)
        binding.pendingRcv.adapter = pendingListAdapter
        // +1 have doubt
        val credential = CredentialRequest(userId = "Test", password = "test@4321")
        val pendingRequest = PendingRequest(
            credential = credential,
            imeiNo = preferenceHelper.getImeiNo(),
            userId = preferenceHelper.getUserId(),
            lastSyncTime = CurrentDateTime.currentDate,
            dataGroup = "2"
        )
        pendingViewModel.getPendingList(pendingRequest)
        binding.etxtSearch.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(msg: String): Boolean {
                    // inside on query text change method we are
                    // calling a method to filter our recycler view.
                    filter(msg)
                    return false
                }
            })

        binding.btnDraft.setOnClickListener {
            val intent = Intent(activity, DraftActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onPendingListResponseUpdated(projectListResponse: ProjectListResponse) {
        projectListResponse.projectDataByServer.forEach { responseData ->
            val projectData = ProjectData(
                address = responseData.address,
                assignDate = responseData.assignDate,
                assignDateText = responseData.assignDateText,
                builder = responseData.builder,
                city = responseData.city,
                colony = responseData.colony,
                compDate = responseData.compDate,
                constructionSlab = responseData.constructionSlab,
                latitude = responseData.latitude,
                launchDate = responseData.launchDate,
                launchSqFt = responseData.launchSqFt,
                launchUnit = responseData.launchUnit,
                longitude = responseData.longitude,
                projectId = responseData.projectId,
                projectName = responseData.projectName,
                projectSubType = responseData.projectSubType,
                projectType = responseData.projectType,
                region = responseData.region,
                townshipName = responseData.townshipName,
                transactionId = responseData.transactionId,
                remark = null,
                isPending = true
            )

            // +1 have doubt about this
            insertDataToDb(projectData)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_PHONE_STATE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "Read phone state permission granted.")
                imei = context?.getDeviceId()
                Log.d("TAG", "IMEI number: ${imei.toString()}")
            } else {
                Log.d("TAG", "Reading phone state permission denied.")
            }
        }
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<ProjectData> = ArrayList()

        // running a for loop to compare elements.
        for (item in pendingList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.colony?.lowercase(Locale.ROOT)
                    ?.contains(text.lowercase(Locale.ROOT)) == true
            ) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Snackbar.make(binding.root, "No Data Found.", Snackbar.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            pendingListAdapter.filterList(filteredList)
        }
    }

    private fun insertDataToDb(projectData: ProjectData) {
        pendingViewModel.getAllDataByProjectList(projectData.projectId)
        pendingViewModel.allProjectDataListByProjectId.observe(viewLifecycleOwner) {
            if (it == null) {
                pendingViewModel.insertProjectData(projectData)
            } else if (it.isEmpty()) {
                pendingViewModel.insertProjectData(projectData)
            }
        }
    }

    private fun getAllData() {
        pendingViewModel.projectDataList.observe(viewLifecycleOwner) { projectData ->
            Log.d("TAG", "Project data updated: ${projectData.size}")
            if (!projectData.isNullOrEmpty()) {
                binding.btnDraft.text = "Draft: ${projectData.filter { !it.isPending }.size}"
                pendingList.clear()
                pendingList.addAll(projectData.filter { it.isPending })
                Log.d("TAG", "Project list with size: ${pendingList.size}")
                pendingListAdapter.notifyDataSetChanged()
            } else {
                Log.d("TAG", "No data found.")
            }
        }
    }

    override fun onItemClicked(projectData: ProjectData) {
        Log.d(
            "TAG",
            "ProjectData with id: ${projectData.projectId} and title: ${projectData.projectName} clicked."
        )
        val intent = Intent(activity, FormActivity::class.java)
        intent.putExtra(FORM_ACTIVITY_EXTRA, projectData)
        startActivity(intent)
    }

    override fun onMapButtonClicked(projectData: ProjectData) {
        if ((projectData.latitude != null && projectData.longitude != null) && (projectData.latitude != 0.0 && projectData.longitude != 0.0)) {
            val mapUrl = Urls.getMapRedirectUrl(
                projectData.latitude,
                projectData.longitude,
                projectData.projectName!!
            )
            val uri = Uri.parse(mapUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } else {
            Snackbar.make(binding.root, "Latitude or Longitude not found", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    companion object {
        private const val READ_PHONE_STATE_PERMISSION = 101
    }
}