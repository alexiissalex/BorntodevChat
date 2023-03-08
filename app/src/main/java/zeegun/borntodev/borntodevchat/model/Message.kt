package zeegun.borntodev.borntodevchat.model

class Message() {
    //ดู Message ใน firebase และสร้างตัวแปรตามนั้นคือ มี message และ sender
    var sender:String? = null
    var message:String? = null
    constructor(sender:String,message:String):this(){
        this.message = message
        this.sender = sender
    }
}