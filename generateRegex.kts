regex {
    character('"')
    atLeastOnce(QuantifierType.LAZY) { any() }
    anyCharacterInSet {
        character('c')
        allIn('j'..'m')
        intersectWith {
            character('l')
        }
    }
}
