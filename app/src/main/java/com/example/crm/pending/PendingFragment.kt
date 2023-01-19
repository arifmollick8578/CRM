package com.example.crm.pending

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.*
import com.example.crm.databinding.FragmentPendingBinding
import com.example.crm.model.DraftListModel
import com.example.crm.preferences.IPreferenceHelper
import com.example.crm.preferences.PreferenceManager
import com.example.crm.utility.CurrentDateTime
import com.example.crm.utility.Urls
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.collections.ArrayList

class PendingFragment : Fragment(), PendingAdapter.ItemClickListner{


    private var _binding: FragmentPendingBinding? = null
    private val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(activity!!.applicationContext) }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var pendingViewModel: PendingViewModel
    private var pendingList = ArrayList<DraftListModel>()
    private lateinit var pendingAdapter: PendingAdapter
    private var imei: String? = null

    // in the below line, we are creating variables.
    private val REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPendingBinding.inflate(inflater, container, false)
        val root: View = binding.root
        pendingViewModel = ViewModelProvider(this)[PendingViewModel::class.java]

        pendingList = ArrayList()
        binding.pendingRcv.layoutManager = LinearLayoutManager(activity)
        pendingAdapter = PendingAdapter(this,pendingList)
        binding.pendingRcv.adapter = pendingAdapter
        val credential = CredentialRequest(UserId = "Test", Password = "test@4321")
        val pendingRequest = PendingRequest(
            credential = credential,
            imeiNo = preferenceHelper.getImeiNo(),
            userId = preferenceHelper.getUserId(),
            lastSyncTime = CurrentDateTime.currentDate,
            dataGroup = "2"
        )
        pendingViewModel.getPendingList(pendingRequest)
        initObserver()

        binding.etxtSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
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

            val intent = Intent(activity,DraftActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    private fun initObserver() {

        pendingViewModel.observePendingResponseData().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                for (i in it.GetProjectForSurveyResult) {
                    val dlm = DraftListModel(
                        Address = i.Address,
                        AssignDate = i.AssignDate,
                        AssignDateText = i.AssignDateText,
                        Builder = i.Builder,
                        City = i.City,
                        Colony = i.Colony,
                        CompDate = i.CompDate,
                        Constructionslab = i.Constructionslab,
                        Lat = i.Lat,
                        LaunchDate = i.LaunchDate,
                        LaunchSqft = i.LaunchSqft,
                        LaunchUnit = i.LaunchUnit,
                        Long = i.Long,
                        ProjectId = i.ProjectId,
                        ProjectName = i.ProjectName,
                        ProjectSubType = i.ProjectSubType,
                        ProjectType = i.ProjectType,
                        Region = i.Region,
                        TownshipName = i.TownshipName,
                        TransId = i.TransId,
                        Remark = null,
                        isPending = true
                    )
                    insertDataToDb(dlm)
                }

                getAllData()

            }
        })

    }


    fun getDeviceId(context: Context?): String? {
        val deviceId: String
        deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getString(
                context?.getContentResolver(),
                Settings.Secure.ANDROID_ID
            )
        } else {
            val mTelephony =
                context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (mTelephony.deviceId != null) {
                mTelephony.deviceId
            } else {
                Settings.Secure.getString(
                    context?.getContentResolver(),
                    Settings.Secure.ANDROID_ID
                )
            }
        }
        return deviceId
    }


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
                imei = getDeviceId(activity?.applicationContext)
                Log.d("TAG", imei.toString())
            } else {
                // in the below line, we are displaying toast message if permissions are not granted.
                showToast("Permission denied. Please go to setting and provide permission")
            }
        }
    }

    private fun showToast(passedString: String) {
        Toast.makeText(activity?.applicationContext, passedString, Toast.LENGTH_SHORT)
            .show()
    }

    private fun filter(text: String) {
        val filteredlist: ArrayList<DraftListModel> = ArrayList()

        // running a for loop to compare elements.
        for (item in pendingList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.Colony?.lowercase(Locale.ROOT)
                    ?.contains(text.lowercase(Locale.ROOT)) == true
            ) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(activity?.applicationContext, "No Data Found..", Toast.LENGTH_SHORT)
                .show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            pendingAdapter.filterList(filteredlist)
        }
    }


    private fun insertDataToDb(draftListModel: DraftListModel) {

        pendingViewModel.getAllDataByProjectList(draftListModel.ProjectId)

        pendingViewModel.getAllDataByProjectIdMutable.observe(viewLifecycleOwner, Observer {
            if(it==null){
                pendingViewModel.insertDraft(draftListModel)
            }else if(it.isEmpty()){
                pendingViewModel.insertDraft(draftListModel)
            }
        })
    }

    private fun getAllData() {
        pendingViewModel.getAllData.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                binding.btnDraft.text = "Draft ${it.filter { !it.isPending }.size}"
                pendingList.clear()
                pendingList.addAll(it.filter { it.isPending })
                Log.d("TAG", "List : $pendingList")
                pendingAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(activity, "No Data Found", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onItemClicked(draftList: DraftListModel) {
        Log.d("BugInfo", "draftList: ${draftList.ProjectId}")
        val intent = Intent(activity,FormActivity::class.java)
        intent.putExtra("dto",draftList)
        startActivity(intent)
    }

    override fun onMapButtonClicked(item: DraftListModel) {
        if ((item.Lat != null && item.Long != null) || (item.Lat != 0.0 && item.Long != 0.0)) {
            val mapUrl = Urls.getMapLink(context?.applicationContext!!, item.Lat!!, item.Long!!)
            val uri = Uri.parse(mapUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } else {
            Snackbar.make(binding.root, "Latitude or Longitude not found", Snackbar.LENGTH_LONG).show()
        }
    }
}