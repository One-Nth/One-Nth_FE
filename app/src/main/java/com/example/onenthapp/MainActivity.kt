package com.example.onenthapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.onenthapp.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NavController 연결
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

        // FAB 클릭시 팝업 띄우기
        binding.fabAdd.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("게시글 작성")
                .setMessage("여기에 게시글 작성 UI를 팝업 형태로 넣으세요")
                .setPositiveButton("작성") { _, _ ->
                    // TODO: 실제 작성 로직 또는 Activity/Fragment 호출
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }
}

