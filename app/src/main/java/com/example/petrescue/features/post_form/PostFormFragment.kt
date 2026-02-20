package com.example.petrescue.features.post_form

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
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.petrescue.databinding.FragmentPostFormBinding
import com.example.petrescue.model.Post
import com.example.petrescue.utilis.extensions.bitmap
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

const val MINIMUM_DESCRIPTION_LENGTH = 10

class PostFormFragment : Fragment() {
  private var _binding: FragmentPostFormBinding? = null
  private val binding get() = _binding!!
  private var imageUri: Uri? = null

  private val viewModel: PostFormViewModel by viewModels()
  private val args: PostFormFragmentArgs by navArgs()

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
  ): View? {
    _binding = FragmentPostFormBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupPetTypeDropdown()
    setupStatusToggle()
    setupListeners()

    setupLocationAutocomplete()
    setupLoadingOverlay()

    val postToUpdate = args.post

    if (postToUpdate != null) lifecycleScope.launch { fillForm(postToUpdate) }
  }

  private suspend fun fillForm(post: Post) {
    binding.postFormTitle.text = "Edit Rescue Post"
    binding.loadingOverlayTextView.text = "Updating your post..."
    binding.buttonSubmit.text = "Update Post"

    binding.editPetName.setText(post.petName)
    binding.editBreed.setText(post.breed)
    binding.editDescription.setText(post.description)
    binding.dropdownPetType.setText(post.petType, false)

    viewModel.selectedLat = post.latitude
    viewModel.selectedLon = post.longitude
    binding.editLocation.setText(
      viewModel.getAddressFromPostLocation(post.latitude, post.longitude),
      false

    )

    val statusButtonId =
      if (post.status == "Found") binding.buttonFound.id else binding.buttonLost.id
    updateSelectedStatusButton(statusButtonId)

    Picasso.get().load(post.imageUri).into(binding.imagePet)
    imageUri = post.imageUri?.toUri()
  }

  private fun setupLoadingOverlay() {
    viewModel.postFormState.observe(viewLifecycleOwner) { state ->
      when (state) {
        is PostFormState.Loading -> {
          binding.loadingOverlay.visibility = View.VISIBLE
        }

        is PostFormState.Success -> {
          binding.loadingOverlay.visibility = View.GONE
          dismiss()
        }

        is PostFormState.Error -> {
          binding.loadingOverlay.visibility = View.GONE
          showToast(state.message)
        }

        else -> {}
      }
    }
  }

  private fun getFirstThreeWords(string: String): String {
    return string.split(",").take(3).joinToString(",").trim()
  }

  private fun setupLocationAutocomplete() {
    viewModel.results.observe(viewLifecycleOwner) { locations ->
      val adapter = ArrayAdapter(
        requireContext(),
        R.layout.simple_dropdown_item_1line,
        locations.map { getFirstThreeWords(it.displayName) }
      )

      binding.editLocation.setAdapter(adapter)

      if (locations.isNotEmpty() && binding.editLocation.hasFocus())
        binding.editLocation.showDropDown()
    }

    binding.editLocation.setOnItemClickListener { _, _, position, _ ->
      val selectedLocation = viewModel.results.value?.get(position)

      selectedLocation?.let {
        viewModel.selectedLat = it.lat.toDoubleOrNull()
        viewModel.selectedLon = it.lon.toDoubleOrNull()

        binding.editLocation.setText(it.displayName, false)
      }
    }

    binding.editLocation.addTextChangedListener { text ->
      val query = text.toString()

      if (!binding.editLocation.isPerformingCompletion) viewModel.searchLocation(query)
    }
  }

  private fun setupPetTypeDropdown() {
    val types = listOf("Dog", "Cat", "Bird", "Other")

    val adapter = ArrayAdapter(
      requireContext(),
      R.layout.simple_dropdown_item_1line,
      types
    )

    binding.dropdownPetType.setAdapter(adapter)
  }

  private fun updateSelectedStatusButton(checkedButtonId: Int?) {
    when (checkedButtonId) {
      com.example.petrescue.R.id.buttonLost -> {
        binding.buttonLost.setBackgroundColor("#FFEBEE".toColorInt()) // Light Red
        binding.buttonLost.setTextColor(Color.RED)
      }

      com.example.petrescue.R.id.buttonFound -> {
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
      dismiss()
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

    // TODO: EXTRACT ALL GET FIELDS TO FUNC!!!
    val petName = binding.editPetName.text.toString()
    val petType = binding.dropdownPetType.text.toString()
    val breed = binding.editBreed.text.toString()
    val description = binding.editDescription.text.toString()
    val status = getStatusName()

    val imageBitmap = binding.imagePet.bitmap

    val existingPost = args.post

    if (existingPost == null) {
      imageBitmap?.let { imageBitmap ->
        viewModel.createPost(petName, petType, breed, status, description, imageBitmap)
      } ?: run {
        showToast("Please upload a pet image")
      }
    } else {
      viewModel.updatePost(
        existingPost.id,
        petName,
        petType,
        breed,
        status,
        description,
        imageBitmap,
        existingPost.imageUri
      )
    }
  }

  private fun validateInputs(): Boolean {
    val petName = binding.editPetName.text.toString().trim()
    val petType = binding.dropdownPetType.text.toString()
    val description = binding.editDescription.text.toString().trim()
    val location = binding.editLocation.text.toString().trim()

    if (imageUri == null) {
      showToast("Please add a photo of the pet")

      return false
    }

    if (petType.isEmpty()) {
      showToast("Please select a pet type")

      return false
    }

    if (petName.isEmpty()) {
      binding.editPetName.error = "Name is required"

      return false
    }

    if (location.isEmpty() || viewModel.selectedLat == null) {
      binding.editLocation.error = "Please select a location from the suggestions"
      showToast("You must select a location from the dropdown list")

      return false
    }


    if (description.length < MINIMUM_DESCRIPTION_LENGTH) {
      binding.editDescription.error =
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

    _binding = null
  }
}
