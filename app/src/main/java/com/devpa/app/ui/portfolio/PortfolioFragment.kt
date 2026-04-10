package com.devpa.app.ui.portfolio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devpa.app.R
import com.devpa.app.databinding.FragmentPortfolioBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * PortfolioFragment — replaced by Journey system.
 * Redirects to JourneyDetailFragment on start.
 */
@AndroidEntryPoint
class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
