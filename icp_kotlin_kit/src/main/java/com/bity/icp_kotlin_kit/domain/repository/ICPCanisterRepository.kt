package com.bity.icp_kotlin_kit.domain.repository

import com.bity.icp_candid.domain.model.CandidValue
import com.bity.icp_kotlin_kit.domain.model.ICPMethod
import com.bity.icp_kotlin_kit.domain.model.ICPPrincipal

interface ICPCanisterRepository {
    suspend fun query(
        method: ICPMethod,
        // method contains canister
        // canister: ICPPrincipal,
        // TODO sender
    ): CandidValue
}