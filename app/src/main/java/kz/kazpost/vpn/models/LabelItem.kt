package kz.kazpost.vpn.models

data class LabelItem (
    val labelListId: String,
    val fromDepartment: String,
    val toDepartment: String,
    val labelType: String,
    val hasAct: Boolean
)