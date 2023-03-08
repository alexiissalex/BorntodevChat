package zeegun.borntodev.borntodevchat.model

class RecentChat() {
    var date:String? = null
    var last_message: String? = null

    constructor(date:String,last_message:String):this(){
        this.date= date
        this.last_message = last_message
    }
}