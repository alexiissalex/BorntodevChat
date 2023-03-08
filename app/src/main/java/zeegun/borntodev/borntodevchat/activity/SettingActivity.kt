package zeegun.borntodev.borntodevchat.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.canhub.cropper.CropImage
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_setting.*
import zeegun.borntodev.borntodevchat.R
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.collections.HashMap

class SettingActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null
    var mDatabase: FirebaseDatabase? = null

    //สร้างตัว storage
    var mStorage: FirebaseStorage? = null

//    //Code 2020
////    //RequestCode เอาไว้ตรวจสอบค่าที่ส่งไปว่าส่งกลับมาเหมือนกันไหม
//    var GALLERY_ID: Int = 1
    lateinit var myBitmap : Bitmap

    private val cropActivityResultContract = object :
    ActivityResultContract<Any?,Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(1,1)
                .getIntent(this@SettingActivity)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uriContent
        }
    }

    private lateinit var cropActivityResultLancher: ActivityResultLauncher<Any?>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.activity_setting
        )

        //เพิ่มการปุ่ม back กลับ ทางซ้ายมือ
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //เชื่อมต่อ database เพื่อเรียกใช้
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mStorage = FirebaseStorage.getInstance()

        //ดึงผู้ใช้จาก firebase ขึ้นมา
        var userId = mAuth!!.currentUser!!.uid //uid == UserID

        var userRef = mDatabase!!.reference.child("Users").child(userId)
        //ดึงข้อมูลจาก userid ทั้งหมดมาแสดง
        userRef.addValueEventListener(object : ValueEventListener {
            //หากข้อมูลมีการเปลี่ยนเปลงจะให้ทำอะไร หรือรับข้อมูลเข้ามาจะให้ทำอะไร
            override fun onDataChange(snapshot: DataSnapshot) {
                //ดึงข้อมูลมาจาก dataSnapshot
                var display_name =
                    snapshot!!.child("display_name").value.toString() //ใน path ต้องชื่อตรงกับ data ใน firebase
                var status =
                    snapshot!!.child("status").value.toString() //ใน path ต้องชื่อตรงกับ data ใน firebase
                var image =
                    snapshot!!.child("image").value.toString() //ใน path ต้องชื่อตรงกับ data ใน firebase

                //ใส่ข้อมูลลงใน textview
                tv_display_name_setting.text = display_name
                tv_status_setting.text = status

                //การเรียกรูปอาจเป็นลิ้งค์ หรือรูป default เข้ามา จำเป็นต้องใช้  library
                //ซึ่งต้องทำการ if ก่อน เพราะถ้าสร้าง user เป็น default ไม่ต้องโหลดรูป
                if (image != "default") {
                    // load (URL) ,placeholder == ระหว่างที่โหลดจะให้ใส่รูปอะไร , ใส่ไปในรูปตัวไหน into(imageview)
                    Picasso.get().load(image).placeholder(R.drawable.ic_man).into(iv_profile_image)
                }

            }

            //cancelled ให้ทำอะไร
            override fun onCancelled(error: DatabaseError) {
                finish()
            }

        })

        //กดปุ่ม status แล้วเปลี่ยนมาหน้า statusActivity
        btn_change_status.setOnClickListener {
            var intent = Intent(this, StatusActivity::class.java)
            //นำ status เก่ามาโชว์ด้วย ส่งค่า status เดิมไปด้วย
            intent.putExtra("status", tv_status_setting.text.toString())
            startActivity(intent)
        }

        cropActivityResultLancher = registerForActivityResult(cropActivityResultContract){
                it?.let{ uri->

//                //สร้างตัว มาเก็บค่า path ของรูปที่ถูก crop
//                var result = CropImage.getActivityResult(data)
                //ดึง uri หรือตัว path ออกมาจาก result
//                var resultUri = result!!.uriContent

                    myBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
                    var thumbBitmap = Bitmap.createScaledBitmap(myBitmap, 200,200,true)


                //ย่อขนาดไฟล์ด้วย liberty
                //ver implementation 'id.zelory:compressor:2.1.1'
//                var thumbBitmap = Compressor(this)
//                    .setMaxHeight(200)
//                    .setMaxWidth(200)
//                    .setQuality(65)
//                    .compressToBitmap(thumbFile)

                //นำ bitmap ที่ได้มาเขียนให้กลายเป็นภาพ jpg
                var byteArray = ByteArrayOutputStream()
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray)


                //แปลงเป็น byteArray
                var thumbByteArray = byteArray.toByteArray()

                var userId = mAuth!!.currentUser!!.uid
                //จะสร้างตัวที่เชื่อมต่อตัว storage ในการ upload รูปขึ้นไปเก็บ
                var imageRef =
                    mStorage!!.reference.child("profile_image").child("$userId.jpg") //เก็บรูป full
                var thumbRef = mStorage!!.reference.child("profile_image").child("thumb_image")
                    .child("$userId.jpg") //เก็บรูปย่อ

                //การทำงานคือ เมื่อ Upload รูปเสร็จ จะดึงตัวหรือ put และส่ง path หรือ Link ไปเก็บใน realtime Database
                // Continuation : รับค่า UploadTask,TaskSnapshot และ return Uri กลับไป
                //Upload รูปแรก
                imageRef.putFile(uri)
                    .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        // ถ้าไม่ successful ให้โยนออกไป
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@Continuation imageRef.downloadUrl //ส่ง uri รูปแรกออกมา
                    }).addOnCompleteListener { //หากเสร็จแล้วให้ทำอะไร
                        task: Task<Uri> ->
                    if (task.isComplete) { // isComplete เพราะหากจะทำงานต่อต้องรอดาวโหลดรูปแรกเสร็จก่อน ถึงจะได้ uri มา
                        var imageUri = task.result.toString() //เก็บ uri รูปแรก

                        //ขั้นตอนต่อไปคือ upload รูปสองต้องสร้าง task ให้
                        var uploadTask: UploadTask = thumbRef.putBytes(thumbByteArray)
                        //สั่งให้ Upload รูปสอง
                        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            return@Continuation thumbRef.downloadUrl //ส่ง uri รูปสองออกมา
                        }).addOnCompleteListener { task: Task<Uri> ->
                            if (task.isComplete) {
                                var thumbUri = task.result.toString()//เก็บ uri รูปสอง

                                //สร้าง Array หรือ Object ในการเก็บข้อมูลเพื่อ Upload
                                var updateObject = HashMap<String, Any>()
                                updateObject.put("image", imageUri)
                                updateObject.put("thumb_image", thumbUri)

                                //Upload ขึ้น firebase
                                mDatabase!!.reference.child("Users").child(userId)
                                    //ใส่ข้อมูลแบบ Array
                                    .updateChildren(updateObject)
                                    .addOnCompleteListener {
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                "Upload Successful",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(this, "Upload Error", Toast.LENGTH_LONG)
                                                .show()
                                        }
                                    }
                            }
                        }
                    }
                }

            }
        }

        btn_change_picture.setOnClickListener {
            cropActivityResultLancher.launch("image/*")
        }

        //Code 2020
        //Set ปุ่มเปลี่ยนรูปทำงาน วิธีการคือต้องสร้างตัวที่ไปดึงไฟล์รูปในเครื่อง นำมา crop
        //เลือกรูป
