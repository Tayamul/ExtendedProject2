package models

import play.api.libs.json.{Json, OFormat}

// _id relates to username
case class DataModel(
                      _id: String,
                     dateCreated: String,
                     location: String,
                     numFollowers: Int,
                     numFollowing: Int,
                     repoUrl: String,
                      name: String
                    )

object DataModel {
  implicit val formats: OFormat[DataModel] = Json.format[DataModel]
}