package io.lain4504.novelreadingapp.api.web.explore

import io.lain4504.novelreadingapp.api.explore.ExplorePage

interface ExplorePageDataSource {
    val title: String
    fun getExplorePage(): ExplorePage
}