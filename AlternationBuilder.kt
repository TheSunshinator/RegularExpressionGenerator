class AlternationBuilder internal constructor(builder: AlternationBuilder.() -> Unit) {

    private val alternatives = mutableListOf<RegexConstruct>()

    init {
        builder()
    }

    fun either(builder: RegexBuilder.() -> Unit) {
        alternatives.add(RegexBuilder(builder).regexConstruct)
    }

    fun build(): RegexConstruct {
        return when (alternatives.size) {
            0 -> EmptyConstruct
            1 -> alternatives.first()
            else -> AlternationConstruct(alternatives)
        }
    }
}