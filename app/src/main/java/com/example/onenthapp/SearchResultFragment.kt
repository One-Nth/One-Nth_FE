package com.example.onenthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.databinding.FragmentSearchResultBinding
import com.example.onenthapp.databinding.ItemSearchResultBinding
import com.example.onenthapp.model.SearchResult
import com.example.onenthapp.model.SearchType
import com.google.android.material.tabs.TabLayout

class SearchResultFragment : Fragment(R.layout.fragment_search_result) {
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var fullList: List<SearchResult>
    private lateinit var adapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // RecyclerView 세팅
        adapter = SearchAdapter { result ->
            (parentFragment as? HomeFragment)?.showMidPreview(result)
        }
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@SearchResultFragment.adapter
        }
        // 2) 번들에서 전달된 전체 리스트 추출
        val fullList = arguments
            ?.getParcelableArrayList<SearchResult>("results")
            .orEmpty()
            .takeIf { it.isNotEmpty() }
            ?: List(20) { i ->
                SearchResult(
                    id = "$i",
                    title = "Item $i",
                    price = (i+1)*1000,
                    unit = "개",
                    category = "테스트",
                    imageUrls = emptyList(),
                    isBookmarked = false,
                    type = if (i%2==0) SearchType.BUY else SearchType.SHARE
                )
            }
        adapter.submitList(fullList)

        // 3) “같이 사요/함께 나눠요” 탭 리스너
        binding.tabLayoutHome.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val filtered = when(tab.position) {
                    0 -> fullList.filter { it.type == SearchType.BUY }
                    else -> fullList.filter { it.type == SearchType.SHARE }
                }
                adapter.submitList(filtered)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) {
                onTabSelected(tab)
            }
        })

        binding.btnCloseExpanded.setOnClickListener {
            (parentFragment as? HomeFragment)?.collapseSheet()
        }
        binding.btnBackExpanded.setOnClickListener {
            (parentFragment as? HomeFragment)?.halfExpandBottomSheet()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        fun newInstance(results: List<SearchResult>) = SearchResultFragment().apply {
            arguments = bundleOf("results" to ArrayList(results))
        }
    }
}