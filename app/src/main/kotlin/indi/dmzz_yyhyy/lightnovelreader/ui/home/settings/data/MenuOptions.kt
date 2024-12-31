package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data

sealed class MenuOptions(vararg options: Option) {
    private val _optionsList: MutableList<Option> = options.toMutableList()
    val optionsList: List<Option> get() = _optionsList.toList()
    fun option(key: String, name: String): String {
        _optionsList.add(Option(key, name))
        return key
    }
    class Option(
        val key: String,
        val name: String
    ) {
        override fun equals(other: Any?): Boolean = this.key == other
        override fun hashCode(): Int = key.hashCode()
    }
    fun get(key: String): Option = _optionsList.first { it.equals(key) }

    data object UpdateChannelOptions: MenuOptions(
        Option("Release", "正式发布版"),
        Option("Development", "开发版"),
    )

    data object UpdatePlatformOptions: MenuOptions(
        Option("GitHub", "GitHub"),
        Option("AppCenter", "Microsoft App Center"),
        /*Options("LNR_API", "yukonisen\'s LightNovelReader API"),*/
    )

    data object DarkModeOptions: MenuOptions(
        Option("FollowSystem", "跟随系统"),
        Option("Enabled", "覆盖启用"),
        Option("Disabled", "覆盖禁用")
    )

    data object AppLocaleOptions: MenuOptions(
        Option("zh-CN", "简体中文 (中国大陆)"),
        Option("zh-HK", "繁體中文 (香港)"),
        Option("zh-TW", "繁體中文 (臺灣)"),
        Option("ja-JP", "日本語"),
        Option("ko-kr", "한국어 (대한민국)"),
        Option("ko-kp", "한국어 (조선민주주의인민공화국)")
    )

    data object FlipAnimeOptions: MenuOptions() {
        val None = option("none", "无")
        val ScrollWithoutShadow = option("scroll", "滚动")
    }
}