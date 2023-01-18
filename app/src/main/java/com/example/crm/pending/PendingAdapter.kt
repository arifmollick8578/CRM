package com.example.crm.pending

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.databinding.PendingItemBinding
import com.example.crm.model.DraftListModel
import com.example.crm.utility.OnSwipeTouchListener

class PendingAdapter(
    private val listner: ItemClickListner,
    var pendingList: ArrayList<DraftListModel>
) :
    RecyclerView.Adapter<PendingAdapter.PendingViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val view = PendingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        val getProjectForSurveyResult = pendingList.get(position)
        getProjectForSurveyResult.ProjectName.also { holder.viewBinding.txtTitle.text = it }
        getProjectForSurveyResult.ProjectId.also { holder.viewBinding.txtDescription.text = it }
        "Residental ${getProjectForSurveyResult.ProjectSubType}".also {
            holder.viewBinding.txtResidental.text = it
        }
        getProjectForSurveyResult.Region.also { holder.viewBinding.txtAddress.text = it }
        "Builder : ${getProjectForSurveyResult.Builder?.trim()}".also {
            holder.viewBinding.txtBuilder.text = it
        }
        "Compdate : ${getProjectForSurveyResult.CompDate}".also {
            holder.viewBinding.txtComdate.text = it
        }
        "Launch Date : ${getProjectForSurveyResult.LaunchDate}".also {
            holder.viewBinding.txtLaunchDate.text = it
        }
        "Launch Unit : ${getProjectForSurveyResult.LaunchUnit}".also {
            holder.viewBinding.txtLaunchUnit.text = it
        }
        "Sq Ft : ${getProjectForSurveyResult.LaunchSqft}".also {
            holder.viewBinding.txtSqft.text = it
        }
        getProjectForSurveyResult.Address.also { holder.viewBinding.txtFullAddress.text = it }

        holder.viewBinding.mapButton.setOnClickListener {
            Log.d("BugInfo", "Map button clicked.")
            listner.onMapButtonClicked(getProjectForSurveyResult)
        }


//        holder.viewBinding.container.setOnTouchListener(
//            object: OnSwipeTouchListener(context = holder.viewBinding.root.context) {
//                override fun onSwipeLeft() {
//                    super.onSwipeLeft()
//                    holder.viewBinding.mapButton.visibility = View.VISIBLE
//                }
//
//                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                    return super.onTouch(v, event)
//                }
//
//                override fun onSwipeRight() {
//                    super.onSwipeRight()
//                    holder.viewBinding.mapButton.visibility = View.GONE
//                }
//            }
//        )

        holder.viewBinding.container.setOnClickListener{
            listner.onItemClicked(getProjectForSurveyResult)
        }
    }

    inner class PendingViewHolder(var viewBinding: PendingItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
    }

    override fun getItemCount(): Int {
        return pendingList!!.size
    }

    /**
     * This functions helps in filtering the list
     *
     * @param filterlist
     */
    fun filterList(filterlist: ArrayList<DraftListModel>) {
        pendingList = filterlist
        notifyDataSetChanged()
    }

    interface ItemClickListner {
        fun onItemClicked(draftList: DraftListModel)
        fun onMapButtonClicked(draftList: DraftListModel)
    }

}
