sealed class RegexConstruct {
    abstract fun computeString(): String
}

internal sealed class GroupingConstruct(protected val content: RegexConstruct) : RegexConstruct() {
    protected abstract val groupPrefix: String

    override fun computeString() = "($groupPrefix${content.computeString()})"
    override fun toString() = "${this::class.simpleName}($content)"
}

internal data class RepetitionConstruct(
        private val construct: RegexConstruct,
        private val quantifierType: QuantifierType,
        private val repetitionType: RepetitionType
) : RegexConstruct() {
    override fun computeString() = "${construct.computeString()}${repetitionType.postfix}${quantifierType.operator}"
}

internal sealed class CompositionConstruct : RegexConstruct {

    protected val composition: MutableList<RegexConstruct>

    constructor(first: RegexConstruct, second: RegexConstruct) : super() {
        composition = mutableListOf(first, second)
    }

    constructor(constructs: List<RegexConstruct>) {
        require(constructs.size > 1)
        composition = constructs.toMutableList()
    }

    abstract val separator: String

    final override fun computeString(): String {
        return composition.joinToString(separator = separator) { it.computeString() }
    }

    override fun toString(): String {
        return "${this::class.simpleName}(${composition.joinToString(separator = separator) { it.toString() }})"
    }
}

internal class QueueConstruct(first: RegexConstruct, second: RegexConstruct) : CompositionConstruct(first, second) {
    override val separator = ""

    fun add(construct: RegexConstruct) {
        require(construct !is EmptyConstruct)
        if (construct is QueueConstruct) composition.addAll(construct.composition)
        else composition.add(construct)
    }
}

internal class AlternationConstruct : CompositionConstruct {
    constructor(alternative1: RegexConstruct, alternative2: RegexConstruct) : super(alternative1, alternative2)
    constructor(alternatives: List<RegexConstruct>) : super(alternatives)

    override val separator = "|"
}

internal object EmptyConstruct : RegexConstruct() {
    override fun computeString() = ""
    override fun toString() = "EmptyConstruct"
}

internal data class RawConstruct(private var value: String) : RegexConstruct() {
    override fun computeString(): String {
        return value.replace(metaCharacterRegex) { if (it.value == "\\") "\\\\\\\\" else "\\\\${it.value}" }
    }

    fun append(construct: RawConstruct) {
        value += construct.value
    }

    companion object {
        private val metaCharacterRegex = "[\\\\^$.|?*+()]".toRegex()
    }
}

internal data class SpecialCharacter(val value: Value) : RegexConstruct() {
    override fun computeString() = value.expression

    enum class Value(val expression: String) {
        LINE_START("^"),
        LINE_END("$"),
        STRING_START("\\\\A"),
        STRING_END("\\\\z"),
        LAST_LINE_END("\\\\Z"),
        PREVIOUS_MATCH("\\\\G"),
        ANY(".")
    }
}

internal data class ShorthandCharacterClass(private val value: Value) : RegexConstruct(), Negatable by NegatableDelegate() {
    override fun computeString() = if (isPositive) value.expression else value.negation

    enum class Value(val expression: String, val negation: String) {
        DIGIT("\\\\d", "\\\\D"),
        WHITESPACE("\\\\s", "\\\\S"),
        WORD("\\\\w", "\\\\W"), // [a-zA-Z0-9_], may vary with flavor
        WORD_BOUNDARY("\\\\b", "\\\\B"),
    }
}

internal data class PosixCharacterClass(val value: Value) : RegexConstruct() {
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

internal class CapturingGroup(construct: RegexConstruct, name: String? = null) : GroupingConstruct(construct) {
    override val groupPrefix = name?.let { "?<$it>:" }.orEmpty()
}

internal data class BackReferenceIndexCapture(private val index: Int) : RegexConstruct() {
    override fun computeString() = "\\\\k<$index>"
}

internal data class BackReferenceRelativeCapture(private val indexDifference: Int) : RegexConstruct() {
    override fun computeString() = "\\\\k<$indexDifference>"
}

internal data class BackReferenceNameCapture(private val name: String) : RegexConstruct() {
    override fun computeString() = "\\\\k<$name>"
}

internal class LookAround(
        private val direction: Direction,
        content: RegexConstruct
) : GroupingConstruct(content), Negatable by NegatableDelegate() {

    override val groupPrefix
        get() = when (direction) {
            Direction.AHEAD -> if (isPositive) "?=" else "?!"
            Direction.BEHIND -> if (isPositive) "?<=" else "?<!"
        }

    override fun toString() = "Look$direction(${if (isPositive) "" else "!"}$content)"

    enum class Direction { AHEAD, BEHIND }
}

internal class Group(construct: RegexConstruct) : GroupingConstruct(construct) {
    override val groupPrefix = "?:"
    override fun computeString(): String {
        return if (content is CompositionConstruct) super.computeString()
        else content.computeString() // Unnecessary grouping
    }
}

internal class FirstMatchGroup(construct: RegexConstruct) : GroupingConstruct(construct) {
    override val groupPrefix = "?>"
}

internal data class CharacterClass(private val characterClass: CharacterClassBuilder) : RegexConstruct(), Negatable by NegatableDelegate() {
    override fun computeString() = characterClass.build(isPositive)
}
