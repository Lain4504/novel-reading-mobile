package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data

import indi.dmzz_yyhyy.lightnovelreader.R

sealed class ObjectOptions(vararg options: Option) {
    val optionsList: List<Option> = options.toList()

    open class Option(
        val key: String,
        val name: Int,
        val description: Int,
        val url: String?
    ) {
        override fun equals(other: Any?): Boolean = this.key == (other as? String)
        override fun hashCode(): Int = key.hashCode()
    }

    data object GitHubProxyUrlOptions: ObjectOptions(
        Option("disabled", R.string.key_proxy_disabled, R.string.key_proxy_disabled_desc, ""),
        Option("gh-proxy.com", R.string.key_proxy_gh_proxy_com, R.string.key_proxy_3rd_party_service_desc, "https://gh-proxy.com/"),
        Option("ghgo.xyz", R.string.key_proxy_ghgo_xyz, R.string.key_proxy_3rd_party_service_desc, "https://ghgo.xyz/"),
        Option("custom", R.string.key_proxy_custom, R.string.key_proxy_custom_desc, null),
    )
}
