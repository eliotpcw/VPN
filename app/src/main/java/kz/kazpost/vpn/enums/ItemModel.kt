package kz.kazpost.vpn.enums

data class ItemModel (
    var type: String,
    var typeName: String,
    var shpi: String,
    var from: String,
    var to: String,
    var fromName: String,
    var toName: String,
    var hasAct: Boolean
)