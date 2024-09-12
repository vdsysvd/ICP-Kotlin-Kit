package com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type

import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_comment.IDLComment
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.dsl.subtype

internal sealed class IDLType(
    open val comment: IDLComment?,
    open val isOptional: Boolean,
    open val id: String?
) {
    companion object : ParserNodeDeclaration<IDLType> by subtype()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IDLType

        if (comment != other.comment) return false
        if (isOptional != other.isOptional) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = comment?.hashCode() ?: 0
        result = 31 * result + isOptional.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }
}