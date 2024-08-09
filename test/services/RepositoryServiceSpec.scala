package services

import baseSpec.BaseSpec
import com.mongodb.client.result.{DeleteResult, UpdateResult}
import models.{APIError, DataModel}
import org.mongodb.scala.bson.{BsonString, BsonValue}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import repositories.DataRepoMethods

import scala.concurrent.{ExecutionContext, Future}

class RepositoryServiceSpec extends BaseSpec with MockFactory with ScalaFutures with GuiceOneAppPerSuite {
  val mockDataRepo: DataRepoMethods = mock[DataRepoMethods]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val testRepoService = new RepositoryService(mockDataRepo)

  private val dataModel: DataModel = DataModel(
    "username",
    "date created",
    "location",
    3, // num followers
    3, // num following
    "repoURL",
    "test Name"
  )


  "RepoService .index" should {
    "return a Right" when {
      "DataRepository .index returns a Right" in {
        (mockDataRepo.index()(_: ExecutionContext))
          .expects(*)
          .returning(Future(Right(Seq(dataModel))))
          .once()

        whenReady(testRepoService.index()) { result =>
          result shouldBe Right(Seq(dataModel))
        }
      }
    }

    "return a Left" when {
      "DataRepository .index returns a Left" in {
        val apiError: APIError.BadAPIResponse = APIError.BadAPIResponse(500, s"An error occurred")

        (mockDataRepo.index()(_: ExecutionContext))
          .expects(*)
          .returning(Future(Left(apiError)))
          .once()

        whenReady(testRepoService.index()) { result =>
          result shouldBe Left(apiError)
        }
      }
    }
  }


  "RepoService .create" should {
    "return a right" when {
      "DataRepository .create returns a right" in {
        (mockDataRepo.create(_: DataModel)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future(Right(dataModel)))
          .once()

        whenReady(testRepoService.create(dataModel)) { result =>
          result shouldBe Right(dataModel)
        }
      }
      "return a left" when {
        "DataRepository .create returns a left" in {
          val apiError: APIError.BadAPIResponse = APIError.BadAPIResponse(500, s"An error occurred")

          (mockDataRepo.create(_: DataModel)(_: ExecutionContext))
            .expects(*, *)
            .returning(Future(Left(apiError)))
            .once()

          whenReady(testRepoService.create(dataModel)) { result =>
            result shouldBe Left(apiError)
          }
        }
      }
    }
  }


  "RepoService .read" should {
    "return a right" when {
      "DataRepository .read returns a right Some" in {

        val username: String = "username"

        (mockDataRepo.read(_: String)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future(Right(Some(dataModel))))
          .once()

        whenReady(testRepoService.read(username)) { result =>
          result shouldBe Right(dataModel)
        }
      }
      "return a left" when {
        "DataRepository .read returns a left" in {
          val username: String = "username"
          val apiError: APIError.BadAPIResponse = APIError.BadAPIResponse(500, s"An error occurred")

          (mockDataRepo.read(_: String)(_: ExecutionContext))
            .expects(*, *)
            .returning(Future(Left(apiError)))
            .once()

          whenReady(testRepoService.read(username)) { result =>
            result shouldBe Left(apiError)
          }
        }
        "DataRepository .read returns a right None" in {
          val username: String = "username"
          val apiError: APIError.BadAPIResponse = APIError.BadAPIResponse(404, s"No Book Found with username: $username")

          (mockDataRepo.read(_: String)(_: ExecutionContext))
            .expects(*, *)
            .returning(Future(Right(None)))
            .once()

          whenReady(testRepoService.read(username)) { result =>
            result shouldBe Left(apiError)
          }
        }

      }
    }

  }
  "RepoService .update" should {
    "return a right" when {
      "DataRepository .update returns a right and result was acknowledged" in {
        val username: String = "username"
        val testUpdateResult = new UpdateResult {
          override def wasAcknowledged(): Boolean = true

          override def getMatchedCount: Long = 1

          override def getModifiedCount: Long = 1

          override def getUpsertedId: BsonValue = BsonString("")
        }
        (mockDataRepo.update(_: String, _: DataModel)(_: ExecutionContext))
          .expects(username, dataModel, *)
          .returning(Future(Right(testUpdateResult)))
          .once()

        whenReady(testRepoService.update(username, dataModel)) { result =>
          result shouldBe Right(testUpdateResult)
        }
      }
    }
    "return a left" when {
      "DataRepository .update returns a right and result was unacknowledged" in {

        val username: String = "username"
        val testUpdateBadResult = new UpdateResult {
          override def wasAcknowledged(): Boolean = false

          override def getMatchedCount: Long = 1

          override def getModifiedCount: Long = 1

          override def getUpsertedId: BsonValue = BsonString("")
        }
        val apiError = APIError.BadAPIResponse(404, s"$testUpdateBadResult Not Found")


        (mockDataRepo.update(_: String, _: DataModel)(_: ExecutionContext))
          .expects(*, *, *)
          .returning(Future(Right(testUpdateBadResult)))
          .once()

        whenReady(testRepoService.update(username, dataModel)) { result =>
          result shouldBe Left(apiError)
        }
      }
      "DataRepository .update returns a left" in {

        val username: String = "username"
        val apiError: APIError.BadAPIResponse = APIError.BadAPIResponse(500, s"An error occurred")

        (mockDataRepo.update(_: String, _: DataModel)(_: ExecutionContext))
          .expects(*, *, *)
          .returning(Future(Left(apiError)))
          .once()

        whenReady(testRepoService.update(username, dataModel)) { result =>
          result shouldBe Left(apiError)
        }

      }
    }
  }


  "RepoService .delete" should {
    "return a right" when {
      "DataRepository .delete returns a right and result was acknowledged" in {
        val username: String = "username"

        val testDeleteResult = new DeleteResult {
          override def wasAcknowledged(): Boolean = true
          override def getDeletedCount: Long = 1
        }

        (mockDataRepo.delete(_:String)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future(Right(testDeleteResult)))
          .once()

        whenReady(testRepoService.delete(username)) { result =>
          result shouldBe Right(testDeleteResult)
        }
      }
    }
    "return a left" when {
      "DataRepository .delete returns a left" in {
        val username: String = "username"
        val apiError: APIError.BadAPIResponse = APIError.BadAPIResponse(500, s"An error occurred")

        (mockDataRepo.delete(_: String)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future(Left(apiError)))
          .once()

        whenReady(testRepoService.delete(username)) { result =>
          result shouldBe Left(apiError)
        }
      }

      "DataRepository .delete returns a right and result was unacknowledged " in {
        val username: String = "username"


        val testDeleteBadResult = new DeleteResult {
          override def wasAcknowledged(): Boolean = false
          override def getDeletedCount: Long = 1
        }

        val apiError = APIError.BadAPIResponse(404, s"$testDeleteBadResult Not Found")

        (mockDataRepo.delete(_:String)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future(Right(testDeleteBadResult)))
          .once()

        whenReady(testRepoService.delete(username)) { result =>
          result shouldBe Left(apiError)
        }
      }
    }
  }
}
