package zeegun.borntodev.borntodevchat.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.bar_customview_chat.view.*
import kotlinx.android.synthetic.main.message_row.view.*
import zeegun.borntodev.borntodevchat.R
import zeegun.borntodev.borntodevchat.model.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        //เปลี่ยนชื่อตรงหัวหน้าต่าง
        supportActionBar!!.title = "Chat"

        //เพิ่มการปุ่ม back กลับ ทางซ้ายมือ
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //สร้าง bar เป็นหน้าผู้ที่แชทด้วย
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        //ปิดแท็บ title
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        mDatabase = FirebaseDatabase.getInstance()

        mAuth = FirebaseAuth.getInstance()
        var userId = mAuth!!.currentUser!!.uid

        var myUser: User? = null
        var friendUser: User? = null

        if(intent.extras != null){
            var chatId = intent.extras!!.get("chatid").toString()
            var frindId = intent.extras!!.get("friendid").toString()


            var linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)

            var query = mDatabase!!.reference.child("Messages").child(chatId).child("data")

            var option = FirebaseRecyclerOptions.Builder<Message>().setQuery(query, Message::class.java).setLifecycleOwner(this).build()

            var inflater = this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            //create adapter
            var adapter = object : FirebaseRecyclerAdapter<Message, MessageHolder>(option){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
                    //บอกว่าจะใช้ row จากไหน
                    return MessageHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_row,parent,false))
                }

                override fun onBindViewHolder(
                    holder: MessageHolder,
                    position: Int,
                    model: Message
                ) {
                    if(model.message != null){
                        var message = model.message
                        var senderName:String?
                        var image:String?

                        //หากเป็นตัวเองคนส่ง message อยู่ฝั่งซ้าย หากคนส่งเป็นผู้อื่นอยู่ฝั่งขวา
                            if(model.sender == userId){
                                senderName = myUser!!.display_name
                                image = myUser!!.image

                                //หากอยู่ฝั่งขวาให้ปิดฝั่งซ้าย
                                holder.customview.tv_name_message_row_left.visibility = View.GONE
                                holder.customview.tv_message_row_left.visibility = View.GONE
                                holder.customview.iv_image_message_row_left.visibility = View.GONE

                                //เปิดฝั่งขวาให้หมด
                                holder.customview.tv_name_message_row_right.visibility = View.VISIBLE
                                holder.customview.tv_message_row_right.visibility = View.VISIBLE
                                holder.customview.iv_image_message_row_right.visibility = View.VISIBLE

                            }else{
                                senderName = friendUser!!.display_name
                                image = friendUser!!.image
                            }
                        holder.bind(message!!,senderName!!,image!!)
                    }
                }

            }
            //ดึงข้อมูล userid and frindid
            var myRef = mDatabase!!.reference.child("Users").child(userId)
            myRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        //หากมีข้อมุลจะให้ getValue ออกมา
                        myUser = snapshot.getValue(User::class.java)

                        var friendRef = mDatabase!!.reference.child("Users").child(frindId)
                        friendRef.addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                friendUser = snapshot.getValue(User::class.java)

                                //ตัวเรียกใช้
                                rv_chat.layoutManager = linearLayoutManager
                                rv_chat.adapter = adapter

                                //เมื่อดึงข้อมูลมาแล้วจะให้ set ลงใน action bar
                                var actionBarView = inflater.inflate(R.layout.bar_customview_chat,null)
                                actionBarView.tv_bar_name.text = friendUser!!.display_name
                                Picasso.get().load(friendUser!!.thumb_image).placeholder(R.drawable.ic_man).into(actionBarView.iv_bar_image)

                                //set เข้าไปใน custom actionbar
                                supportActionBar!!.customView = actionBarView
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            btn_send_chat.setOnClickListener {
                //ดึงข้อความออกมาจากกล่อง
                var text = et_message_chat.text.toString().trim()

                //กดส่งแล้วข้อความหาย (เคลียร์ช่องแชท)
                et_message_chat.setText("")

                //หาก text เป็นช่องว่าง หรือไม่มีอะไรกำหนดให้ไม่ทำงาน
                if(!TextUtils.isEmpty(text)){
                    //หากไม่ empty ให้ส่งไปที่ firebase
                    var messageRef = mDatabase!!.reference.child("Messages").child(chatId).child("data").push()
                    //สร้าง message
                    var message = Message(userId,text)
                    //ส่ง message ขึ้น firebase
                    messageRef.setValue(message)

                    //สร้าง recent ส่งขึ้น firebase
                    var dateFormat = SimpleDateFormat("yyMMddHHmmssSSS")
                    var date = dateFormat.format(Date())

                    var myRecentRef = mDatabase!!.reference.child("Chat").child(userId).child(frindId).child("Recent")
                    var friendRecentRef = mDatabase!!.reference.child("Chat").child(frindId).child(userId).child("Recent")

                    var recentChat = RecentChat(date,message.message!!)

                    myRecentRef.setValue(recentChat)
                    friendRecentRef.setValue(recentChat)
                }

            }
        }

        }
    }