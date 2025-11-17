package com.miraimagiclab.novelreadingapp.utils

import android.util.Log
import kotlinx.coroutines.delay
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.SocketException
import javax.net.ssl.SSLHandshakeException

suspend fun Connection.autoReconnectionGet(reconnectTime: Int = 5, reconnectDelay: Long = 250): Document? =
    autoReconnection(
        reconnectTime,
        reconnectDelay,
        { this.get() }
    )

suspend fun Connection.autoReconnectionPost(reconnectTime: Int = 5, reconnectDelay: Long = 250): Document? =
    autoReconnection(
        reconnectTime,
        reconnectDelay,
        { this.post() }
    )

private suspend fun autoReconnection(
    reconnectTime: Int = 3,
    reconnectDelay: Long = 250,
    block: suspend () -> Document?
): Document? {
    try {
        return block.invoke()
    } catch (e: HttpStatusException) {
        Log.e("Network", "failed to get data from ${e.url}, last reconnection times: $reconnectTime")
        e.printStackTrace()
        return retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, (reconnectDelay * 2), block)
        }
    } catch (e: SocketException) {
        Log.e("Network", "failed to get data from ${e.cause}, last reconnection times: $reconnectTime")
        e.printStackTrace()
        return retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, (reconnectDelay * 2), block)
        }
    } catch (e: SSLHandshakeException) {
        Log.e("Network", "failed to get data, last reconnection times: $reconnectTime")
        e.printStackTrace()
        return retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, (reconnectDelay * 2), block)
        }
    } catch (e: IOException) {
        Log.e("Network", "failed to get data, last reconnection times:  $reconnectTime")
        e.printStackTrace()
        return retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, (reconnectDelay * 2), block)
        }
    }
}

private suspend fun retry(reconnectTimes: Int, reconnectDelay: Long, block: suspend () -> Document?): Document? {
    if (reconnectTimes < 1) return null
    delay(reconnectDelay)
    return block.invoke()
}

suspend fun Connection.autoReconnectionGetJsonText(): String? =
    this.ignoreContentType(true)
        .autoReconnectionGet()
        ?.outputSettings(
            Document.OutputSettings()
                .prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.xml)
        )
        ?.body()
        ?.text()