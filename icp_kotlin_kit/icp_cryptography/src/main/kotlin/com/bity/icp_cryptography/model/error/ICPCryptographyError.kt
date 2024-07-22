package com.bity.icp_cryptography.model.error

sealed class ICPCryptographyError(errorMessage: String? = null): Error(errorMessage) {

    sealed class ICPCRC32Error(errorMessage: String? = null): ICPCryptographyError(errorMessage) {
        class InvalidChecksum: ICPCRC32Error()
    }
}