package dasturlash.attachservice

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

data class CreateAttachResponse(
    val id: Long,
)

data class AttachUrl(
    val hash: String,
    val url: String,
)