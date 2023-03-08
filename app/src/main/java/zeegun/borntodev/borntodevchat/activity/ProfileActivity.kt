package zeegun.borntodev.borntodevchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import zeegun.borntodev.borntodevchat.R

class ProfileActivity : AppCompatActivity() {

    var mDatabase : FirebaseDatabase? = null
    var mAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        //เปลี่ยนชื่อตรงหัวหน้าต่าง
        supportActionBar!!.title = "Profile"

        //เพิ่มการปุ่ม back กลับ ทางซ้ายมือ
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

        if(intent.extras != null){

            var chatId:String?
            var userId = intent.extras!!.get("userid").toString()
            var myId = mAuth!!.currentUser!!.uid

            mDatabase!!.reference.child("Users").child(userId).addValueEventListener(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var display_name = snapshot!!.child("display_name").value.toString()
                    var image = snapshot!!.child("image").value.toString()
                    var status = snapshot!!.child("status").value.toString()

                    tv_display_name_profile.text = display_name
                    tv_status_profile.text = status
                    if(!image.equals("default")){
                        Picasso.get().load(image).placeholder(R.drawable.ic_man).into(iv_user_image)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    finish()
                }

            })

            btn_send_message_profile.setOnClickListener {

                //check ตัว id ของ userId และ friendId ว่ามีใน firebase ไหม หากไม่มีจะได้สร้างห้องใหม่ หากมีจะดึงห้องเก่า
                var chartRef = mDatabase!!.reference.child("Chat").child(myId).child(userId).child("chat_id")

                chartRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //ข้อมูลที่รับเข้ามา จะดูว่า chat ที่ดึงมามีข้อมูลไหม หากไม่มีจะสร้าง chat id ขึ้นใหม่
                        if(snapshot.exists()){
                            //กรณีมีข้อมูลให้ดึงมาแสดง
                            chatId = snapshot.value.toString()
                        }else{
                            //กรณีไม่มีข้อมูลให้สร้างห้อง chat ขึ้นมา ใส่ .push() เพื่อให้สร้าง id ใหม่ขึ้นมาเรื่อยๆ
                            var messageRef = mDatabase!!.reference.child("Messages").push()

                            //ห้อง chat คุยกันระหว่างใคร
                            var userList = HashMap<String,String>()
                            userList.put("0",myId
                            )
                            userList.put("1",userId)

                            //เรียก user list และใส่ user list ลงไปใน messages
                            messageRef.child("user_list").setValue(userList)

                            //ดึง key ออกมาจาก messageRef
                            chatId = messageRef.key.toString()

                            //นำ key ไปเก็บไว้ที่ผู้ส่ง
                            var userDataRef = mDatabase!!.reference.child("Chat").child(myId).child(userId).child("chat_id")
                            userDataRef.setValue(chatId)

                            //นำ key ไปเก็บไว้ที่ผู้รับ
                            var friendDataRef = mDatabase!!.reference.child("Chat").child(userId).child(myId).child("chat_id")
                            friendDataRef.setValue(chatId)
                        }
                        //ส่ง chat id ไปให้หน้า chat
                        var intent = Intent(this@ProfileActivity,ChatActivity::class.java)
                        intent.putExtra("chatid",chatId)
                        intent.putExtra("friendid",userId)
                        startActivity(intent)

                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }

        }else{
            finish()
        }

    }
}