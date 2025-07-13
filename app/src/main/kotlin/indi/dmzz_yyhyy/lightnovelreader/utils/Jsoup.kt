package indi.dmzz_yyhyy.lightnovelreader.utils

import android.util.Log
import io.nightfish.potatoautoproxy.ProxyPool
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document
import java.io.IOException
import java.lang.Thread.sleep
import java.net.SocketException
import javax.net.ssl.SSLHandshakeException

fun Connection.autoReconnectionGet(lastReconnectTimes: Int = 3, lastReconnectTime: Int = 250, isUesProxy: Boolean = false): Document? {
    try {
        if (ProxyPool.enable && isUesProxy)
            ProxyPool.apply {
                return this@autoReconnectionGet.proxyGet()
            }
        else return this.get()
    } catch (e: HttpStatusException) {
        Log.e("Network", "failed to get data from ${e.url}")
        e.printStackTrace()
        if (lastReconnectTime > 1) {
            sleep(lastReconnectTime.toLong())
            this.autoReconnectionGet(
                lastReconnectTimes - 1,
                (lastReconnectTime * 1.3).toInt(),
                isUesProxy = true
            )
        } else
            return null
    } catch (e: SocketException) {
        Log.e("Network", "failed to get data")
        e.printStackTrace()
        if (lastReconnectTime > 1) {
            sleep(lastReconnectTime.toLong())
            this.autoReconnectionGet(
                lastReconnectTimes - 1,
                (lastReconnectTime * 1.3).toInt(),
                isUesProxy = true
            )
        } else
            return null
    } catch (e: SSLHandshakeException) {
        Log.e("Network", "failed to get data")
        e.printStackTrace()
        if (lastReconnectTime > 1) {
            sleep(lastReconnectTime.toLong())
            this.autoReconnectionGet(
                lastReconnectTimes - 1,
                (lastReconnectTime * 1.3).toInt(),
                isUesProxy = true
            )
        } else
            return null
    } catch (e: IOException) {
        Log.e("Network", "failed to get data")
        e.printStackTrace()
        if (lastReconnectTime > 1) {
            sleep(lastReconnectTime.toLong())
            this.autoReconnectionGet(
                lastReconnectTimes - 1,
                (lastReconnectTime * 1.3).toInt(),
                isUesProxy = true
            )
        } else
            return null
    }
    return null
}

fun Connection.autoReconnectionPost(lastReconnectTimes: Int = 3, lastReconnectTime: Int = 250, isUesProxy: Boolean = false): Document? {
    try {
        if (ProxyPool.enable && isUesProxy)
            ProxyPool.apply {
                return this@autoReconnectionPost.proxyPost()
            }
        else return this.post()
    } catch (e: HttpStatusException) {
        Log.e("Network", "failed to get data from ${e.url}")
        e.printStackTrace()
        if (lastReconnectTime > 1) {
            sleep(lastReconnectTime.toLong())
            this.autoReconnectionPost(
                lastReconnectTimes - 1,
                (lastReconnectTime * 1.3).toInt(),
                isUesProxy = true
            )
        } else
            return null
    } catch (e: SocketException) {
        Log.e("Network", "failed to get data")
        e.printStackTrace()
        if (lastReconnectTime > 1) {
            sleep(lastReconnectTime.toLong())
            this.autoReconnectionPost(
                lastReconnectTimes - 1,
                (lastReconnectTime * 1.3).toInt(),
                isUesProxy = true
            )
        } else
            return null
    } catch (e: SSLHandshakeException) {
        Log.e("Network", "failed to get data")
        e.printStackTrace()
        if (lastReconnectTime > 1) {
            sleep(lastReconnectTime.toLong())
            this.autoReconnectionPost(
                lastReconnectTimes - 1,
                (lastReconnectTime * 1.3).toInt(),
                isUesProxy = true
            )
        } else
            return null
    } catch (e: IOException) {
        Log.e("Network", "failed to get data")
        e.printStackTrace()
        if (lastReconnectTime > 1) {
            sleep(lastReconnectTime.toLong())
            this.autoReconnectionPost(
                lastReconnectTimes - 1,
                (lastReconnectTime * 1.3).toInt(),
                isUesProxy = true
            )
        } else
            return null
    }
    return null
}

fun Connection.autoReconnectionGetJsonText(): String? =
    this.ignoreContentType(true)
        .autoReconnectionGet()
        ?.outputSettings(
            Document.OutputSettings()
                .prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.xml)
        )
        ?.body()
        ?.text()