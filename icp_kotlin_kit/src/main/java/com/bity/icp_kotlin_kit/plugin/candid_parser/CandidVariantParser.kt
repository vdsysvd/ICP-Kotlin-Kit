package com.bity.icp_kotlin_kit.plugin.candid_parser

import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_comment.IDLComment
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_comment.IDLSingleLineComment
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLType
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeBlob
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeCustom
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeInt
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeNat
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeNat64
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeNull
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeRecord
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeText
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeVec
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_variant.IDLVariant
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_variant.IDLVariantDeclaration
import com.bity.icp_kotlin_kit.plugin.candid_parser.util.ext_fun.trimCommentLine
import com.bity.icp_kotlin_kit.plugin.candid_parser.util.ext_fun.trimEndOfLineComment
import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.parser.dsl.either
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.item
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.optional
import guru.zoroark.tegral.niwen.parser.dsl.or
import guru.zoroark.tegral.niwen.parser.dsl.repeated
import guru.zoroark.tegral.niwen.parser.dsl.self

internal object CandidVariantParser {

    private val variantLexer = niwenLexer {
        state {
            matches(";[^\n]\\s*//.*") isToken Token.EndOfLineComment
            matches("//.*") isToken Token.SingleLineComment

            "{" isToken Token.LBrace
            "}" isToken Token.RBrace
            ";" isToken Token.Semi
            ":" isToken Token.Colon

            "opt" isToken Token.Opt

            // TODO join?
            matches("""vec\s+record\s+\{([^{}]*|\{[^{}]*\})*}""") isToken Token.Vec
            matches("""vec\s+\w+""") isToken Token.Vec
            matches("record \\{[^}]+\\}") isToken Token.Record

            // TODO, can I have nested variant?
            "variant" isToken Token.Variant

            "null" isToken Token.Null
            "text" isToken Token.Text
            "blob" isToken Token.Blob

            "int" isToken Token.Int

            "nat64" isToken Token.Nat64
            "nat" isToken Token.Nat

            matches("[a-zA-Z_][a-zA-Z0-9_]*") isToken Token.Id
            matches("[ \t\r\n]+").ignore
            matches("//[^\n]*").ignore
        }
    }

    private val variantParser = niwenParser {

        IDLVariantDeclaration root {
            expect(Token.Variant)
            expect(Token.LBrace)
            repeated<IDLVariantDeclaration, IDLVariant> {
                expect(IDLVariant) storeIn item
            } storeIn IDLVariantDeclaration::variants
            expect(Token.RBrace)
            optional { expect(Token.Semi) }
        }

        /**
         * IDLVariant
         */
        // TODO, we can add an extra param for end of line comment
        IDLVariant {
            optional {
                expect(IDLComment) storeIn IDLVariant::comment
            }
            optional {
                expect(Token.Id) storeIn IDLVariant::id
                expect(Token.Colon)
            }

            expect(IDLType) storeIn IDLVariant::type
            either {
                expect(IDLComment) storeIn IDLVariant::comment
            } or {
                optional { expect(Token.Semi) }
            }
        }

        /**
         * Comment
         */
        IDLComment {
            either {
                expect(IDLSingleLineComment) storeIn self()
            }
        }
        IDLSingleLineComment {
            either {
                repeated(min = 1) {
                    expect(Token.SingleLineComment) transform { it.trimCommentLine() } storeIn item
                } storeIn IDLSingleLineComment::commentLines
            } or {
                repeated(min = 1) {
                    expect(Token.EndOfLineComment) transform { it.trimEndOfLineComment() } storeIn item
                } storeIn IDLSingleLineComment::commentLines
            }
        }

        /**
         * IDL Type
         */
        IDLType {
            either {
                expect(IDLTypeCustom) storeIn self()
            } or {
                expect(IDLTypeText) storeIn self()
            } or {
                expect(IDLTypeBlob) storeIn self()
            } or {
                expect(IDLTypeInt) storeIn self()
            } or {
                expect(IDLTypeNat) storeIn self()
            } or {
                expect(IDLTypeNat64) storeIn self()
            } or {
                expect(IDLTypeRecord) storeIn self()
            } or {
                expect(IDLTypeNull) storeIn self()
            } or {
                expect(IDLTypeVec) storeIn self()
            }
        }

        IDLTypeNull { expect(Token.Null) }
        IDLTypeBlob { expect(Token.Blob) }
        IDLTypeText { expect(Token.Text) }
        IDLTypeVec { expect(Token.Vec) storeIn IDLTypeVec::vecDeclaration }
        IDLTypeCustom { expect(Token.Id) storeIn IDLTypeCustom::typeDef }
        IDLTypeRecord {
            expect(Token.Record) transform { indentString(it) } storeIn IDLTypeRecord::recordDeclaration
            optional { expect(Token.Semi) }
        }

        /**
         * Type Int
         */
        IDLTypeInt { expect(Token.Int) }

        /**
         * Type Nat
         */
        IDLTypeNat { expect(Token.Nat) }
        IDLTypeNat64 { expect(Token.Nat64) }
    }

    fun parseVariant(input: String): IDLVariantDeclaration {
        return variantParser.parse(variantLexer.tokenize(input))
    }

    private fun indentString(input: String, indentSize: Int = 4): String {
        var currentIndent = 0
        val indent = " ".repeat(indentSize)
        val lines = input.trim().lines()

        return lines.joinToString("\n") { line ->
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("}") -> {
                    currentIndent -= 1
                    "${indent.repeat(currentIndent)}$trimmedLine"
                }

                trimmedLine.endsWith("{") -> {
                    val result = "${indent.repeat(currentIndent)}$trimmedLine"
                    currentIndent += 1
                    result
                }

                else -> "${indent.repeat(currentIndent)}$trimmedLine"
            }
        }
    }
}