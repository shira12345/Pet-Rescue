package com.example.petrescue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.petrescue.databinding.FragmentCreatePostBinding

class CreatePostFragment : Fragment() {

  private var _binding: FragmentCreatePostBinding? = null
  private val binding get() = _binding!!

  private var selectedLocation: String? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupAnimalSpinner()
    setupListeners()
  }

  private fun setupAnimalSpinner() {
    val animals = listOf("Dog", "Cat", "Bird", "Other")

    val adapter = ArrayAdapter(
      requireContext(),
      android.R.layout.simple_spinner_dropdown_item,
      animals
    )

    binding.spinnerAnimalType.adapter = adapter
  }

  private fun setupListeners() {

    binding.buttonSelectLocation.setOnClickListener {
      // TODO: Open Map or Location Picker
      selectedLocation = "Tel Aviv"
      binding.textViewLocation.text = selectedLocation
    }

    binding.buttonSubmit.setOnClickListener {
      createPost()
    }
  }

  private fun createPost() {

    val animalType = binding.spinnerAnimalType.selectedItem.toString()

    val status = when (binding.radioGroupStatus.checkedRadioButtonId) {
      binding.radioLost.id -> "Lost"
      binding.radioFound.id -> "Found"
      else -> null
    }

    val description = binding.editTextDescription.text.toString()

    if (status == null || description.isBlank() || selectedLocation == null) {
      Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
      return
    }

    // TODO: Send to Firestore / API
    Toast.makeText(requireContext(), "Post Created!", Toast.LENGTH_SHORT).show()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
