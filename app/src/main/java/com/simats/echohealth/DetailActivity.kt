package com.simats.echohealth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.simats.echohealth.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

	private lateinit var binding: ActivityDetailBinding

	companion object {
		private const val TAG = "DetailActivity"
		const val EXTRA_TITLE = "title"
		const val EXTRA_CONTENT = "content"
		const val EXTRA_IMAGE_RES_ID = "imageResId"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Log.d(TAG, "DetailActivity onCreate started")

		binding = ActivityDetailBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setupBackNavigation()
		loadContent()

		Log.d(TAG, "DetailActivity onCreate completed successfully")
	}

	private fun setupBackNavigation() {
		binding.backArrow.setOnClickListener {
			Log.d(TAG, "Back arrow clicked - finishing activity")
			finish()
		}
	}

	private fun loadContent() {
		Log.d(TAG, "Loading content from intent extras")

		val title = intent.getStringExtra(EXTRA_TITLE) ?: "Learn Details"
		val content = intent.getStringExtra(EXTRA_CONTENT) ?: "Content not available"
		val imageResId = intent.getIntExtra(EXTRA_IMAGE_RES_ID, R.drawable.logo)

		Log.d(TAG, "Received data - Title: $title, Content length: ${content.length}, Image ResId: $imageResId")

		binding.titleText.text = title
		binding.contentText.text = content
		try {
			binding.headerImage.setImageResource(imageResId)
		} catch (e: Exception) {
			Log.e(TAG, "Error setting header image: ${e.message}")
			binding.headerImage.setImageResource(R.drawable.logo)
		}
	}

	override fun onResume() {
		super.onResume()
		Log.d(TAG, "DetailActivity onResume called")
	}

	override fun onPause() {
		super.onPause()
		Log.d(TAG, "DetailActivity onPause called")
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "DetailActivity onDestroy called")
	}
}
