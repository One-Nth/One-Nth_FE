package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class MypageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        // menuList는 fragment_mypage.xml에 정의된 LinearLayout
        val menuList = view.findViewById<LinearLayout>(R.id.menuList)

        val menuItems = listOf(
            Pair(R.drawable.stash_save_ribbon, "스크랩한 글"),
            Pair(R.drawable.solar_heart_linear, "공감한 글"),
            Pair(R.drawable.icon_park_outline_write, "내가 쓴 글"),
            Pair(R.drawable.mdi_paper_outline, "나의 거래 내역 및 후기"),
            Pair(R.drawable.material_symbols_info_outline, "약관 및 정책"),
            Pair(R.drawable.material_symbols_how_to_reg_outline, "고객센터")
        )

        // 메뉴 동적으로 추가
        for ((iconRes, text) in menuItems) {
            val itemView = layoutInflater.inflate(R.layout.mypage_item_menu, menuList, false)

            val icon = itemView.findViewById<ImageView>(R.id.menuIcon)
            val label = itemView.findViewById<TextView>(R.id.menuText)

            icon.setImageResource(iconRes)
            label.text = text

            // 클릭 이벤트 예시 (필요시)
            itemView.setOnClickListener {
                // TODO: 클릭 시 액션 (예: Toast, 이동 등)
            }

            menuList.addView(itemView)
        }

        val settingsButton = view.findViewById<ImageView>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }


        return view
    }
}
