package kz.kazpost.vpn.models

data class VPNData (
    val transportListId: String?,
    val status: String?,
    val index: Int?,
    val fromDep: String?,
    val nextDep: String?,
    val roads: List<Road>?,
    val result: String,
    val resultInfo: String?
)