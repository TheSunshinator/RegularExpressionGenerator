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

internal data class PosixClass(val value: Value) : CharacterClassConstruct() {
    override fun computeString() = value.expression

    enum class Value(val expression: String) {
        LOWER_CASE("\\\\p{Lower}"),
        UPPER_CASE("\\\\p{Upper}"),
        ASCII("\\\\p{ASCII}"),
        LETTER("\\\\p{Alpha}"),
        ALPHANUMERIC("\\\\p{Alnum}"),
        SYMBOL("\\\\p{Punct}"),
        GRAPH("\\\\p{Graph}"),
        PRINT("\\\\p{Print}"),
        BLANK("\\\\p{Blank}"),
        CONTROL("\\\\p{Cntrl}"),
        HEXADECIMAL("\\\\p{XDigit}"),
    }
}

internal data class Intersection(private val construct: CharacterClassConstruct) : CharacterClassConstruct() {
    override fun computeString() = "&&${construct.computeString()}"
}

internal data class Subtraction(internal val construct: CharacterClassConstruct) : CharacterClassConstruct() {
    override fun computeString() = "&&[^${construct.computeString()}]"
}
