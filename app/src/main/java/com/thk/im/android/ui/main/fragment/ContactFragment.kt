package com.thk.im.android.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.thk.im.android.api.DataRepository
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Contact
import com.thk.im.android.databinding.FragmentContactBinding
import com.thk.im.android.ui.base.BaseFragment
import com.thk.im.android.ui.main.fragment.adapter.ContactAdapter

class ContactFragment : BaseFragment() {

    private lateinit var binding: FragmentContactBinding
    private var mode = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mode = if (arguments == null) {
            0
        } else {
            requireArguments().getInt("mode")
        }
        val adapter = ContactAdapter(requireContext(), this, mode)
        binding.rcvContact.layoutManager = LinearLayoutManager(context)
        binding.rcvContact.adapter = adapter
        queryAllContacts()
    }

    private fun queryAllContacts() {
        val subscriber = object :BaseSubscriber<List<Contact>>() {
            override fun onNext(t: List<Contact>?) {
                t?.let {
                    setContactList(it)
                }
            }

            override fun onComplete() {
                super.onComplete()
                removeDispose(this)
            }
        }
        IMCoreManager.contactModule.queryAllContact()
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }

    private fun setContactList(t: List<Contact>) {
        val adapter = binding.rcvContact.adapter as ContactAdapter
        adapter.setContactList(t)
    }

    fun getSelectedIds(): LongArray {
        val adapter = binding.rcvContact.adapter as ContactAdapter
        return adapter.selectedIds.toLongArray()
    }

}