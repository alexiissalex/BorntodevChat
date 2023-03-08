package zeegun.borntodev.borntodevchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*
import zeegun.borntodev.borntodevchat.R

class SignUpActivity : AppCompatActivity() {

    // connect firebase
    // connect database
    var mAuth : FirebaseAuth? = null
    var mDatabase: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()

        // setup button
        btn_create_submit.setOnClickListener {
            var display_name = edt_displayname.text.toString().trim()
            var email = edt_email.text.toString().trim()
            var password = edt_password.text.toString().trim()

            createUser(display_name,email,password)

            }
        }

        private fun createUser(display_name:String,email: String, password: String){
            //check user already filled out?
            if(!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                //check firebase id Sign Up success
                mAuth!!.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                        task : Task<AuthResult> ->

                    // เมื่อสำเร็จแล้วให้ทำอะไรต่อ
                    if(task.isSuccessful){
                        sendUserDataToFirebase(display_name)
                    }
                }
            }
        }

        private fun sendUserDataToFirebase(display_name: String){

            //หากสำเร็จตัว Account จะถูก login Auto และ มี id ติดว่า user มี id เป็นอะไร
            var user = mAuth!!.currentUser
            var userID = user!!.uid

            //Referent ที่จะเขียน user ลง Database
            var userRef = mDatabase!!.reference.child("Users").child(userID)
            //เตรียมข้อมูลก่อนส่ง Database
            var userObject = HashMap<String,String>()
            userObject.put("display_name",display_name)
            userObject.put("status","Hello World")
            userObject.put("image","default")
            userObject.put("thumb_image","default")

            //เขียนข้อมูลลง Database
            userRef.setValue(userObject).addOnCompleteListener {
                    task: Task<Void> ->
                if(task.isSuccessful) {
                    //หากเขียนข้อมูลสำเร็จให้แสดง toast ดังนี้
                    Toast.makeText(this, "Create Successful", Toast.LENGTH_LONG).show()
                    //sign up successful and sent userID -> DashboardActivity
                    var intent = Intent(this, DashboardActivity::class.java)
                    intent.putExtra("userID",userID)
                    startActivity(intent)
                    //หน้าเสร็จแล้วจบแล้ว ไม่ต้องส่งข้อมูลกลับมาและจะกดปุ่ม back ไม่ได้ นอกจากออกจากระบบ
                    finish()
                }else
                //หากเขียนไม่สำเร็จให้แสดง toast ดังนี้
                    Toast.makeText(this,"Create Unsuccessful",Toast.LENGTH_LONG).show()
            }

        }
    }