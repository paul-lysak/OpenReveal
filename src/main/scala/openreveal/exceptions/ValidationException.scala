package openreveal.exceptions

/**
 * Created by Paul Lysak on 03.06.15.
 */
case class ValidationException(msg: String) extends RuntimeException(msg)
