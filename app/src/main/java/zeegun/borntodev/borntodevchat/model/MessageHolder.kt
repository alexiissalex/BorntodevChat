package zeegun.borntodev.borntodevchat.model

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.message_row.view.*
import zeegun.borntodev.borntodevchat.R

class MessageHolder(val customview: View) : RecyclerView.ViewHolder(customview) {
    //รับแค่ข้อมูลเข้ามาก่อน ยังไม่รวมรูปภาพ
    fun bind(message:String,sender:String,image:String){
        customview.tv_message_row_left.text = message
        customview.tv_name_message_row_left.text = sender
        customview.tv_message_row_right.text = message
        customview.tv_name_message_row_right.text = sender
        Picasso.get().load(image).placeholder(R.drawable.ic_man).into(customview.iv_image_message_row_left)
        Picasso.get().load(image).placeholder(R.drawable.ic_man).into(customview.iv_image_message_row_right)
    }

}