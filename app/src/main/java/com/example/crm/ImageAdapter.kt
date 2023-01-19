package com.example.crm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.databinding.ImageItemBinding
import com.example.crm.model.ImageDetails

class ImageAdapter(
    var pendingList: ArrayList<Bitmap>?,
    private var context: Context
) : RecyclerView.Adapter<ImageAdapter.PendingViewHolder>() {

    private var imageDetailsList = ArrayList<ImageDetails>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val view = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        val getProjectForSurveyResult = pendingList?.get(position)
        if (getProjectForSurveyResult != null) {
            holder.viewBinding.imageView.setImageBitmap(getProjectForSurveyResult)
        }

        val editText = holder.viewBinding.imageNameText
        val imageButton = holder.viewBinding.imageTypeButton
        val item = imageDetailsList.get(position)
        val imageType = item.imageType
        imageButton.text = imageType

        editText.setText(item.imageName)

        val backgroundColor =
            if (imageButton.text.equals("B")) {
                Color.WHITE
            } else {
                R.color.purple_500
            }
        imageButton.setBackgroundColor(backgroundColor)
        imageButton.setOnClickListener {
            if (imageButton.text.equals("B")) {
                imageButton.text = "P"
                imageButton.setBackgroundColor(R.color.purple_500)
            } else {
                imageButton.text = "B"
                imageButton.setBackgroundColor(Color.WHITE)
            }
            item.imageType = imageButton.text.toString()
        }
        editText.addTextChangedListener {
            item.imageName = it.toString()
        }
    }

    inner class PendingViewHolder(var viewBinding: ImageItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun getItemCount(): Int {
        return pendingList!!.size
    }

    fun setImageDetailsList(list: ArrayList<ImageDetails>) {
        imageDetailsList = list
    }

    fun getImageDetailsList(): ArrayList<ImageDetails> {
        return imageDetailsList
    }
}