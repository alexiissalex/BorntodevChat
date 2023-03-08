package zeegun.borntodev.borntodevchat.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_status.*
import zeegun.borntodev.borntodevchat.R

class StatusActivity : AppCompatActivity() {
    //set ปุ่ม ส่งไปที่ database
    var mDatabase: FirebaseDatabase? = null
    var mCurrentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        //ดึง intent มาแสดงผล
        if (intent.extras != null){
            var status = intent.extras!!.get("status").toString()

            //นำ statusเก่า ไปไว้ที่ edittext
            edt_status.setText(status)
        }

        //เมื่อกดปุ่มส่ง status แล้วจะให้ทำอะไร
        btn_status_submit.setOnClickListener {
            //ดึงค่าออกมาจาก edittext ก่อนว่าพิมพ์อะไร
            var updateStatus = edt_status.text.toString().trim()
            //เขียนขึ้นไปบน firebase โดยต้องรู้ว่าคนนั้นคือ id อะไร
            mCurrentUser = FirebaseAuth.getInstance().currentUser
            //หา user id จาก mCurrentUser
            var uid = mCurrentUser!!.uid

            //สร้างตัว database
            mDatabase = FirebaseDatabase.getInstance()
            var statusRef = mDatabase!!.reference.child("Users").child(uid).child("status")
            //ทำการ update status
            statusRef.setValue(updateStatus).addOnCompleteListener {
                //เช็คว่าผ่านหรือไม่ผ่าน
                task: Task<Void> ->
                if(task.isSuccessful){
                    Toast.makeText(this,"Update Status Successful.",Toast.LENGTH_LONG).show()
                    finish() // หน้านี้ไม่ต้องเรียกต่อ มันถูกเรียกมาจากหน้า setting หากทำงานเสร็จจะกลับไปหน้า setting เอง
                    //ถ้าเป็น Intent การส่งหน้าไปที่ Setting เวลา back จะกลับมาที่หน้า StatusActivity(update Status)
                }else{
                    Toast.makeText(this,"Update Status Error.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}