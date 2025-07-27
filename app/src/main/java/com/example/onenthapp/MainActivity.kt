package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
            val hideOn = setOf(
                R.id.productDetailFragment,
                R.id.plusFragment,
                R.id.statsFragment,
                R.id.chatFragment,
                R.id.mypageFragment
            ).contains(dest.id)

            binding.bottomNavigationView.isVisible = !hideOn
            binding.fabAdd.isVisible = !hideOn
        }

    }
}

