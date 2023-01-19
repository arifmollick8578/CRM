package com.example.crm.pending

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.databinding.PendingItemBinding
import com.example.crm.model.ProjectData

class PendingListAdapter(
    private val listener: ItemClickListener,
    var pendingProjectList: ArrayList<ProjectData>
) : RecyclerView.Adapter<PendingListAdapter.PendingListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingListViewHolder {
        val view = PendingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingListViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingListViewHolder, position: Int) {
        val projectsDataByServer = pendingProjectList.get(position)
        projectsDataByServer.projectName.also { holder.viewBinding.txtTitle.text = it }
        projectsDataByServer.projectId.also { holder.viewBinding.txtDescription.text = it }
        projectsDataByServer.projectSubType.also {
            holder.viewBinding.txtResidental.text = "Residental $it"
        }
        projectsDataByServer.region.also { holder.viewBinding.txtAddress.text = it }
        projectsDataByServer.builder?.trim().also {
            holder.viewBinding.txtBuilder.text = "builder : $it"
        }
        projectsDataByServer.compDate.also {
            holder.viewBinding.txtComdate.text = "Compdate : $it"
        }
        projectsDataByServer.launchDate.also {
            holder.viewBinding.txtLaunchDate.text = "Launch Date : $it"
        }
        projectsDataByServer.launchUnit.also {
            holder.viewBinding.txtLaunchUnit.text = "Launch Unit : $it"
        }
        projectsDataByServer.launchSqFt.also {
            holder.viewBinding.txtSqft.text = "Sq Ft : $it"
        }
        projectsDataByServer.address.also { holder.viewBinding.txtFullAddress.text = it }

        holder.viewBinding.mapButton.setOnClickListener {
            Log.d("BugInfo", "Map button clicked.")
            listener.onMapButtonClicked(projectsDataByServer)
        }
        holder.viewBinding.container.setOnClickListener{
            listener.onItemClicked(projectsDataByServer)
        }
    }

    inner class PendingListViewHolder(var viewBinding: PendingItemBinding): RecyclerView.ViewHolder(viewBinding.root) {

    }

    override fun getItemCount(): Int {
        return pendingProjectList.size
    }

    /**
     * This functions helps in filtering the list
     *
     * @param filterList
     */
    fun filterList(filterList: ArrayList<ProjectData>) {
        pendingProjectList = filterList
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onItemClicked(projectData: ProjectData)
        fun onMapButtonClicked(projectData: ProjectData)
    }
}
