package kz.kazpost.vpn.acts.models

data class RequestAct51(
    val login: String,
    val transportId: String,
    val currentDepartment: String,
    val act: Act51,
    val violations: Violations
)