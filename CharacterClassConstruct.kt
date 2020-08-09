sealed class CharacterClassConstruct {
    abstract fun computeString(): String
}

internal sealed class NegatableCharacterClass : CharacterClassConstruct(), Negatable by NegatableDelegate()

internal data class RawCharacters(internal var value: String) : CharacterClassConstruct() {
    override fun computeString(): String {
        return value.replace(metaCharacterRegex) { if (it.value == "\\") "\\\\\\\\" else "\\\\${it.value}" }
    }

    val isUseful: Boolean
        get() = value.length > 1

    fun append(construct: RawCharacters) {
        value += construct.value
    }

    companion object {
        private val metaCharacterRegex = "(?<!\\A)-(?!\\z)|(?<!\\A)]|(?<=\\A)\\^|\\\\".toRegex()
    }
}

internal object EmptyCharacterClassConstruct : CharacterClassConstruct() {
    override fun computeString() = ""
}

internal class CharacterSet(first: CharacterClassConstruct, second: CharacterClassConstruct) : NegatableCharacterClass() {
    private val characters = mutableListOf(first, second)

    fun add(construct: CharacterClassConstruct) {
        characters.add(construct)
    }

    override fun computeString() = characters.joinToString(
            separator = ""
    ) { it.computeString() }
}

internal class CharacterRange(range: CharRange) : CharacterClassConstruct() {
    private val value = "${range.first}-${range.last}"
    override fun computeString() = value
}

internal data class PosixClass(val value: Value) : NegatableCharacterClass() {
    override fun computeString() = if (isPositive) value.expression else value.negated

    enum class Value(val expression: String, val negated: String) {
        ALPHANUMERIC("[:alnum:]", "[:^alnum:]"),
        LETTER("[:alpha:]", "[:^alpha:]"),
        ASCII("[:ascii:]", "[:^ascii:]"),
        BLANK("[:blank:]", "[:^blank:]"),
        CONTROL("[:cntrl:]", "[:^cntrl:]"),
        GRAPH("[:graph:]", "[:^graph:]"),
        LOWER_CASE("[:lower:]", "[:^lower:]"),
        UPPER_CASE("[:upper:]", "[:^upper:]"),
        PRINT("[:print:]", "[:^print:]"),
        SYMBOL("[:punct:]", "[:^punct:]"),
        HEXADECIMAL("[:xdigit:]", "[:^xdigit:]"),
    }
}

internal data class Intersection(private val construct: CharacterClassConstruct) : CharacterClassConstruct() {
    override fun computeString() = "&&${construct.computeString()}"
}

internal data class Subtraction(internal val construct: CharacterClassConstruct) : CharacterClassConstruct() {
    override fun computeString() = "&&[^${construct.computeString()}]"
}
