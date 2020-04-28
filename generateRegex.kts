#!/usr/bin/env kscript

@file:Include("RegexBuilder.kt")

/**
 * Run with `kscript generateRegex.kts`
 */
val regularExpression = regex {
    character('"')
    atLeastOnce(QuantifierType.LAZY) { any() }
    characterClass {
        character('c')
        characterIn('j'..'m')
        and {
            character('l')
        }
    }
}

println("\"$regularExpression\"")
