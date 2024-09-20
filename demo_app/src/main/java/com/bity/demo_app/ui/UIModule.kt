package com.bity.demo_app.ui

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.bity.demo_app.ui.icp_balance.ICPBalanceViewModel
import com.bity.demo_app.ui.tokens_balance.TokensBalanceViewModel
import com.bity.demo_app.ui.icp_tokens.ICPTokensViewModel

val uiModule = module {

    viewModelOf(::ICPBalanceViewModel)
    viewModelOf(::TokensBalanceViewModel)
    viewModelOf(::ICPTokensViewModel)

}