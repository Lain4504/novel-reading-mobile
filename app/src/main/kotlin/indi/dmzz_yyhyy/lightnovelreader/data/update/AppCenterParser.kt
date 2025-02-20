package indi.dmzz_yyhyy.lightnovelreader.data.update

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object AppCenterParser {
    private const val APP_CENTER_RELEASE = "https://api.appcenter.ms/v0.1/public/sdk/apps/f7743820-f7dc-498f-b31d-ec5032b0d66d/distribution_groups/bfcd55aa-302c-452a-b59e-90f065d437f5/releases/latest"
    private const val APP_CENTER_DEV = "https://api.appcenter.ms/v0.1/public/sdk/apps/f7743820-f7dc-498f-b31d-ec5032b0d66d/distribution_groups/f21a594f-5a56-4b2b-9361-9a734c10f1c9/releases/latest"
    private val gson = Gson()

    class AppCenterRelease(
        @SerializedName("version")
        override val version: Int,
        @SerializedName("short_version")
        override val versionName: String,
        @SerializedName("release_notes")
        override val releaseNotes: String,
        @SerializedName("download_url")
        override val downloadUrl: String
    ) : Release

    private fun parser(url: String) =
        gson.fromJson<AppCenterRelease>(
            Jsoup
                .connect(url)
                .ignoreContentType(true)
                .get()
                .outputSettings(
                    Document.OutputSettings()
                        .prettyPrint(false)
                        .syntax(Document.OutputSettings.Syntax.xml)
                )
                .toString()
                .replace("<html><head></head><body>", "")
                .replace("</body></html>", "")
                .replace("&amp;", "&"),
            object: TypeToken<AppCenterRelease>() {}.type
        )

    object ReleaseParser: UpdateParser {
        override fun parser(updatePhase: MutableStateFlow<String>): Release? = parser(APP_CENTER_RELEASE)
    }
    object DevelopmentParser: UpdateParser {
        override fun parser(updatePhase: MutableStateFlow<String>): Release? = parser(APP_CENTER_DEV)
    }
}