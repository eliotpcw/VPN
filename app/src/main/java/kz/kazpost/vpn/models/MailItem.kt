package kz.kazpost.vpn.models

data class MailItem(
    val mailId: String,
    val mailType:String,
    val fromDepartment: String,
    val toDepartment: String,
    val hasAct: Boolean
)