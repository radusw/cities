package model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.generic.auto._

final case class CountryInfo(name: String, currencies: List[Currency])
object CountryInfo {
  implicit val decoder: Decoder[CountryInfo] = deriveDecoder
  implicit val encoder: Encoder[CountryInfo] = deriveEncoder
}

final case class Currency(code: String, symbol: String)
