package zeegun.borntodev.borntodevchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import zeegun.borntodev.borntodevchat.R

class LoginActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        btn_login_submit.setOnClickListener {
            var email = edt_email_login.text.toString().trim()
            var password = edt_password_login.text.toString().trim()

            loginWithEmailPassword(email,password)
        }
    }

    private fun loginWithEmailPassword(email:String,password:String){
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG).show()
                        //sign up successful and sent userID -> DashboardActivity
                        var intent = Intent(this, DashboardActivity::class.java)
                        intent.putExtra("userID",mAuth!!.currentUser!!.uid)
                        startActivity(intent)
                        //หน้าเสร็จแล้วจบแล้ว ไม่ต้องส่งข้อมูลกลับมาและจะกดปุ่ม back ไม่ได้ นอกจากออกจากระบบ
                        finish()
                    } else {
                        Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this, "Please check Your email and password", Toast.LENGTH_LONG).show()
        }
    }
}