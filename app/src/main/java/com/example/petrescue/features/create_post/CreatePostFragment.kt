package com.example.petrescue.features.create_post

import android.R
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.petrescue.CreatePostViewModel
import com.example.petrescue.databinding.FragmentCreatePostBinding
import com.example.petrescue.utilis.extensions.bitmap
import com.google.android.material.button.MaterialButton

const val MINIMUM_DESCRIPTION_LENGTH = 10

class CreatePostFragment : Fragment() {
  private var binding: FragmentCreatePostBinding? = null

  private var imageUri: Uri? = null

  private val viewModel: CreatePostViewModel by viewModels()

  private val pickImage =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      uri?.let {
        imageUri = it
        binding?.imagePet?.setImageURI(it)
      }
    }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentCreatePostBinding.inflate(inflater, container, false)

    return binding?.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupPetTypeDropdown()
    setupStatusToggle()
    setupListeners()

    setupLocationAutocomplete()

//    viewModel.uploadStatus.observe(viewLifecycleOwner) { isSuccess ->
//      binding?.loadingOverlay?.visibility = View.GONE
//
//      if (isSuccess) {
//        Toast.makeText(requireContext(), "Post created successfully! 🐾", Toast.LENGTH_LONG).show()
//        findNavController().popBackStack()
//      } else {
//        showError("Failed to upload post. Please try again.")
//      }
//    }
  }

  private fun getFirstThreeWords(string: String): String {
    return string.split(",").take(3).joinToString(",").trim()
  }

  private fun setupLocationAutocomplete() {
    viewModel.results.observe(viewLifecycleOwner) { locations ->
      val adapter = ArrayAdapter(
        requireContext(),
        R.layout.simple_dropdown_item_1line,
        locations.map { it.displayName }
      )

      binding?.editLocation?.setAdapter(adapter)

      if (locations.isNotEmpty() && binding?.editLocation?.hasFocus() == true)
        binding?.editLocation?.showDropDown()
    }

    binding?.editLocation?.setOnItemClickListener { _, _, position, _ ->
      val selectedLocation = viewModel.results.value?.get(position)

      selectedLocation?.let {
        viewModel.selectedLat = it.lat.toDoubleOrNull()
        viewModel.selectedLon = it.lon.toDoubleOrNull()

        binding?.editLocation?.setText(it.displayName, false)
      }
    }

    binding?.editLocation?.addTextChangedListener { text ->
      val query = text.toString()

      if (binding?.editLocation?.isPerformingCompletion == false) viewModel.searchLocation(query)
    }
  }

  private fun setupPetTypeDropdown() {
    val types = listOf("Dog", "Cat", "Bird", "Other")

    val adapter = ArrayAdapter(
      requireContext(),
      R.layout.simple_dropdown_item_1line,
      types
    )

    binding?.dropdownPetType?.setAdapter(adapter)
  }

  private fun updateSelectedStatusButton(checkedButtonId: Int?) {
    when (checkedButtonId) {
      com.example.petrescue.R.id.buttonLost -> {
        binding?.buttonLost?.setBackgroundColor("#FFEBEE".toColorInt()) // Light Red
        binding?.buttonLost?.setTextColor(Color.RED)
      }

      com.example.petrescue.R.id.buttonFound -> {
        binding?.buttonFound?.setBackgroundColor("#E8F5E9".toColorInt()) // Light Green
        binding?.buttonFound?.setTextColor("#2E7D32".toColorInt())
      }
    }
  }

  private fun setupStatusToggle() {
    updateSelectedStatusButton(binding?.toggleStatus?.checkedButtonId)

    binding?.toggleStatus?.addOnButtonCheckedListener { group, checkedId, isChecked ->
      if (isChecked) return@addOnButtonCheckedListener updateSelectedStatusButton(checkedId)

      // Reset to default white/gray when unselected
      val button = group.findViewById<MaterialButton>(checkedId)

      button.setBackgroundColor(Color.TRANSPARENT)
      button.setTextColor(Color.GRAY)
    }
  }

  private fun setupListeners() {
    binding?.cardPhoto?.setOnClickListener {
      pickImage.launch("image/*")
    }

    binding?.buttonSubmit?.setOnClickListener {
      submitPost()
    }

    binding?.buttonCancel?.setOnClickListener {
      dismiss()
    }

    binding?.editPetName?.addTextChangedListener {
      binding?.editPetName?.error = null
    }

    binding?.editDescription?.addTextChangedListener {
      it?.length?.let { length ->
        if (length >= MINIMUM_DESCRIPTION_LENGTH) binding?.editDescription?.error = null
      }
    }
  }

  private fun getStatusName(): String {
    val status = when (binding?.toggleStatus?.checkedButtonId) {
      binding?.buttonLost?.id -> binding?.buttonLost
      binding?.buttonFound?.id -> binding?.buttonFound
      else -> binding?.buttonLost
    }?.text.toString().trim()

    return status
  }

  private fun submitPost() {
    if (!validateInputs()) return

    binding?.loadingOverlay?.visibility = View.VISIBLE

    // TODO: EXTRACT ALL GET FIELDS TO FUNC!!!
    val petName = binding?.editPetName?.text.toString()
    val petType = binding?.dropdownPetType?.text.toString()
    val breed = binding?.editBreed?.text.toString()
    val description = binding?.editDescription?.text.toString()
    val status = getStatusName()

    val image = binding?.imagePet?.bitmap

    image?.let { image ->
      viewModel.createPost(petName, petType, breed, status, description, image) { errorMessage ->
        binding?.loadingOverlay?.visibility = View.GONE

        if (errorMessage == null) return@createPost dismiss()

        showToast(errorMessage)
      }
    } ?: run {
      showToast("Please upload a pet image")
    }
  }

  private fun validateInputs(): Boolean {
    val petName = binding?.editPetName?.text.toString().trim()
    val petType = binding?.dropdownPetType?.text.toString()
    val description = binding?.editDescription?.text.toString().trim()
    val location = binding?.editLocation?.text.toString().trim()

    if (imageUri == null) {
      showToast("Please add a photo of the pet")

      return false
    }

    if (petType.isEmpty()) {
      showToast("Please select a pet type")

      return false
    }

    if (petName.isEmpty()) {
      binding?.editPetName?.error = "Name is required"

      return false
    }

    if (location.isEmpty() || viewModel.selectedLat == null) {
      binding?.editLocation?.error = "Please select a location from the suggestions"
      showToast("You must select a location from the dropdown list")

      return false
    }


    if (description.length < MINIMUM_DESCRIPTION_LENGTH) {
      binding?.editDescription?.error =
        "Please provide more details (at least $MINIMUM_DESCRIPTION_LENGTH chars)"

      return false
    }

    return true
  }

  private fun showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(requireContext(), message, duration).show()
  }

  private fun dismiss() {
    view?.findNavController()?.popBackStack()
  }

  override fun onDestroyView() {
    super.onDestroyView()

    binding = null
  }
}
