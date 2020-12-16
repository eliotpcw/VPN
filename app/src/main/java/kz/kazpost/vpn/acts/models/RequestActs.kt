package kz.kazpost.vpn.acts.models

data class RequestActs(
    val login: String,
    val transportId: String,
    val currentDepartment: String,
    val acts: MutableList<ActModel>?
)