package models.github

import play.api.libs.json.{Json, Reads, Writes}

case class RepoContentItem(
                  name: String,
                  path: String,
                  sha: String,
                  `type`: String
                  )

object RepoContentItem{
  implicit val reads: Reads[RepoContentItem] = Json.reads[RepoContentItem]
  implicit val writes: Writes[RepoContentItem] = Json.writes[RepoContentItem]
}
