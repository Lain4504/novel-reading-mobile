package io.lain4504.novelreadingapp.api.userdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class UserData<T> (
    open val path: String
) {
    val group get() = path.split(".").dropLast(1).joinToString(".")
    /**
     * Hàm này là đồng bộ, không nên gọi trong giai đoạn khởi tạo hoặc trên main thread
     */
    abstract fun set(value: T)
    fun asynchronousSet(value: T) {
        CoroutineScope(Dispatchers.IO).launch {
            set(value)
        }
    }
    /**
     * Hàm này là đồng bộ, không nên gọi trong giai đoạn khởi tạo hoặc trên main thread
     */
    abstract fun get(): T?
    abstract fun getFlow(): Flow<T?>
    fun getFlowWithDefault(default: T): Flow<T> = getFlow().map { it ?: default }
    /**
     * Hàm này là đồng bộ, không nên gọi trong giai đoạn khởi tạo hoặc trên main thread
     */
    fun getOrDefault(default: T): T {
        return get() ?: default
    }
    /**
     * Hàm này là đồng bộ, không nên gọi trong giai đoạn khởi tạo hoặc trên main thread
     */
    fun update(updater: (T) -> T, default: T) {
        set(updater(getOrDefault(default)))
    }
}