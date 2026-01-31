package com.example.gateway_service


const val USER_ID_KEY = "id"
const val USER_USERNAME_KEY = "username"
const val USER_ROLE_KEY = "role"
const val USER_PERMISSION_GROUPS_KEY = "permissionGroups"
const val USER_PERMISSIONS_KEY = "permissions"
const val USER_DETAILS_HEADER_KEY = "X-User-Details"
const val USER_ID_HEADER_KEY = "X-User-Id"
const val USER_NAME_HEADER_KEY = "X-User-Name"
const val REQUEST_ID_HEADER = "J-Request-Id"
const val START_TIME_HEADER_KEY = "J-Start-Time"
const val CACHED_REQUEST_BODY_KEY = "cached-request-body"
const val CACHED_URL_KEY = "cached-request-url"
const val API_LOG_TOPIC = "OB_API_LOG"
const val X_REAL_IP = "x-real-ip"


val IGNORED_HEADERS = setOf(USER_DETAILS_HEADER_KEY, REQUEST_ID_HEADER)