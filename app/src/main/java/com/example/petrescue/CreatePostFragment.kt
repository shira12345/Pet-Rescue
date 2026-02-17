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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.petrescue.databinding.FragmentCreatePostBinding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreatePostFragment : Fragment() {

  private var _binding: FragmentCreatePostBinding? = null
  private val binding get() = _binding!!

  private var imageUri: Uri? = null

  private val viewModel: CreatePostViewModel by viewModels()

  private val LOCATION_IQ_KEY = "pk.f01f2d98d23143fc44b97e085ce7bda0"

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
    setupListeners()

    setupLocationAutocomplete()
  }

  private fun setupLocationAutocomplete() {
    // 1. Observe the results from ViewModel
    viewModel.results.observe(viewLifecycleOwner) { results ->
      val adapter = ArrayAdapter(
        requireContext(),
        android.R.layout.simple_dropdown_item_1line,
        results.map { it.displayName }
      )
      binding.editLocation.setAdapter(adapter)

      // Show dropdown only if we have results
      if (results.isNotEmpty() && binding.editLocation.hasFocus()) {
        binding.editLocation.showDropDown()
      }

      // 2. Handle Item Selection
      binding.editLocation.setOnItemClickListener { _, _, position, _ ->
        val selected = results[position]
        viewModel.selectedLat = selected.lat.toDoubleOrNull()
        viewModel.selectedLon = selected.lon.toDoubleOrNull()

        // Set text and move cursor to end
        binding.editLocation.setText(selected.displayName, false)
      }
    }

    // 3. Listen for text changes
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

  private fun setupListeners() {

    binding.toggleStatus.addOnButtonCheckedListener { group, checkedId, isChecked ->
      if (isChecked) {
        when (checkedId) {
          R.id.buttonLost -> {
            binding.buttonLost.setBackgroundColor(Color.parseColor("#FFEBEE")) // Light Red
            binding.buttonLost.setTextColor(Color.RED)
          }
          R.id.buttonFound -> {
            binding.buttonFound.setBackgroundColor(Color.parseColor("#E8F5E9")) // Light Green
            binding.buttonFound.setTextColor(Color.parseColor("#2E7D32"))
          }
        }
      } else {
        // Reset to default white/gray when unselected
        val btn = group.findViewById<MaterialButton>(checkedId)
        btn.setBackgroundColor(Color.TRANSPARENT)
        btn.setTextColor(Color.GRAY)
      }
    }

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
      if (it!!.length >= 10) binding.editDescription.error = null
    }
  }

  private fun submitPost() {
    // 1. Validation
    if (!validateInputs()) return

    // 2. Show Loading UI
    binding.loadingOverlay.visibility = View.VISIBLE

    // 3. Start Upload (Simulated here)
    lifecycleScope.launch {
      try {
        // This is where your Firebase Storage + Firestore logic goes
        // val result = viewModel.uploadPost(...)

        delay(2000) // Simulating network delay

        // 4. Success!
        binding.loadingOverlay.visibility = View.GONE
        Toast.makeText(requireContext(), "Post created successfully! 🐾", Toast.LENGTH_LONG).show()
        findNavController().popBackStack()

      } catch (e: Exception) {
        binding.loadingOverlay.visibility = View.GONE
        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun validateInputs(): Boolean {
    val petName = binding.editPetName.text.toString().trim()
    val petType = binding.dropdownPetType.text.toString()
    val description = binding.editDescription.text.toString().trim()
    val location = binding.editLocation.text.toString().trim()

    // 1. Check if image is picked
    if (imageUri == null) {
      showError("Please add a photo of the pet")
      return false
    }

    // 2. Check Pet Type (Dropdown)
    if (petType.isEmpty()) {
      showError("Please select a pet type")
      return false
    }

    // 3. Check Pet Name
    if (petName.isEmpty()) {
      binding.editPetName.error = "Name is required"
      return false
    }

    // 4. Check Location Selection (Crucial!)
    // We check if the text is there AND if we have coordinates from the API
    if (location.isEmpty() || viewModel.selectedLat == null) {
      binding.editLocation.error = "Please select a location from the suggestions"
      showError("You must select a location from the dropdown list")
      return false
    }

    // 5. Check Description length
    if (description.length < 10) {
      binding.editDescription.error = "Please provide more details (at least 10 chars)"
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
