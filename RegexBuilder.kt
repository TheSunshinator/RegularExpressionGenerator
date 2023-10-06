class RegexBuilder constructor(builder: RegexBuilder.() -> Unit) {

    var regexConstruct: RegexConstruct = EmptyConstruct

    init {
        builder()
    }

    operator fun RegexConstruct.unaryPlus() {
        if (this !is EmptyConstruct) regexConstruct = when (val currentConstruct = regexConstruct) {
            EmptyConstruct -> this
            is QueueConstruct -> {
                val constructToAdd = if (this is AlternationConstruct) Group(this) else this
                currentConstruct.apply { add(constructToAdd) }
            }
            is AlternationConstruct -> {
                val constructToAdd = if (this is AlternationConstruct) Group(this) else this
                QueueConstruct(currentConstruct.groupIfNeeded(), constructToAdd)
            }
            else -> if (currentConstruct is RawConstruct && this is RawConstruct) currentConstruct.apply {
                append(this@unaryPlus)
            } else {
                val constructToAdd = if (this is AlternationConstruct) Group(this) else this
                val currentConstructGrouped = if (currentConstruct is AlternationConstruct) Group(currentConstruct) else currentConstruct
                QueueConstruct(currentConstructGrouped, constructToAdd)
            }
        }
    }

    operator fun String.unaryPlus() = string(this)
    operator fun Char.unaryPlus() = character(this)

    fun string(s: String) = +RawConstruct(s)
    fun character(char: Char) = string(char.toString())

    fun tab() = character('\t')
    fun newLine() = string("\\n")
    fun carriageReturn() = character('\r')

    fun any() = +SpecialCharacter(SpecialCharacter.Value.ANY)
    fun lineStart() = +SpecialCharacter(SpecialCharacter.Value.LINE_START)
    fun lineEnd() = +SpecialCharacter(SpecialCharacter.Value.LINE_END)
    fun stringStart() = +SpecialCharacter(SpecialCharacter.Value.STRING_START)
    fun stringEnd() = +SpecialCharacter(SpecialCharacter.Value.STRING_END)
    fun lastLineEnd() = +SpecialCharacter(SpecialCharacter.Value.LAST_LINE_END)
    fun previousMatch() = +SpecialCharacter(SpecialCharacter.Value.PREVIOUS_MATCH)

    fun digit(): Negatable = ShorthandCharacterClass(ShorthandCharacterClass.Value.DIGIT).also { +it }
    fun whitespace(): Negatable = ShorthandCharacterClass(ShorthandCharacterClass.Value.WHITESPACE).also { +it }
    fun word(): Negatable = ShorthandCharacterClass(ShorthandCharacterClass.Value.WORD).also { +it }
    fun wordBoundary(): Negatable = ShorthandCharacterClass(ShorthandCharacterClass.Value.WORD_BOUNDARY).also { +it }

    fun lowerCase() = +PosixCharacterClass(PosixCharacterClass.Value.LOWER_CASE)
    fun upperCase() = +PosixCharacterClass(PosixCharacterClass.Value.UPPER_CASE)
    fun ascii() = +PosixCharacterClass(PosixCharacterClass.Value.ASCII)
    fun letter() = +PosixCharacterClass(PosixCharacterClass.Value.LETTER)
    fun alphanumeric() = +PosixCharacterClass(PosixCharacterClass.Value.ALPHANUMERIC)
    fun symbol() = +PosixCharacterClass(PosixCharacterClass.Value.SYMBOL)
    fun graphical() = +PosixCharacterClass(PosixCharacterClass.Value.GRAPH)
    fun printable() = +PosixCharacterClass(PosixCharacterClass.Value.PRINT)
    fun blank() = +PosixCharacterClass(PosixCharacterClass.Value.BLANK)
    fun control() = +PosixCharacterClass(PosixCharacterClass.Value.CONTROL)
    fun hexadecimalDigit() = +PosixCharacterClass(PosixCharacterClass.Value.HEXADECIMAL)

    fun capture(name: String? = null, builder: RegexBuilder.() -> Unit) = +CapturingGroup(
            RegexBuilder(builder).regexConstruct,
            name
    )

    fun referenceCapture(name: String) = +BackReferenceNameCapture(name)
    fun referenceCapture(index: Int) = +BackReferenceIndexCapture(index)
    fun referenceLastCapture(indexFromLast: Int) = +BackReferenceRelativeCapture(indexFromLast)

    fun subRegex(builder: RegexBuilder.() -> Unit): RegexConstruct = RegexBuilder(builder).regexConstruct


    fun ahead(builder: RegexBuilder.() -> Unit): Negatable {
        return LookAround(LookAround.Direction.AHEAD, RegexBuilder(builder).regexConstruct).also { +it }
    }

    fun behind(builder: RegexBuilder.() -> Unit): Negatable {
        return LookAround(LookAround.Direction.BEHIND, RegexBuilder(builder).regexConstruct).also { +it }
    }

    fun maybe(quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        +repeatConstructOrEmpty(RegexBuilder(builder).regexConstruct, quantifierType, RepetitionType.Optional)
    }

    private fun repeatConstructOrEmpty(
            construct: RegexConstruct,
            quantifierType: QuantifierType,
            repetitionType: RepetitionType
    ): RegexConstruct {
        return if (construct === EmptyConstruct) EmptyConstruct
        else RepetitionConstruct(
                construct.groupIfNeeded(),
                quantifierType,
                repetitionType
        )
    }

    private fun RegexConstruct.groupIfNeeded(): RegexConstruct {
        return if (this is CompositionConstruct || (this is RawConstruct && isGroup)) Group(this) else this
    }

    fun atLeastOnce(quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        +repeatConstructOrEmpty(RegexBuilder(builder).regexConstruct, quantifierType, RepetitionType.AtLeastOnce)
    }

    fun anyNumberOfTimes(quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        +repeatConstructOrEmpty(RegexBuilder(builder).regexConstruct, quantifierType, RepetitionType.Any)
    }

    fun exactly(times: Int, quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        +repeatConstructOrEmpty(RegexBuilder(builder).regexConstruct, quantifierType, RepetitionType.Exactly(times))
    }

    fun atLeast(times: Int, quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        +repeatConstructOrEmpty(RegexBuilder(builder).regexConstruct, quantifierType, RepetitionType.AtLeast(times))
    }

    fun atMost(times: Int, quantifierType: QuantifierType = QuantifierType.GREEDY, builder: RegexBuilder.() -> Unit) {
        +repeatConstructOrEmpty(RegexBuilder(builder).regexConstruct, quantifierType, RepetitionType.AtMost(times))
    }

    fun repeatedBetween(
            minTimes: Int,
            maxTimes: Int,
            quantifierType: QuantifierType = QuantifierType.GREEDY,
            builder: RegexBuilder.() -> Unit
    ) {
        require(minTimes < maxTimes)
        +repeatConstructOrEmpty(RegexBuilder(builder).regexConstruct, quantifierType, RepetitionType.Range(minTimes, maxTimes))
    }

    fun repeatedBetween(
            range: IntRange,
            quantifierType: QuantifierType = QuantifierType.GREEDY,
            builder: RegexBuilder.() -> Unit
    ) {
        +repeatConstructOrEmpty(RegexBuilder(builder).regexConstruct, quantifierType, RepetitionType.Range(range.first, range.last))
    }

    fun firstMatch(builder: RegexBuilder.() -> Unit) = +FirstMatchGroup(RegexBuilder(builder).regexConstruct)

    fun anySubRegexIn(alternatives: AlternationBuilder.() -> Unit) {
        +AlternationBuilder(alternatives).build()
    }

    fun anyCharacterInSet(builder: CharacterClassBuilder.() -> Unit): Negatable {
        return CharacterClass(CharacterClassBuilder(builder)).also { +it }
    }

    fun build(): String = regexConstruct.regexRepresentation
}

fun regex(builder: RegexBuilder.() -> Unit): Regex = RegexBuilder(builder)
        .build()
        .also { println("\"$it\"") }
        .replace("\\\\", "\\")
        .also { println("\"$it\"") }
        .toRegex()