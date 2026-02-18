package com.example.petrescue

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.petrescue.databinding.FragmentCreatePostBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val MINIMUM_DESCRIPTION_LENGTH = 10

class CreatePostFragment : Fragment() {
  private var _binding: FragmentCreatePostBinding? = null
  private val binding get() = _binding!!

  private var imageUri: Uri? = null

  private val viewModel: CreatePostViewModel by viewModels()

  private val pickImage =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      uri?.let {
        imageUri = it
        binding.imagePet.setImageURI(it)
      }
    }

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

    setupPetTypeDropdown()
    setupStatusToggle()
    setupListeners()

    setupLocationAutocomplete()
  }

  private fun getFirstThreeWords(string: String): String {
    return string.split(",").take(3).joinToString(",").trim()
  }

  private fun setupLocationAutocomplete() {
    viewModel.results.observe(viewLifecycleOwner) { results ->
      val adapter = ArrayAdapter(
        requireContext(),
        android.R.layout.simple_dropdown_item_1line,
        results.map { getFirstThreeWords(it.displayName) }
      )

      binding.editLocation.setAdapter(adapter)

      if (results.isNotEmpty() && binding.editLocation.hasFocus())
        binding.editLocation.showDropDown()

      binding.editLocation.setOnItemClickListener { _, _, position, _ ->
        val selected = results[position]
        viewModel.selectedLat = selected.lat.toDoubleOrNull()
        viewModel.selectedLon = selected.lon.toDoubleOrNull()

        // Set text and move cursor to end
        binding.editLocation.setText(selected.displayName, false)
      }
    }

    binding.editLocation.addTextChangedListener { text ->
      // Only search if the user is typing (not if we just set the text programmatically)
      if (binding.editLocation.isPerformingCompletion) return@addTextChangedListener

      viewModel.searchLocation(text.toString(), BuildConfig.LOCATION_IQ_KEY)
    }
  }

  private fun setupPetTypeDropdown() {
    val types = listOf("Dog", "Cat", "Bird", "Other")

    val adapter = ArrayAdapter(
      requireContext(),
      android.R.layout.simple_dropdown_item_1line,
      types
    )

    binding.dropdownPetType.setAdapter(adapter)
  }

  private fun updateSelectedStatusButton(checkedButtonId: Int) {
    when (checkedButtonId) {
      R.id.buttonLost -> {
        binding.buttonLost.setBackgroundColor("#FFEBEE".toColorInt()) // Light Red
        binding.buttonLost.setTextColor(Color.RED)
      }

      R.id.buttonFound -> {
        binding.buttonFound.setBackgroundColor("#E8F5E9".toColorInt()) // Light Green
        binding.buttonFound.setTextColor("#2E7D32".toColorInt())
      }
    }
  }

  private fun setupStatusToggle() {
    updateSelectedStatusButton(binding.toggleStatus.checkedButtonId)

    binding.toggleStatus.addOnButtonCheckedListener { group, checkedId, isChecked ->
      if (isChecked) return@addOnButtonCheckedListener updateSelectedStatusButton(checkedId)

      // Reset to default white/gray when unselected
      val button = group.findViewById<MaterialButton>(checkedId)

      button.setBackgroundColor(Color.TRANSPARENT)
      button.setTextColor(Color.GRAY)
    }
  }

  private fun setupListeners() {
    binding.cardPhoto.setOnClickListener {
      pickImage.launch("image/*")
    }

    binding.buttonSubmit.setOnClickListener {
      submitPost()
    }

    binding.buttonCancel.setOnClickListener {
      findNavController().popBackStack()
    }

    binding.editPetName.addTextChangedListener {
      binding.editPetName.error = null
    }

    binding.editDescription.addTextChangedListener {
      it?.length?.let { length ->
        if (length >= MINIMUM_DESCRIPTION_LENGTH) binding.editDescription.error = null
      }
    }
  }

  private fun getStatusName(): String {
    val status = when (binding.toggleStatus.checkedButtonId) {
      binding.buttonLost.id -> binding.buttonLost
      binding.buttonFound.id -> binding.buttonFound
      else -> binding.buttonLost
    }.text.toString().trim()

    return status
  }

  private fun submitPost() {
    if (!validateInputs()) return

    binding.loadingOverlay.visibility = View.VISIBLE

    // 3. Start Upload (Simulated here)
    lifecycleScope.launch {
      try {
        // This is where your Firebase Storage + Firestore logic goes
        // val result = viewModel.uploadPost(...)

        val status = getStatusName()

        delay(2000) // Simulating network delay

        // 4. Success!
        binding.loadingOverlay.visibility = View.GONE
        Toast.makeText(requireContext(), "Post created successfully! 🐾", Toast.LENGTH_LONG).show()
        findNavController().popBackStack()
      } catch (exception: Exception) {
        binding.loadingOverlay.visibility = View.GONE

        showError("Error: ${exception.message}")
      }
    }
  }

  private fun validateInputs(): Boolean {
    val petName = binding.editPetName.text.toString().trim()
    val petType = binding.dropdownPetType.text.toString()
    val description = binding.editDescription.text.toString().trim()
    val location = binding.editLocation.text.toString().trim()

    if (imageUri == null) {
      showError("Please add a photo of the pet")

      return false
    }

    if (petType.isEmpty()) {
      showError("Please select a pet type")

      return false
    }

    if (petName.isEmpty()) {
      binding.editPetName.error = "Name is required"

      return false
    }

    if (location.isEmpty() || viewModel.selectedLat == null) {
      binding.editLocation.error = "Please select a location from the suggestions"
      showError("You must select a location from the dropdown list")

      return false
    }


    if (description.length < MINIMUM_DESCRIPTION_LENGTH) {
      binding.editDescription.error =
        "Please provide more details (at least $MINIMUM_DESCRIPTION_LENGTH chars)"

      return false
    }

    return true
  }

  private fun showError(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
