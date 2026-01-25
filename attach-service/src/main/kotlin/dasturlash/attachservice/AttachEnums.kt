package dasturlash.attachservice

enum class ErrorCode(val code: Int, val message: String) {
    FILE_CREATION_FAILED(100, "FILE_CREATION_FAILED"),
    ATTACH_NOT_FOUND(101, "ATTACH_NOT_FOUND"),

}