package SfAttachDownload

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Credentials(username: String, password: String)

object JSONConverter {

  implicit val credsWrites = new Writes[Credentials] {
    def writes(creds: Credentials) = Json.obj(
      "username" -> creds.username,
      "password" -> creds.password
    )
  }

  implicit val credsReads: Reads[Credentials] = (
    (JsPath \ "username").read[String] and
      (JsPath \ "password").read[String]
    )(Credentials.apply _)

}