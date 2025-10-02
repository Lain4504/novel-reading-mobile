package indi.dmzz_yyhyy.lightnovelreader.data.web

import io.nightfish.lightnovelreader.api.web.WebBookDataSource

interface WebBookDataSourceProvider {
    val value: WebBookDataSource
}

class MutableWebDataSourceProvider(): WebBookDataSourceProvider {
    override var value: WebBookDataSource = EmptyWebDataSource
}