package com.bity.icp_kotlin_kit.plugin.candid_parser

import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeDeclaration
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_comment.IDLSingleLineComment
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeBlob
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeFunc
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeNat64
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeRecord
import com.bity.icp_kotlin_kit.plugin.candid_parser.model.idl_type.IDLTypeVariant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

internal class CandidTypeParserTest {

    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("singleTypeDeclaration")
    fun `single types` (
        input: String,
        expectedResult: IDLTypeDeclaration
    ) {
        val typeDeclaration = CandidTypeParser.parseType(input)
        assertEquals(expectedResult, typeDeclaration)
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("funcTypeDeclaration")
    fun `func types` (
        input: String,
        expectedResult: IDLTypeDeclaration
    ) {
        val typeDeclaration = CandidTypeParser.parseType(input)
        assertEquals(expectedResult, typeDeclaration)
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("recordTypeDeclaration")
    fun `record types` (
        input: String,
        expectedResult: IDLTypeDeclaration
    ) {
        val typeDeclaration = CandidTypeParser.parseType(input)
        assertEquals(expectedResult, typeDeclaration)
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("variantTypeDeclaration")
    fun `variant types` (
        input: String,
        expectedResult: IDLTypeDeclaration
    ) {
        val typeDeclaration = CandidTypeParser.parseType(input)
        assertEquals(expectedResult, typeDeclaration)
    }


    companion object {

        @JvmStatic
        private fun singleTypeDeclaration() = listOf(
            Arguments.of(
                "type AccountIdentifier = blob;",
                IDLTypeDeclaration(
                    id = "AccountIdentifier",
                    type = IDLTypeBlob()
                )
            ),

            Arguments.of(
                "type AccountIdentifier = opt blob;",
                IDLTypeDeclaration(
                    id = "AccountIdentifier",
                    isOptional = true,
                    type = IDLTypeBlob()
                )
            ),

            Arguments.of(
                """
                    // This
                    // is
                    // a
                    // comment
                    type AccountIdentifier = opt blob;
                """.trimIndent(),
                IDLTypeDeclaration(
                    comment = IDLSingleLineComment(
                        listOf("This", "is", "a", "comment")
                    ),
                    id = "AccountIdentifier",
                    isOptional = true,
                    type = IDLTypeBlob()
                )
            ),

            Arguments.of(
                "type Memo = nat64;",
                IDLTypeDeclaration(
                    id = "Memo",
                    type = IDLTypeNat64(),
                )
            ),

            Arguments.of(
                """
                    // This is a simple comment
                    type Subaccount = blob;
                """.trimIndent(),
                IDLTypeDeclaration(
                    comment = IDLSingleLineComment(listOf("This is a simple comment")),
                    id = "Subaccount",
                    type = IDLTypeBlob()
                )
            ),
        )

        @JvmStatic
        private fun funcTypeDeclaration() = listOf(
            Arguments.of(
                "type QueryArchiveFn = func (GetBlocksArgs) -> (QueryArchiveResult) query;",
                IDLTypeDeclaration(
                    id = "QueryArchiveFn",
                    type = IDLTypeFunc(
                        funcDeclaration = "func (GetBlocksArgs) -> (QueryArchiveResult) query"
                    )
                )
            ),

            Arguments.of(
                "type QueryArchiveFn = func (GetBlocksArgs) -> (QueryArchiveResult);",
                IDLTypeDeclaration(
                    id = "QueryArchiveFn",
                    type = IDLTypeFunc(
                        funcDeclaration = "func (GetBlocksArgs) -> (QueryArchiveResult)"
                    )
                )
            ),
        )

        @JvmStatic
        private fun recordTypeDeclaration() = listOf(
            Arguments.of(
                """
                    type Tokens = record {
                        e8s : nat64;
                    };
                """.trimIndent(),
                IDLTypeDeclaration(
                    id = "Tokens",
                    type = IDLTypeRecord(
                        recordDeclaration = """
                            record {
                                e8s : nat64;
                            }
                        """.trimIndent()
                    )
                )
            ),

            Arguments.of(
                """
                    type Account = record { owner : principal; subaccount : opt Subaccount };
                """.trimIndent(),
                IDLTypeDeclaration(
                    id = "Account",
                    type = IDLTypeRecord(
                        recordDeclaration = "record { owner : principal; subaccount : opt Subaccount }"
                    )
                )
            ),

            Arguments.of(
                """
                    // Timestamps are represented as nanoseconds from the UNIX epoch in UTC timezone
                    type TimeStamp = record {
                        timestamp_nanos: nat64;
                    };
                """.trimIndent(),
                IDLTypeDeclaration(
                    comment = IDLSingleLineComment(listOf("Timestamps are represented as nanoseconds from the UNIX epoch in UTC timezone")),
                    id = "TimeStamp",
                    type = IDLTypeRecord("""
                        record {
                            timestamp_nanos: nat64;
                        }
                    """.trimIndent())
                )
            ),

            Arguments.of(
                """
                    type TransferArg = record {
                        from_subaccount: opt blob; // The subaccount to transfer the token from
                        to : Account;
                        token_id : nat;
                        memo : opt blob;
                        created_at_time : opt nat64;
                    };
                """.trimIndent(),
                IDLTypeDeclaration(
                    id = "TransferArg",
                    type = IDLTypeRecord("""
                        record {
                            from_subaccount: opt blob; // The subaccount to transfer the token from
                            to : Account;
                            token_id : nat;
                            memo : opt blob;
                            created_at_time : opt nat64;
                        }
                    """.trimIndent())
                )
            ),

            Arguments.of(
                """
                    type QueryBlocksResponse = record {
                        // The total number of blocks in the chain.
                        // If the chain length is positive, the index of the last block is `chain_len - 1`.
                        chain_length : nat64;

                        // System certificate for the hash of the latest block in the chain.
                        // Only present if `query_blocks` is called in a non-replicated query context.
                        certificate : opt blob;

                        // List of blocks that were available in the ledger when it processed the call.
                        //
                        // The blocks form a contiguous range, with the first block having index
                        // [first_block_index] (see below), and the last block having index
                        // [first_block_index] + len(blocks) - 1.
                        //
                        // The block range can be an arbitrary sub-range of the originally requested range.
                        blocks : vec Block;

                        // The index of the first block in "blocks".
                        // If the blocks vector is empty, the exact value of this field is not specified.
                        first_block_index : BlockIndex;

                        // Encoding of instructions for fetching archived blocks whose indices fall into the
                        // requested range.
                        //
                        // For each entry `e` in [archived_blocks], `[e.from, e.from + len)` is a sub-range
                        // of the originally requested block range.
                        archived_blocks : vec record {
                            // The index of the first archived block that can be fetched using the callback.
                            start : BlockIndex;

                            // The number of blocks that can be fetched using the callback.
                            length : nat64;

                            // The function that should be called to fetch the archived blocks.
                            // The range of the blocks accessible using this function is given by [from]
                            // and [len] fields above.
                            callback : QueryArchiveFn;
                        };
                    };
                """.trimIndent(),
                IDLTypeDeclaration(
                    id = "QueryBlocksResponse",
                    type = IDLTypeRecord(
                        recordDeclaration = """
                            record {
                                // The total number of blocks in the chain.
                                // If the chain length is positive, the index of the last block is `chain_len - 1`.
                                chain_length : nat64;
        
                                // System certificate for the hash of the latest block in the chain.
                                // Only present if `query_blocks` is called in a non-replicated query context.
                                certificate : opt blob;
        
                                // List of blocks that were available in the ledger when it processed the call.
                                //
                                // The blocks form a contiguous range, with the first block having index
                                // [first_block_index] (see below), and the last block having index
                                // [first_block_index] + len(blocks) - 1.
                                //
                                // The block range can be an arbitrary sub-range of the originally requested range.
                                blocks : vec Block;
        
                                // The index of the first block in "blocks".
                                // If the blocks vector is empty, the exact value of this field is not specified.
                                first_block_index : BlockIndex;
        
                                // Encoding of instructions for fetching archived blocks whose indices fall into the
                                // requested range.
                                //
                                // For each entry `e` in [archived_blocks], `[e.from, e.from + len)` is a sub-range
                                // of the originally requested block range.
                                archived_blocks : vec record {
                                    // The index of the first archived block that can be fetched using the callback.
                                    start : BlockIndex;
        
                                    // The number of blocks that can be fetched using the callback.
                                    length : nat64;
        
                                    // The function that should be called to fetch the archived blocks.
                                    // The range of the blocks accessible using this function is given by [from]
                                    // and [len] fields above.
                                    callback : QueryArchiveFn;
                                };
                            }
                        """.trimIndent()
                    )
                )
            )
        )

        @JvmStatic
        private fun variantTypeDeclaration() = listOf(
            Arguments.of(
                """
                    type Transfer = variant {
                        Mint: record {
                            to: AccountIdentifier;
                            amount: Tokens;
                        };
                        Burn: record {
                             from: AccountIdentifier;
                             amount: Tokens;
                       };
                        Send: record {
                            from: AccountIdentifier;
                            to: AccountIdentifier;
                            amount: Tokens;
                        };
                    };
                """.trimIndent(),
                IDLTypeDeclaration(
                    id = "Transfer",
                    type = IDLTypeVariant(
                        variantDeclaration = """
                            variant {
                                Mint: record {
                                    to: AccountIdentifier;
                                    amount: Tokens;
                                };
                                Burn: record {
                                     from: AccountIdentifier;
                                     amount: Tokens;
                               };
                                Send: record {
                                    from: AccountIdentifier;
                                    to: AccountIdentifier;
                                    amount: Tokens;
                                };
                            }
                        """.trimIndent()
                    )
                )
            ),

            Arguments.of(
                """
                    type Transfer = variant {
                        // This is a comment
                        Mint: record {
                            // Second comment
                            to: AccountIdentifier;
                            amount: Tokens;
                        };
                    };
                """.trimIndent(),
                IDLTypeDeclaration(
                    id = "Transfer",
                    type = IDLTypeVariant(
                        variantDeclaration = """
                            variant {
                                // This is a comment
                                Mint: record {
                                    // Second comment
                                    to: AccountIdentifier;
                                    amount: Tokens;
                                };
                            }
                        """.trimIndent()
                    )
                )
            ),

            Arguments.of(
                """
                    // Generic value in accordance with ICRC-3
                    type Value = variant {
                        Blob : blob;
                        Text : text;
                        Nat : nat;
                        Int : int;
                        Array : vec Value;
                        Map : vec record { text; Value };
                    };
                """.trimIndent(),
                IDLTypeDeclaration(
                    comment = IDLSingleLineComment(listOf("Generic value in accordance with ICRC-3")),
                    id = "Value",
                    type = IDLTypeVariant("""
                        variant {
                            Blob : blob;
                            Text : text;
                            Nat : nat;
                            Int : int;
                            Array : vec Value;
                            Map : vec record { text; Value };
                        }
                    """.trimIndent())
                )
            ),

            Arguments.of(
                """
                    type TransferResult = variant {
                        Ok : nat; // Transaction index for successful transfer
                        Err : TransferError;
                    };
                """.trimIndent(),
                IDLTypeDeclaration(
                    id = "TransferResult",
                    type = IDLTypeVariant("""
                        variant {
                            Ok : nat; // Transaction index for successful transfer
                            Err : TransferError;
                        }
                    """.trimIndent())
                )
            )
        )
    }
}