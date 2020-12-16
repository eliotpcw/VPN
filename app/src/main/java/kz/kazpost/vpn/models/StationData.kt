package kz.kazpost.vpn.models

data class StationData (
    val transportListId: String,
    val status: String,
    val index: Int,
    val fromDep: String,
    val nextDep: String,
    val road: ArrayList<Road>,
    val labels: Labels,
    val labelItems: List<LabelItem>?,
    val mailItems: List<MailItem>?,
    val items: List<String>?,
    val result: String
)