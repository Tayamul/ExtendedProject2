package services

import baseSpec.BaseSpec
import models.{APIError, DataModel}
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
          .expects(dataModel, *)
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
            .expects(dataModel, *)
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
          .expects(username, *)
          .returning(Future(Right(Some(dataModel))))
          .once()

        whenReady(testRepoService.read(username)) { result =>
          result shouldBe Right(Some(dataModel))
        }
      }
      "return a left" when {
        "DataRepository .read returns a left" in {
          val username: String = "username"
          val apiError: APIError.BadAPIResponse = APIError.BadAPIResponse(500, s"An error occurred")

          (mockDataRepo.read(_: String)(_: ExecutionContext))
            .expects(username, *)
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
            .expects(username, *)
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

      }
    }
    "return a left" when {
      "DataRepository .update returns a left" in {}
    }
  }


  "RepoService .delete" should {
    "return a right" when {
      "DataRepository .delete returns a right" in {}
    }
    "return a left" when {
      "DataRepository .delete returns a left" in {}
    }
  }
}
// TEMPLATE
// "RepoService .method" should {
//   "return a right" when {
//     "DataRepository .method returns a right" in {}
//     }
//   "return a left" when {
//     "DataRepository .method returns a left" in {}
//     }
//   }