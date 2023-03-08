package zeegun.borntodev.borntodevchat.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import zeegun.borntodev.borntodevchat.fragment.RecentFragment
import zeegun.borntodev.borntodevchat.fragment.UserFragment

/* โค้ดของ borntodev
//รับ fm เข้ามา เรียกตัว fragmentManager เข้ามา และ return FragmentPagerAdapter กลับไป
class SectionPageAdaptor (fm:FragmentManager) : FragmentPagerAdapter(fm) {
    // override เป็นตัวบอกว่า fragment แต่ละตัวเป็นยังไง มีอะไรบ้าง

    //item ทั้งหมดมีกี่อัน
    override fun getCount(): Int {
        return 2
    }

    //ถ้าให้ postiton แบบ int มา ให้ return เป็นอะไรกลับไปให้
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return UserFragment()
            1 -> return RecentFragment()
        }
        //หาก error ให้ return null กลับไป
        return null!!
    }

    //เซ็ตชื่อให้แต่ละแท็บ อ้างอิงตัวเลขจาก fun getItem
    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return "Users"
            1 -> return "Recent"
        }
        return null!!
    }
}
 */

//coding 2021
class SectionPageAdaptor (fa:FragmentActivity): FragmentStateAdapter(fa){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment  {
        when (position) {
            0 -> return UserFragment()
            1 -> return RecentFragment()
        }
        return null!!
    }


}