package com.dinhtai.fchat.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.dinhtai.fchat.utils.showToast
import com.dinhtai.fchat.utils.toast
import java.lang.IllegalStateException

abstract class BindingFragment<T : ViewDataBinding> : Fragment() {
    @LayoutRes
    abstract fun getLayoutResId(): Int

    abstract val viewModel: RxViewModel?

    private var _binding: T? = null

    protected val binding: T
        get() = _binding ?: throw IllegalStateException(EXCEPTION)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        DataBindingUtil
            .inflate<T>(inflater, getLayoutResId(), container, false)
            .apply { _binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel?.errorException?.observe(viewLifecycleOwner, Observer(::showToast))
        setupView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun showToast(msg: String) = requireContext().showToast(msg)

    abstract fun setupView()
    companion object{
        private const val EXCEPTION = "Binding only is valid after onCreateView"
    }
}
