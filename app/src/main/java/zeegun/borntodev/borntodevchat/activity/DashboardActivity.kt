package zeegun.borntodev.borntodevchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*
import zeegun.borntodev.borntodevchat.R
import zeegun.borntodev.borntodevchat.adapter.SectionPageAdaptor

class DashboardActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        mAuth = FirebaseAuth.getInstance()

        //เปลี่ยนชื่อด้านบนเป็นอย่างอื่น
        supportActionBar!!.title = "DashBoard"
/*

        //เรียกใช้ adaptor

        //สร้าง adaptor
        var sectionAdaptor = SectionPageAdaptor(supportFragmentManager)
        //เซ็ต adapter
        vp_dashboard.adapter = sectionAdaptor

        tl_dashboard.setupWithViewPager(vp_dashboard)

 */
        //code ปี2021 เรียกใช้ adaptor
        val tabLayout = findViewById<TabLayout>(R.id.tl_dashboard)
        val viewPager2 = findViewById<ViewPager2>(R.id.vp_dashboard)

        viewPager2.adapter = SectionPageAdaptor(this)

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Users"
                }
                1 -> {
                    tab.text = "Recent"
                }
            }
        }.attach()
    }
    /*
        //จะเอาตัว menu มาใช้ โดยบอกตัว Activity ว่าจะใช้ menu ในการบอกจะบอกให้สร้าง โดยใช้ override ตัว function
        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            super.onCreateOptionsMenu(menu)
            menuInflater.inflate(R.menu.main_menu, menu)
            //โดยให้เรียกใช้ main_menu ที่สร้างไว้
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            super.onOptionsItemSelected(item)

            if(item != null){
                if(item.itemId == R.id.menu_setting){
                    //หาก menu == setting ให้ส่งค่าข้ามมาที่หน้า setting
                    val intent = Intent(this,SettingActivity::class.java)
                    startActivity(intent)
                }
                if(item.itemId == R.id.menu_logout){
                    mAuth!!.signOut()
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            return true
        }
     */
    //coding 2021
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menu_setting -> {
                val intent = Intent(this,SettingActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_logout -> {
                mAuth!!.signOut()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    }