package kz.kazpost.vpn.acts.models

data class RequestAct30(
    val login: String,
    val transportId: String,
    val currentDepartment: String,
    val act: Act30,
    val violations: Violations
)