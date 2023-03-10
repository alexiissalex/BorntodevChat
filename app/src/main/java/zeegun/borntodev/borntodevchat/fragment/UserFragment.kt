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

    //??????????????????????????? option ?????????????????????????????????????????? firebase ui
    //????????????????????????????????? adepter ????????????????????????????????? firebase ui ?????????????????????????????????????????????????????????????????? recyclerview ?????????
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

        //?????????????????????????????????????????????????????????????????????????????? user ?????????????????? dashboard user
        var linearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)

        //??????????????????????????? query ?????????????????????????????? query ?????????????????? firebase
        //query ?????????????????????????????????????????????????????????????????? query ??????????????? ???????????????????????????????????????????????????????????? ????????? .orderByChild
        var query = mDatabase!!.reference.child("Users").orderByChild("display_name")

        //??????????????? option
        var option = FirebaseRecyclerOptions.Builder<User>().setQuery(query,User::class.java).setLifecycleOwner(this).build()
        //?????? option ????????????????????? recycler ???????????????????????????????????? adepter
        //??????????????????????????????????????????????????? adepter ??????????????????????????????????????? ViewHolder ?????????????????????????????????????????? biding ????????? data ????????? text box ??????????????? ?????????????????????????????????

        var adapter = object : FirebaseRecyclerAdapter<User,UserHolder>(option){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
                //?????????????????????????????????????????? roll ?????????????????? xml ??????????????????
                return UserHolder(LayoutInflater.from(parent.context).inflate(R.layout.users_row,parent,false))
            }


            override fun onBindViewHolder(holder: UserHolder, position: Int, model: User) {
                holder.bind(model)

                //????????? id ????????? ??????????????????????????????????????????????????????????????????????????????
                var friendId = getRef(position).key.toString()
                //??????????????? userid ???????????????????????????
                var userId = mAuth!!.currentUser!!.uid
                //??????????????? chat id
                var chatId:String? = null

                //check ????????? id ????????? userId ????????? friendId ????????????????????? firebase ????????? ?????????????????????????????????????????????????????????????????????????????? ??????????????????????????????????????????????????????
                var chartRef = mDatabase!!.reference.child("Chat").child(userId).child(friendId).child("chat_id")


                //Set onclick listener ????????????????????? option ??????????????????????????????????????? profile ???????????? chat
                holder.itemView.setOnClickListener {
                    //???????????? option
                    var option = arrayOf("Open Profile","Send Message")
                    //??????????????? builder ?????????????????? option ?????????????????????????????????????????????
                    var builder = AlertDialog.Builder(context!!)
                    builder.setTitle("Select Option")
                    builder.setItems(option){dialogInterface, i ->
                        if (i == 0){
                            //????????? context ????????? this ???????????????????????? fragment ??????????????? context ???????????????????????????????????????
                            //context ????????? context ????????? activity ????????????????????? parent ????????? ?????????????????????????????? DashboardActivity
                            var intent = Intent(context,ProfileActivity::class.java)
                            intent.putExtra("userid",friendId)
                            startActivity(intent)
                        }else{
                            //??????????????????????????? (addListenerForSingleValueEvent ??????????????????????????????????????????????????????????????? ????????????????????????????????????????????????????????????????????????????????????????????????
                            chartRef.addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    //?????????????????????????????????????????????????????? ????????????????????? chat ????????????????????????????????????????????????????????? ????????????????????????????????????????????? chat id ????????????????????????
                                    if(snapshot.exists()){
                                        //????????????????????????????????????????????????????????????????????????
                                        chatId = snapshot.value.toString()
                                    }else{
                                        //????????????????????????????????????????????????????????????????????????????????? chat ?????????????????? ????????? .push() ??????????????????????????????????????? id ???????????????????????????????????????????????????
                                        var messageRef = mDatabase!!.reference.child("Messages").push()

                                        //???????????? chat ????????????????????????????????????????????????
                                        var userList = HashMap<String,String>()
                                        userList.put("0",userId)
                                        userList.put("1",friendId)

                                        //??????????????? user list ?????????????????? user list ?????????????????? messages
                                        messageRef.child("user_list").setValue(userList)

                                        //????????? key ???????????????????????? messageRef
                                        chatId = messageRef.key.toString()

                                        //?????? key ??????????????????????????????????????????????????????
                                        var userDataRef = mDatabase!!.reference.child("Chat").child(userId).child(friendId).child("chat_id")
                                        userDataRef.setValue(chatId)

                                        //?????? key ??????????????????????????????????????????????????????
                                        var friendDataRef = mDatabase!!.reference.child("Chat").child(friendId).child(userId).child("chat_id")
                                        friendDataRef.setValue(chatId)
                                    }
                                    //????????? chat id ??????????????????????????? chat
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

