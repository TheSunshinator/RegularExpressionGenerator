infix fun Regex.shouldMatch(value: CharSequence) {
    check(value matches this) {
        "Value does not match regex: $value"
    }
}

infix fun Regex.shouldNotMatch(value: CharSequence) {
    check(!(value matches this)) {
        "Value matches regex: $value"
    }
}

infix fun Regex.shouldMatchAll(values: Collection<CharSequence>) {
    values.filterNot { it matches this }
        .forEach {
            System.err.println("$it should match regex but match failed")
        }
}

infix fun Regex.shouldNotMatchAny(values: Collection<String>) {
    values.filter { it matches this }
        .forEach {
            System.err.println("$it should NOT match regex but match succeeded")
        }
}

fun Regex.shouldMatchAll(vararg values: String) {
    shouldMatchAll(values.toSet())
}

fun Regex.shouldNotMatchAny(vararg values: String) {
    shouldNotMatchAny(values.toSet())
}

infix fun Regex.shouldContainMatch(value: String) {
    check(containsMatchIn(value)) {
        "Regex does not contain any match: $value"
    }
}

infix fun Regex.shouldNotContainMatch(value: String) {
    val match = find(value)

    if (match != null) System.err.println("Regex does contain a match in: $value ($match)")
}
