package openreveal.service

import org.joda.time.DateTime

/**
 * Created by Paul Lysak on 02.06.15.
 */
trait Clock {
  def now() = DateTime.now()
}
