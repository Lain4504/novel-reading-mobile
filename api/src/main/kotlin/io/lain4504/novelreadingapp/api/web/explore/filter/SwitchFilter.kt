package io.lain4504.novelreadingapp.api.web.explore.filter

abstract class SwitchFilter(
    private var title: String,
    private var onChange: () -> Unit
): Filter() {
    var enabled = false
        set(value) {
            field = value
            onChange.invoke()
        }

    override fun getType(): FilterTypes = FilterTypes.SWITCH
    override fun getTitle(): String = title
}