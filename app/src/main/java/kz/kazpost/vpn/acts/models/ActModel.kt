package kz.kazpost.vpn.acts.models

data class ActModel (
    val typeRpo: String,
    val typeAct: String,
    val shpi: String,
    val reasonId: String,
    val violationId: String,
    val violationName: String,
    val content: String
)