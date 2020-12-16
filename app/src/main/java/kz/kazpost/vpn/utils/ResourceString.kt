package kz.kazpost.vpn.utils

import android.content.Context

sealed class ResourceString {
    abstract fun format(context: Context): String
}

data class IdResourceString(private val id: Int) : ResourceString() {
    override fun format(context: Context): String = context.getString(id)
    override fun toString() = "IdResourceString: $id"
}

data class TextResourceString(private val text: String?) : ResourceString() {
    override fun format(context: Context): String = text ?: ""
    override fun toString() = "TextResourceString: $text"
}

class FormatResourceString(private val id: Int, vararg val args: Any) : ResourceString() {
    override fun format(context: Context): String = context.getString(id, *args)
    override fun equals(other: Any?) = other is FormatResourceString && id == other.id && args.contentEquals(other.args)
    override fun hashCode() = id * 37 xor args.contentHashCode()
    override fun toString() = "FormatResourceString: $id ${args.contentToString()}"
}