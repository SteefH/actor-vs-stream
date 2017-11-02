package avs.util

object AccumulationHelper {

  implicit class AccumulationSyntax[Key, Value](val mapping: Map[Key, Value])
      extends AnyVal {

    def sumCounts(
        other: Map[Key, Value]
    )(
        implicit ev: Numeric[Value]
    ): Map[Key, Value] = {
      other.foldLeft(mapping.withDefaultValue(ev.zero)) {
        case (acc, (key, value)) =>
          acc + (key -> ev.plus(acc(key), value))
      }
    }
    def increaseCount(key: Key)(
        implicit ev: Numeric[Value]
    ): Map[Key, Value] = {
      val current = mapping.getOrElse(key, ev.zero)
      mapping + (key -> ev.plus(current, ev.one))
    }
  }

}
