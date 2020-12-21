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

    fun lowerCase() {
        +PosixClass(PosixClass.Value.LOWER_CASE)
    }

    fun upperCase() {
        +PosixClass(PosixClass.Value.UPPER_CASE)
    }

    fun ascii() {
        +PosixClass(PosixClass.Value.ASCII)
    }

    fun letter() {
        +PosixClass(PosixClass.Value.LETTER)
    }

    fun alphanumeric() {
        +PosixClass(PosixClass.Value.ALPHANUMERIC)
    }

    fun symbol() {
        +PosixClass(PosixClass.Value.SYMBOL)
    }

    fun graphical() {
        +PosixClass(PosixClass.Value.GRAPH)
    }

    fun printable() {
        +PosixClass(PosixClass.Value.PRINT)
    }

    fun blank() {
        +PosixClass(PosixClass.Value.BLANK)
    }

    fun control() {
        +PosixClass(PosixClass.Value.CONTROL)
    }

    fun hexadecimalDigit() {
        +PosixClass(PosixClass.Value.HEXADECIMAL)
    }

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