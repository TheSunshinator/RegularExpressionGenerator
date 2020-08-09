class CharacterClassBuilder internal constructor(builder: CharacterClassBuilder.() -> Unit) {

    private val isValid: Boolean
        get() = characterClassConstruct !== EmptyCharacterClassConstruct
    private var characterClassConstruct: CharacterClassConstruct = EmptyCharacterClassConstruct

    init {
        builder()
    }

    operator fun CharacterClassConstruct.unaryPlus() {
        characterClassConstruct = when (val currentConstruct = characterClassConstruct) {
            EmptyCharacterClassConstruct -> {
                check(this !is Intersection) { "Cannot start character class expression with an intersection" }
                this
            }
            is CharacterSet -> currentConstruct.apply {
                add(this@unaryPlus)
            }
            else -> if (currentConstruct is RawCharacters && this is RawCharacters) currentConstruct.apply {
                append(this@unaryPlus)
            } else CharacterSet(currentConstruct, this)
        }
    }

    operator fun Char.unaryPlus() = character(this)
    operator fun String.unaryPlus() = allIn(this)
    operator fun CharRange.unaryPlus() = allIn(this)

    fun character(char: Char) = allIn(char.toString())
    fun allIn(s: String) = +RawCharacters(s)
    fun allIn(range: CharRange) = +CharacterRange(range)

    fun tab() = character('\t')
    fun newLine() = character('\n')
    fun carriageReturn() = character('\r')

    fun lowerCase(): Negatable = PosixClass(PosixClass.Value.LOWER_CASE).also { +it }
    fun upperCase(): Negatable = PosixClass(PosixClass.Value.UPPER_CASE).also { +it }
    fun ascii(): Negatable = PosixClass(PosixClass.Value.ASCII).also { +it }
    fun letter(): Negatable = PosixClass(PosixClass.Value.LETTER).also { +it }
    fun alphanumeric(): Negatable = PosixClass(PosixClass.Value.ALPHANUMERIC).also { +it }
    fun symbol(): Negatable = PosixClass(PosixClass.Value.SYMBOL).also { +it }
    fun graphical(): Negatable = PosixClass(PosixClass.Value.GRAPH).also { +it }
    fun printable(): Negatable = PosixClass(PosixClass.Value.PRINT).also { +it }
    fun blank(): Negatable = PosixClass(PosixClass.Value.BLANK).also { +it }
    fun control(): Negatable = PosixClass(PosixClass.Value.CONTROL).also { +it }
    fun hexadecimalDigit(): Negatable = PosixClass(PosixClass.Value.HEXADECIMAL).also { +it }

    fun exclude(builder: CharacterClassBuilder.() -> Unit) {
        +Subtraction(CharacterClassBuilder(builder).characterClassConstruct)
    }

    fun intersectWith(builder: CharacterClassBuilder.() -> Unit) {
        +Intersection(CharacterClassBuilder(builder).characterClassConstruct)
    }

    fun build(isPositive: Boolean): String {
        val characterClassConstruct = characterClassConstruct
        return when {
            !isValid -> {
                println("`${characterClassConstruct.computeString()}` is not a valid character class")
                ""
            }
            characterClassConstruct is RawCharacters && !characterClassConstruct.isUseful -> RegexBuilder {
                string(characterClassConstruct.value)
            }.build()
            isPositive -> "[${characterClassConstruct.computeString()}]"
            else -> "[^${characterClassConstruct.computeString()}]"
        }
    }
}