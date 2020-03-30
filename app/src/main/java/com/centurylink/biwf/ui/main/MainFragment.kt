package com.centurylink.biwf.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders.of
import com.centurylink.biwf.ui.viewmodel.factory.DaggerViewModelFactory
import com.centurylink.biwf.R
import com.centurylink.biwf.ui.fragment.BaseFragment
import com.centurylink.biwf.ui.viewmodel.MainViewModel
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class MainFragment : BaseFragment() {


    @Inject
    lateinit var factory: DaggerViewModelFactory

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }
}