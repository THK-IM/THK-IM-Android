package com.thk.im.android.ui.panel.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.thk.im.android.ui.databinding.FragmentComponentPanelBinding
import com.thk.im.android.ui.panel.PanelFragment
import com.thk.im.android.ui.panel.component.internal.ComponentAdapter
import com.thk.im.android.ui.panel.component.internal.UIComponentManager

class ComponentPanelFragment(private val uiComponentManager: UIComponentManager) : PanelFragment() {

    companion object {
        private const val KEY_POSITION = "key_position"
        fun getInstance(
            position: Int,
            uiComponentManager: UIComponentManager
        ): ComponentPanelFragment {
            val componentPanelFragment = ComponentPanelFragment(uiComponentManager)
            val bundle = Bundle()
            bundle.putInt(KEY_POSITION, position)
            componentPanelFragment.arguments = bundle
            return componentPanelFragment
        }
    }

    private var _binding: FragmentComponentPanelBinding? = null
    private val binding get() = _binding!!


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentPanelBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.setBackgroundColor(uiComponentManager.panelColor)
        initAdapter()
    }

    private fun initAdapter() {
        val adapter = ComponentAdapter(uiComponentManager)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recyclerView.adapter = adapter
    }
}