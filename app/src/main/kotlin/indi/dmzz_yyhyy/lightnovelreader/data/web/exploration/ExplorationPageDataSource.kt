package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration

import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage

interface ExplorationPageDataSource {
    val title: String
    fun getExplorationPage(): ExplorationPage
}