//        btn_change_picture.setOnClickListener {
//            //สร้าง intent ขึ้นมา
//            var galleryIntent = Intent()
//            galleryIntent.type = "image/*" //type เลือกรูป /* คือทั้งหมด
//            galleryIntent.action = Intent.ACTION_GET_CONTENT  //ให้ไปดึง content
//            //ต้องการผลลัพธ์ที่ส่งกลับมาด้วย
//            startActivityForResult(
//                Intent.createChooser(galleryIntent, "Select image"),
//                GALLERY_ID
//            ) //จะส่ง path ของรูปกลับมาให้
//        }


        //Code 2020
        //เมื่อบรรทัดนี้รันเสร็จ จะเรียก activity ของ crop image ขึ้นมา แต่ในการเรียกจะเรียก activity จะเป็น activity results เหมือนกัน
        //หมายความว่าหลังจากเรียกรูป ปรับรูปเสร็จ จะส่ง request หรือ results กลับมาที่ fun onActivityResult

//        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//            super.onActivityResult(requestCode, resultCode, data)
//            //หากตรงตามเงื่อนไขแล้วไม่ error (Ok) ให้ทำตามเงื่อนไขดังนี้
//            if (requestCode == GALLERY_ID && resultCode == Activity.RESULT_OK) {
//                var image = data!!.data
//                //เรียกใช้ function ที่ import เข้ามา .activity () ต้องใส่ uri หรือ data ที่ได้มา
//                //ปรับแต่งรูป
//                CropImage.activity(image).setAspectRatio(1, 1).start(this)
//            }
//
//
//            //Code 2020
//            //เมื่อกดปรับแต่งรูปเรียบร้อยจะเข้าการทำงานที่นี่
//            //เมื่อ crop รูปเสร็จจะได้ activity Result data กลับมาคือ uri
//            //uri คือ เมื่อทำการ crop เรียบร้อย จะส่งข้อมูลกลับมาที่ ActivityResult เราจะทำการเช็คว่าตัวนี้เป็นของ crop image ใช่หรือไม่
//            //เพราะฉะนั้นข้อมูลหรือ data ตรง onActivityResult คือ path ที่เอาไว้เก็บไฟล์อยู่ในเครื่อง
//            //เมื่อได้ path จะทำการย่อไฟล์ให้เป็นอีกไฟล์ เพราะต้องมี terminal กับ ไฟล์ตัวหลัก เมื่อมีไฟล์ 2 ตัวนี้เราจะส่ง 2 ไฟล์นี้(รูป) ขึ้นไปที่ storage คืออยู่ใน firebase
//            //และเราจะส่งลิ้งค์ หรือ path ไปเก็บไว้ที่ตัว realtime
//            // &&resultCode == Activity.RESULT_OK เช็คว่าถูกต้องไหม
//            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//                //สร้างตัว มาเก็บค่า path ของรูปที่ถูก crop
//                var result = CropImage.getActivityResult(data)
//                //ดึง uri หรือตัว path ออกมาจาก result
//                var resultUri = result!!.uriContent
//                var thumbFile = File(resultUri!!.path)
//
//                //ย่อขนาดไฟล์ด้วย liberty
//                //ver implementation 'id.zelory:compressor:2.1.0'
//                var thumbBitmap = Compressor(this)
//                    .setMaxHeight(200)
//                    .setMaxWidth(200)
//                    .setQuality(65)
//                    .compressToBitmap(thumbFile)
//
//                //นำ bitmap ที่ได้มาเขียนให้กลายเป็นภาพ jpg
//                var byteArray = ByteArrayOutputStream()
//                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray)
//
//
//                //แปลงเป็น byteArray
//                var thumbByteArray = byteArray.toByteArray()
//
//                var userId = mAuth!!.currentUser!!.uid
//                //จะสร้างตัวที่เชื่อมต่อตัว storage ในการ upload รูปขึ้นไปเก็บ
//                var imageRef =
//                    mStorage!!.reference.child("profile_image").child("$userId.jpg") //เก็บรูป full
//                var thumbRef = mStorage!!.reference.child("profile_image").child("thumb_image")
//                    .child("$userId.jpg") //เก็บรูปย่อ
//
//                //การทำงานคือ เมื่อ Upload รูปเสร็จ จะดึงตัวหรือ put และส่ง path หรือ Link ไปเก็บใน realtime Database
//                // Continuation : รับค่า UploadTask,TaskSnapshot และ return Uri กลับไป
//                //Upload รูปแรก
//                imageRef.putFile(resultUri)
//                    .continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
//                        // ถ้าไม่ successful ให้โยนออกไป
//                        if (!task.isSuccessful) {
//                            task.exception?.let {
//                                throw it
//                            }
//                        }
//                        return@Continuation imageRef.downloadUrl //ส่ง uri รูปแรกออกมา
//                    }).addOnCompleteListener { //หากเสร็จแล้วให้ทำอะไร
//                        task: Task<Uri> ->
//                    if (task.isComplete) { // isComplete เพราะหากจะทำงานต่อต้องรอดาวโหลดรูปแรกเสร็จก่อน ถึงจะได้ uri มา
//                        var imageUri = task.result.toString() //เก็บ uri รูปแรก
//
//                        //ขั้นตอนต่อไปคือ upload รูปสองต้องสร้าง task ให้
//                        var uploadTask: UploadTask = thumbRef.putBytes(thumbByteArray)
//                        //สั่งให้ Upload รูปสอง
//                        uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
//                            if (!task.isSuccessful) {
//                                task.exception?.let {
//                                    throw it
//                                }
//                            }
//                            return@Continuation thumbRef.downloadUrl //ส่ง uri รูปสองออกมา
//                        }).addOnCompleteListener { task: Task<Uri> ->
//                            if (task.isComplete) {
//                                var thumbUri = task.result.toString()//เก็บ uri รูปสอง
//
//                                //สร้าง Array หรือ Object ในการเก็บข้อมูลเพื่อ Upload
//                                var updateObject = HashMap<String, Any>()
//                                updateObject.put("image", imageUri)
//                                updateObject.put("thumb_image", thumbUri)
//
//                                //Upload ขึ้น firebase
//                                mDatabase!!.reference.child("Users").child(userId)
//                                    //ใส่ข้อมูลแบบ Array
//                                    .updateChildren(updateObject)
//                                    .addOnCompleteListener {
//                                        if (task.isSuccessful) {
//                                            Toast.makeText(
//                                                this,
//                                                "Upload Successful",
//                                                Toast.LENGTH_LONG
//                                            ).show()
//                                        } else {
//                                            Toast.makeText(this, "Upload Error", Toast.LENGTH_LONG)
//                                                .show()
//                                        }
//                                    }
//
//                            }
//
//                        }
//                    }
//                }
//
//
//            }
//        }
    }
}