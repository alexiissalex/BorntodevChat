package zeegun.borntodev.borntodevchat.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import kotlinx.android.synthetic.main.fragment_user.*
import zeegun.borntodev.borntodevchat.R
import zeegun.borntodev.borntodevchat.activity.ChatActivity
import zeegun.borntodev.borntodevchat.activity.ProfileActivity
import zeegun.borntodev.borntodevchat.model.User
import zeegun.borntodev.borntodevchat.model.UserHolder

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class UserFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    var mDatabase: FirebaseDatabase? = null
    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //ต้องสร้าง option ที่เอาไว้ใช้ใน firebase ui
    //และสร้างตัว adepter ที่สร้างจาก firebase ui เพื่อที่จะให้เอาไว้ให้ recyclerview ใช้
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

        //ใช้จัดการการเรียงของข้อมูล user ในหน้า dashboard user
        var linearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)

        //ต้องสร้าง query จะทำการดึง query ต่อกับ firebase
        //query สามารถจำกัดจำนวนของการ query มาได้ และเรียงลำดับได้ด้วย คือ .orderByChild
        var query = mDatabase!!.reference.child("Users").orderByChild("display_name")

        //สร้าง option
        var option = FirebaseRecyclerOptions.Builder<User>().setQuery(query,User::class.java).setLifecycleOwner(this).build()
        //นำ option ไปใช้ใน recycler โดยนำไปใส่ใน adepter
        //แต่ก่อนที่จะสร้าง adepter ต้องสร้างคลาส ViewHolder มีหน้าที่ในการ biding ตัว data กับ text box ต่างๆ เข้าด้วยกัน

        var adapter = object : FirebaseRecyclerAdapter<User,UserHolder>(option){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
                //กำหนดให้ว่าตัว roll ที่ใช้ xml ตัวไหน
                return UserHolder(LayoutInflater.from(parent.context).inflate(R.layout.users_row,parent,false))
            }


            override fun onBindViewHolder(holder: UserHolder, position: Int, model: User) {
                holder.bind(model)

                //ส่ง id ให้ เพื่อให้รู้ว่าคลิกไปที่ใคร
                var friendId = getRef(position).key.toString()
                //สร้าง userid ของผู้ใช้
                var userId = mAuth!!.currentUser!!.uid
                //สร้าง chat id
                var chatId:String? = null

                //check ตัว id ของ userId และ friendId ว่ามีใน firebase ไหม หากไม่มีจะได้สร้างห้องใหม่ หากมีจะดึงห้องเก่า
                var chartRef = mDatabase!!.reference.child("Chat").child(userId).child(friendId).child("chat_id")


                //Set onclick listener ให้แสดง option ขึ้นมาว่าจะดู profile หรือ chat
                holder.itemView.setOnClickListener {
                    //แสดง option
                    var option = arrayOf("Open Profile","Send Message")
                    //สร้าง builder คือตัว option ที่จะแสดงขึ้นมา
                    var builder = AlertDialog.Builder(context!!)
                    builder.setTitle("Select Option")
                    builder.setItems(option){dialogInterface, i ->
                        if (i == 0){
                            //ใส่ context แทน this เพราะตัว fragment ไม่มี context เป็นของตัวเอง
                            //context คือ context ของ activity ที่เป็น parent มัน นั้นคือตัว DashboardActivity
                            var intent = Intent(context,ProfileActivity::class.java)
                            intent.putExtra("userid",friendId)
                            startActivity(intent)
                        }else{
                            //ดึงข้อมูล (addListenerForSingleValueEvent ใช้ในการดึงครั้งเดียว ไม่ต้องการให้มีการอัพเดทตลอดเวลา
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
                                        userList.put("0",userId)
                                        userList.put("1",friendId)

                                        //เรียก user list และใส่ user list ลงไปใน messages
                                        messageRef.child("user_list").setValue(userList)

                                        //ดึง key ออกมาจาก messageRef
                                        chatId = messageRef.key.toString()

                                        //นำ key ไปเก็บไว้ที่ผู้ส่ง
                                        var userDataRef = mDatabase!!.reference.child("Chat").child(userId).child(friendId).child("chat_id")
                                        userDataRef.setValue(chatId)

                                        //นำ key ไปเก็บไว้ที่ผู้รับ
                                        var friendDataRef = mDatabase!!.reference.child("Chat").child(friendId).child(userId).child("chat_id")
                                        friendDataRef.setValue(chatId)
                                    }
                                    //ส่ง chat id ไปให้หน้า chat
                                    var intent = Intent(context,ChatActivity::class.java)
                                    intent.putExtra("chatid",chatId)
                                    intent.putExtra("friendid",friendId)
                                    startActivity(intent)

                                }
                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })

                        }
                    }
                    builder.show()
                }

            }

        }
        recycle_users.setHasFixedSize(true)
        recycle_users.layoutManager = linearLayoutManager
        recycle_users.adapter = adapter
    }

}

