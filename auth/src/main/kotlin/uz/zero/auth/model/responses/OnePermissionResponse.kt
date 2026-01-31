package uz.davrbank.auth.models.responses

data class OnePermissionResponse(
    val id: Long,
    val name: String,
    val description: String,
    val key: String,
    val permissionGroup: PermissionGroupResponse
)