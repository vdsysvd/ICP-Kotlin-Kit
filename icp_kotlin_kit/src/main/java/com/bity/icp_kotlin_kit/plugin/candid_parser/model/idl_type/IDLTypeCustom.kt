package com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type

import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.reflective

internal class IDLTypeCustom(
    typeId: String? = null,
    isOptional: Boolean = false,
    val typeDef: String
) : IDLType(typeId, isOptional) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as IDLTypeCustom

        return typeDef == other.typeDef
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + typeDef.hashCode()
        return result
    }

    companion object : ParserNodeDeclaration<IDLTypeCustom> by reflective()
}