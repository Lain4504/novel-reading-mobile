package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data

import indi.dmzz_yyhyy.lightnovelreader.R

sealed class MenuOptions(vararg options: Option) {
    private val _optionList: MutableList<Option> = options.toMutableList()
    val optionList: List<Option> get() = _optionList.toList()
    fun option(key: String, nameId: Int): String {
        _optionList.add(Option(key, nameId))
        return key
    }
    class Option(
        val key: String,
        val nameId: Int
    ) {
        override fun equals(other: Any?): Boolean = this.key == other
        override fun hashCode(): Int = key.hashCode()
    }
    fun get(key: String): Option = _optionList.first { it.equals(key) }

    data object UpdateChannelOptions: MenuOptions(
        Option("Release", R.string.key_update_channel_release),
        Option("Development", R.string.key_update_channel_development)
    )

    data object UpdatePlatformOptions: MenuOptions(
        Option("GitHub", R.string.key_platform_github),
        Option("AppCenter", R.string.key_platform_appcenter),
        /*Options("LNR_API", R.string.key_platform_lnr_api)*/
    )

    data object DarkModeOptions: MenuOptions(
        Option("FollowSystem", R.string.key_dark_mode_follow_system),
        Option("Enabled", R.string.key_dark_mode_enabled),
        Option("Disabled", R.string.key_dark_mode_disabled)
    )

    data object AppLocaleOptions: MenuOptions(
        Option("zh-CN", R.string.key_locale_zh_cn),
        Option("zh-HK", R.string.key_locale_zh_hk),
        Option("zh-TW", R.string.key_locale_zh_tw),
        Option("ja-JP", R.string.key_locale_ja_jp),
        Option("ko-kr", R.string.key_locale_ko_kr),
        Option("ko-kp", R.string.key_locale_ko_kp)
    )

    data object ReaderBgImageDisplayModeOptions: MenuOptions() {
        val Fixed = option("fixed", R.string.key_bg_image_display_mode_fixed)
        val Loop = option("loop", R.string.key_bg_image_display_mode_loop)
    }

    data object FlipAnimationOptions: MenuOptions() {
        val None = option("none", R.string.key_flip_animation_none)
        val ScrollWithoutShadow = option("scroll", R.string.key_flip_animation_scroll)
    }

    data object SelectImage: MenuOptions() {
        val Default = option("default", R.string.key_default_image)
        val Customize = option("customize", R.string.key_customize_image)
    }
}