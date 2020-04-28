class CharacterClassBuilder(
        private val positive: Boolean,
        private val bracketsMandatory: Boolean = false,
        builder: CharacterClassBuilder.() -> Unit
) {

    private var characterClass = ""

    init {
        builder()
    }

    fun character(char: Char) {
        characterClass += char
    }

    fun characterIn(range: CharRange) {
        characterClass += "${range.first}-${range.endInclusive}"
    }

    fun characterClass(builder: CharacterClassBuilder.() -> Unit) {
        characterClass += CharacterClassBuilder(
                positive = true,
                bracketsMandatory = false,
                builder = builder
        ).build()
    }

    fun notCharacterClass(builder: CharacterClassBuilder.() -> Unit) {
        characterClass += CharacterClassBuilder(
                positive = false,
                bracketsMandatory = true,
                builder = builder
        ).build()
    }

    fun and(builder: CharacterClassBuilder.() -> Unit) {
        characterClass += "&&" + CharacterClassBuilder(
                positive = true,
                bracketsMandatory = true,
                builder = builder
        ).build()
    }

    fun andNot(builder: CharacterClassBuilder.() -> Unit) {
        characterClass += "&&" + CharacterClassBuilder(
                positive = false,
                bracketsMandatory = true,
                builder = builder
        ).build()
    }

    fun build() = when {
        characterClass.length < 2 && positive && !bracketsMandatory -> characterClass
        positive -> "[$characterClass]"
        else -> "[^$characterClass]"
    }
}