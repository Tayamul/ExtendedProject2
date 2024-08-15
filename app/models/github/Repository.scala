package models.github

import play.api.libs.json.{Json, OFormat, Reads, Writes}

case class Repository(
                    name: String,
                    `private`: Boolean,
                    html_url: String,
                    description: Option[String],
                    )

object Repository{
  implicit val reads: Reads[Repository] = Json.reads[Repository]
  implicit val writes: Writes[Repository] = Json.writes[Repository]
}

