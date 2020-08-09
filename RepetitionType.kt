internal sealed class RepetitionType {
    abstract val postfix: String

    object Optional : RepetitionType() {
        override val postfix = "?"
    }

    object AtLeastOnce : RepetitionType() {
        override val postfix = "+"
    }

    object Any : RepetitionType() {
        override val postfix = "*"
    }

    class Exactly(times: Int) : RepetitionType() {
        override val postfix = "{$times}"
    }

    class AtLeast(times: Int) : RepetitionType() {
        override val postfix = "{$times,}"
    }

    class AtMost(times: Int) : RepetitionType() {
        override val postfix = "{,$times}"
    }

    class Range(minTimes: Int, maxTimes: Int) : RepetitionType() {
        override val postfix = "{$minTimes,$maxTimes}"
    }
}
