package dasturlash.postservice

enum class ErrorCode(val code: Int, val message: String) {
    POST_NOT_FOUND(100, "POST_NOT_FOUND"),
    USER_ALREADY_LIKED(101, "USER_ALREADY_LIKED"),
    USER_ALREADY_DISLIKED(102, "USER_ALREADY_DISLIKED"),
    POST_NOT_YOURS(103,"POST_NOT_YOURS"),

}