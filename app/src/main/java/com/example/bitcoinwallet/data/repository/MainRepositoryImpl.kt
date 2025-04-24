package com.example.bitcoinwallet.data.repository

import com.example.bitcoinwallet.data.base.BaseRepository
import com.example.bitcoinwallet.features.main.domain.MainRepository
import com.example.bitcoinwallet.network.api.Api

class MainRepositoryImpl(
    api: Api
) : MainRepository, BaseRepository(){

}