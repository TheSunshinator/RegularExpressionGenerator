@file:Include("CharacterClassBuilder.kt")
@file:Include("QuantifierType.kt")

class RegexBuilder(builder: RegexBuilder.() -> Unit) {

    private var regex = ""
    private val specialCharacters = arrayOf('\\', '\t', '\n', '\r', '.')

    init {
        builder()
    }

    fun character(char: Char) {
        when (char) {
            '\t' -> tab()
            '\n' -> newLine()
            '\r' -> carriageReturn()
            '\\' -> regex += "\\\\"
            '"' -> regex += "\\\""
            '.',
            '*',
            '+',
            '?',
            '^',
            '[',
            ']',
            '$',
            '&',
            '|' -> regex += char.escape()
            else -> regex += char
        }
    }

    fun tab() {
        regex += "\\t"
    }

    fun newLine() {
        regex += "\\n"
    }

    fun carriageReturn() {
        regex += "\\r"
    }

    fun any() {
        regex += '.'
    }

    fun digit() {
        regex += "\\\\d"
    }

    fun nonDigit() {
        regex += "\\\\D"
    }

    fun whitespace() {
        regex += "\\\\s"
    }

    fun nonWhitespace() {
        regex += "\\\\S"
    }

    fun word() { // [a-zA-Z_0-9]
        regex += "\\\\w"
    }

    fun nonWord() {
        regex += "\\\\W"
    }

    fun lowerCase() {
        regex += "\\\\p{Lower}"
    }

    fun upperCase() {
        regex += "\\\\p{Upper}"
    }

    fun ascii() {
        regex += "\\\\p{ASCII}"
    }

    fun letter() {
        regex += "\\\\p{Alpha}"
    }

    fun alphaNumeric() {
        regex += "\\\\p{Alnum}"
    }

    fun symbol() {
        regex += "\\\\p{Punct}"
    }

    fun letterOrNumberOrSymbol() {
        regex += "\\\\p{Graph}"
    }

    fun letterOrNumberOrSymbolOrSpace() {
        regex += "\\\\p{Print}"
    }

    fun blank() {
        regex += "\\\\p{Blank}"
    }

    fun control() {
        regex += "\\\\p{Cntrl}"
    }

    fun hexdecimalDigit() {
        regex += "\\\\p{XDigit}"
    }

    fun lineStart() {
        regex += '^'
    }

    fun lineEnd() {
        regex += '$'
    }

    fun wordBoundary() {
        regex += "\\\\b"
    }

    fun nonWordBoundary() {
        regex += "\\\\B"
    }

    fun stringStart() {
        regex += "\\\\A"
    }

    fun stringEnd() {
        regex += "\\\\z"
    }

    fun lastLineEnd() {
        regex += "\\\\Z"
    }

    fun previousMatch() {
        regex += "\\\\G"
    }

    fun string(s: String) {
        s.forEach(::character)
    }

    fun capture(name: String? = null, builder: RegexBuilder.() -> Unit) {
        regex += if (name.isNullOrEmpty()) "(${RegexBuilder(builder)})" else "(?<$name>:${RegexBuilder(builder)})"
    }

    fun getCapture(index: Int) {
        regex += "\\\\$index"
    }

    fun getCapture(name: String) {
        regex += "\\\\k<$name>"
    }

    fun group(builder: RegexBuilder.() -> Unit) {
        regex += RegexBuilder(builder).build().grouped()
    }

    private fun String.grouped() = "(?:$this)"

    fun followedBy(builder: RegexBuilder.() -> Unit) {
        regex += "(?=${RegexBuilder(builder).build()})"
    }

    fun notFollowedBy(builder: RegexBuilder.() -> Unit) {
        regex += "(?!${RegexBuilder(builder).build()})"
    }

    fun precededBy(builder: RegexBuilder.() -> Unit) {
        regex += "(?<=${RegexBuilder(builder).build()})"
    }

    fun notPrecededBy(builder: RegexBuilder.() -> Unit) {
        regex += "(?<!${RegexBuilder(builder).build()})"
    }

    fun independentGroup(builder: RegexBuilder.() -> Unit) {
        regex += "(?>${RegexBuilder(builder).build()})"
    }

    fun maybe(quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        val expression = RegexBuilder(builder).build().groupIfNeeded()
        if (expression.isNotEmpty()) regex += expression + '?' + quantifierType.operator
    }

    private fun String.isSingleCharacterSet() = length > 2 && first() == '[' && last() == ']' && count { it == '[' || it == ']' } == 2
    private fun String.groupIfNeeded() = if (length < 2 || isSingleCharacterSet()) this else grouped()

    fun atLeastOnce(quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        val expression = RegexBuilder(builder).build().groupIfNeeded()
        if (expression.isNotEmpty()) regex += expression + '+' + quantifierType.operator
    }

    fun anyNumberOfTimes(quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        val expression = RegexBuilder(builder).build().groupIfNeeded()
        if (expression.isNotEmpty()) regex += expression + '*' + quantifierType.operator
    }

    fun repeatedExactly(times: Int, quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        val expression = RegexBuilder(builder).build().groupIfNeeded()
        if (expression.isNotEmpty()) regex += expression + "{$times}" + quantifierType.operator
    }

    fun repeatedAtLeast(times: Int, quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        val expression = RegexBuilder(builder).build().groupIfNeeded()
        if (expression.isNotEmpty()) regex += expression + "{$times,}" + quantifierType.operator
    }

    fun repeatedBetween(minTimes: Int, maxTimes: Int, quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        require(maxTimes > minTimes)
        val expression = RegexBuilder(builder).build().groupIfNeeded()
        if (expression.isNotEmpty()) regex += expression + "{$minTimes,$maxTimes}" + quantifierType.operator
    }

    fun characterClass(builder: CharacterClassBuilder.() -> Unit) {
        val expression = CharacterClassBuilder(
                positive = true,
                bracketsMandatory = false,
                builder = builder
        ).build()
        regex += expression
    }

    fun notCharacterClass(builder: CharacterClassBuilder.() -> Unit) {
        val expression = CharacterClassBuilder(
                positive = false,
                bracketsMandatory = true,
                builder = builder
        ).build()
        regex += expression
    }

    fun build(): String = regex
}

fun Char.escape() = "\\$this"

fun regex(builder: RegexBuilder.() -> Unit): String = RegexBuilder(builder).build()