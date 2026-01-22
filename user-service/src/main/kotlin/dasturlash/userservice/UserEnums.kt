package dasturlash.userservice

enum class ErrorCode(val code: Int, val message: String) {
    USER_NOT_FOUND_EXCEPTION(100, "USER_NOT_FOUND_EXCEPTION"),
    EMAIL_ALREADY_EXISTS(102, "EMAIL_ALREADY_EXISTS"),
    USERNAME_ALREADY_EXISTS(103, "USERNAME_ALREADY_EXISTS"),

}