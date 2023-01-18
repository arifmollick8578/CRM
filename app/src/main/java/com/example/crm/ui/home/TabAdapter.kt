package com.example.crm.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.crm.DraftFragment
import com.example.crm.pending.PendingFragment


class TabAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        if (position == 0) {
            fragment = PendingFragment()
        } else if (position == 1) {
            fragment = DraftFragment()
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: String? = null
        if (position == 0) {
            title = "Tab-1"
        } else if (position == 1) {
            title = "Tab-2"
        }
        return title
    }
}