package com.example.cameraapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cameraapp.databinding.GalleryListBinding
import java.io.File

class GalleryAdapter(private val fileArray: Array<File>) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    class ViewHolder(private val binding: GalleryListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val mediaController = MediaController(binding.root.context)

        fun bind(file: File) {
            if (file.extension == "mp4") {
                binding.localVideo.visibility = View.VISIBLE
                binding.localImg.visibility = View.GONE
                binding.localVideo.setVideoPath(file.absolutePath)
                binding.localVideo.setMediaController(mediaController)
                mediaController.setAnchorView(binding.localVideo)
                binding.localVideo.start()
            } else {
                binding.localVideo.visibility = View.GONE
                binding.localImg.visibility = View.VISIBLE
                Glide.with(binding.root).load(file).into(binding.localImg)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(GalleryListBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fileArray[position])
    }

    override fun getItemCount(): Int {
        return fileArray.size
    }
}