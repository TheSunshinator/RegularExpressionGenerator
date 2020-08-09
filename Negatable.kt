interface Negatable {
    val isPositive: Boolean
    operator fun not()
}

internal class NegatableDelegate : Negatable {
    private var _isPositive = true
    override val isPositive: Boolean
        get() = _isPositive

    override fun not() {
        _isPositive = !isPositive
    }
}