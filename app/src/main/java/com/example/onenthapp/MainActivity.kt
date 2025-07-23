package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.onenthapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)
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
            navController.navigate(R.id.plusFragment)
        }
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val hideOn = dest.id == R.id.plusFragment
            val hideOn2 = dest.id == R.id.statsFragment
            val hideOn3 = dest.id == R.id.chatFragment
            val hideOn4 = dest.id == R.id.mypageFragment
            binding.bottomNavigationView.visibility = if (hideOn || hideOn2 || hideOn3 || hideOn4) View.GONE else View.VISIBLE
            binding.fabAdd.visibility = if (hideOn || hideOn2 || hideOn3 || hideOn4) View.GONE else View.VISIBLE
        }

    }
}

