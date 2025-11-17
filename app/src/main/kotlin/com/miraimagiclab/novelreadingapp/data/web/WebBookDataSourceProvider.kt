package com.miraimagiclab.novelreadingapp.data.web

import io.lain4504.novelreadingapp.api.web.WebBookDataSource

interface WebBookDataSourceProvider {
    val value: WebBookDataSource
}

class MutableWebDataSourceProvider(): WebBookDataSourceProvider {
    override var value: WebBookDataSource = EmptyWebDataSource
}