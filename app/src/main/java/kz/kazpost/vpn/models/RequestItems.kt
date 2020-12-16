package kz.kazpost.vpn.models

data class RequestItems(
    var login: String,
    var department: String,
    var transportListId: String,
    var index: Int,
    var labels: Labels,
    var items: List<String>
)