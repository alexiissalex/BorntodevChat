package zeegun.borntodev.borntodevchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import zeegun.borntodev.borntodevchat.R

class MainActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null
    //เอาไว้ใช้ในการดูว่า user มีการ login อยู่หรือไม่ หรือเปลี่ยน user หรือ logout แล้วหรือไม่
    var mAuthListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //click button activity_main connect activity_login
        btn_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        //click button activity_main connect activity_signup
        btn_signup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        mAuth = FirebaseAuth.getInstance()

        //เป็นแค่การสร้าง Listener ยังไม่ได้add mAuth
        mAuthListener = FirebaseAuth.AuthStateListener {
            //ส่งค่าในการ login ต่างๆ กลับมา
            firebaseAuth: FirebaseAuth ->
            var user = firebaseAuth.currentUser

            //ดูว่ามีการ login แล้วหรือยัง แล้วให้ส่งไปที่หน้า DashboardActivity
            if(user != null){
                var intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this,"Please Login",Toast.LENGTH_LONG).show()
            }
        }

    }
    //นำตัว listener มาใส่ใน mAuth
    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()

        mAuth!!.removeAuthStateListener(mAuthListener!!)
    }
}