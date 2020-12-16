package kz.kazpost.vpn.acts.models

data class RequestActLabel(
    val login: String,
    val transportId: String,
    val currentDepartment: String,
    val act: ActLabel,
    val violations: Violations
)