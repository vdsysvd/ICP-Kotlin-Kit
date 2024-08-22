package com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_variant

import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_comment.IDLComment
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLType
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.reflective

internal data class IDLVariant(
    val comment: IDLComment? = null,
    val id: String,
    val type: IDLType
) {
    companion object : ParserNodeDeclaration<IDLVariant> by reflective()
}