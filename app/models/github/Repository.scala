package models.github

import play.api.libs.json.{Json, OFormat, Reads, Writes}

case class Repository(
                    name: String,
                    `private`: Boolean,
                    html_url: String,
                    description: Option[String],
                    language: Option[String],
                    visibility: String,
                    default_branch: String,
                    forks: Int,
                    git_url: String,
                    clone_url: String,
                    ssh_url: String,
                    )

object Repository{
  implicit val reads: Reads[Repository] = Json.reads[Repository]
  implicit val writes: Writes[Repository] = Json.writes[Repository]
}

