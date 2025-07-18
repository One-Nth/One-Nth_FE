package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.onenthapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.plusFragment -> {
                    false
                }

                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
            }
        }

        // FAB 클릭시 상품 등록 화면으로 이동
        binding.fabAdd.setOnClickListener {
            Log.d("MainActivity", "FAB clicked!")
            navController.navigate(R.id.plusFragment)
        }
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val hideOn = dest.id == R.id.plusFragment
            binding.bottomNavigationView.visibility = if (hideOn) View.GONE else View.VISIBLE
            binding.fabAdd.visibility            = if (hideOn) View.GONE else View.VISIBLE
        }

    }
}

