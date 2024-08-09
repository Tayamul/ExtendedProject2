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
    "_username",
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
        (mockDataRepo.create(dataModel)(_: ExecutionContext))
          .expects(*)
          .returning(Future(Right(dataModel)))
          .once()

        whenReady(testRepoService.create(dataModel)) { result =>
          result shouldBe Right(dataModel)
      }
    }
    "return a left" when {
      "DataRepository .create returns a left" in {
      true}
    }
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