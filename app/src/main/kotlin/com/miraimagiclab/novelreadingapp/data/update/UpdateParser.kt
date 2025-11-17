package com.miraimagiclab.novelreadingapp.data.update

import kotlinx.coroutines.flow.MutableStateFlow

interface UpdateParser {
    fun parser(updatePhase: MutableStateFlow<String>): Release?
}