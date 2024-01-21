package com.example.cameraapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cameraapp.R
import com.example.cameraapp.adapters.GalleryAdapter
import com.example.cameraapp.databinding.FragmentGalleryBinding
import java.io.File

class GalleryFragment : Fragment() {
    private lateinit var binding: FragmentGalleryBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentGalleryBinding.inflate(layoutInflater)

        val directory = File(requireContext().externalMediaDirs[0].absolutePath)
        val files = directory.listFiles() as Array<File>

        val adapter = GalleryAdapter(files.reversedArray())
        binding.viewPager.adapter = adapter

        binding.backToPhotoBtn.setOnClickListener {
            findNavController().navigate(R.id.photoFragment)
        }

        binding.backToVideoBtn.setOnClickListener {
            findNavController().navigate(R.id.videoFragment)
        }

        return binding.root
    }
}