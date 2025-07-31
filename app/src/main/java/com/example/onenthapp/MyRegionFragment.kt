package com.example.onenthapp.ui.region

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.model.Region
import com.google.android.material.chip.Chip

class MyRegionFragment : Fragment() {
//
//    private var _binding: FragmentRegionChangeBinding? = null
//    private val binding get() = _binding!!
//    private val vm: MyRegionViewModel by viewModels()
//
//    // 제안용 어댑터
//    private lateinit var suggestionAdapter: RegionSearchAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentRegionChangeBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        // 1) 칩 영역 초기화
//        vm.selectedRegions.observe(viewLifecycleOwner) { list ->
//            renderChips(list)
//        }
//
//        // 2) 제안 리스트 초기화
//        suggestionAdapter = RegionSearchAdapter(emptyList()) { region ->
//            // 클릭 시: 이름(○○동)만 잘라서 추가
//            val dongName = region.name.takeLast( region.name.indexOf("동") + 1 )
//            vm.addRegion( Region(region.id, dongName, region.latitude, region.longitude) )
//            hideSuggestions()
//            binding.etSearch.text?.clear()
//        }
//        binding.rvSuggestions.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = suggestionAdapter
//        }
//
//        // 3) 검색창 이벤트
//        binding.searchBarEt.addTextChangedListener { text ->
//            val q = text.toString().trim()
//            if (q.isEmpty()) {
//                hideSuggestions()
//            } else {
//                // 검색 → 최대 3개
//                val results = vm.repo.searchRegions(q).take(3)
//                suggestionAdapter.updateList(results)
//                binding.rvSuggestions.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
//            }
//        }
//
//        // 툴바 뒤로가기
//        binding.includeToolbar.btnBack.setOnClickListener {
//            requireActivity().onBackPressed()
//        }
//    }
//
//    private fun renderChips(list: List<Region>) {
//        binding.flexSelected.removeAllViews()
//        list.forEach { region ->
//            val chip = Chip(requireContext()).apply {
//                text = region.name
//                isCloseIconVisible = true
//                setOnCloseIconClickListener {
//                    vm.removeRegion(region)
//                }
//            }
//            binding.flexSelected.addView(chip)
//        }
//    }
//
//    private fun hideSuggestions() {
//        binding.rvSuggestions.visibility = View.GONE
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}
