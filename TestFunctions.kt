infix fun Regex.shouldMatch(value: String) {
    check(value matches this) {
        "Value does not match regex: $value"
    }
}

infix fun Regex.shouldNotMatch(value: String) {
    check(!(value matches this)) {
        "Value matches regex: $value"
    }
}

infix fun Regex.shouldMatchAll(values: Collection<String>) {
    values.forEach {
        check(it matches this) {
            "Value does not match regex: $it"
        }
    }
}

infix fun Regex.shouldNotMatchAny(values: Collection<String>) {
    values.forEach {
        check(!(it matches this)) {
            "Value matches regex: $it"
        }
    }
}

fun Regex.shouldMatchAll(vararg values: String) {
    values.forEach {
        check(it matches this) {
            "Value does not match regex: $it"
        }
    }
}

fun Regex.shouldNotMatchAny(vararg values: String) {
    values.forEach {
        check(!(it matches this)) {
            "Value matches regex: $it"
        }
    }
}
