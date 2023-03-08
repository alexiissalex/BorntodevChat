package zeegun.borntodev.borntodevchat.model

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.users_row.view.*
import zeegun.borntodev.borntodevchat.R

//รับค่าเข้ามา สามารถนำมาใช้ได้ โดยนำข้อความมาใส่ เมื่อใส่แล้วให้ทำการส่งข้อความส่งกลับไป เพื่อนำไปแสดงผล
class UserHolder(val customView: View): RecyclerView.ViewHolder(customView) {

    fun bind(user: User){
        customView.tv_name_row?.text = user.display_name
        customView.tv_status_row?.text = user.status
        if(!user.thumb_image!!.equals("default")){
            Picasso.get().load(user.thumb_image).placeholder(R.drawable.ic_man).into(customView.iv_user_row)
        }
    }

    fun bind(user:User , recent:String){
        customView.tv_name_row?.text = user.display_name
        if (recent != null && recent != "null"){
            customView.tv_status_row?.text = recent
        }else{
            customView.tv_status_row?.text= ""
        }

        if(!user.thumb_image!!.equals("default")){
            Picasso.get().load(user.thumb_image).placeholder(R.drawable.ic_man).into(customView.iv_user_row)
        }
    }
}