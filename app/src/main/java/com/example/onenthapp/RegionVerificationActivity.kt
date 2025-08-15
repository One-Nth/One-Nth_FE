package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.userset.UserSetRepository
import kotlinx.coroutines.launch

class RegionVerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_region_verification)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }
    }
}
