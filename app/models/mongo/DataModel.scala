package models.mongo

import play.api.libs.json.{Json, OFormat}
import play.api.data._
import play.api.data.Forms._


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

  val dataForm: Form[DataModel] = Form(
    mapping(
      "_id" -> text,
      "dateCreated" -> text,
      "location" -> text,
      "numFollowers" -> number,
      "numFollowing" -> number,
      "repoUrl" -> text,
      "name" -> text
    )(DataModel.apply)(DataModel.unapply)
  )

}