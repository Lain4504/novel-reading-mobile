package indi.dmzz_yyhyy.lightnovelreader.data.web

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WebDataSource(
    val name: String,
    val provider: String
)
