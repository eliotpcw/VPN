package kz.kazpost.vpn.models

data class Road (
    val dept: Dept,
    val isActive: Boolean,
    val index: Int
){
    override fun toString(): String {
        return dept.nameRu
    }
}