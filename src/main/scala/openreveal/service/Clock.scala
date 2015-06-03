package openreveal.service

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by Paul Lysak on 02.06.15.
 */
trait Clock {
  def now() = DateTime.now()

  def nowIsoString() = ISODateTimeFormat.dateTime().print(now)
}

object DefaultClock extends Clock
