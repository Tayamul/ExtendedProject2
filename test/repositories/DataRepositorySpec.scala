package repositories

import baseSpec.BaseSpec
import models.{APIError, DataModel}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.test.Injecting

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class DataRepositorySpec extends BaseSpec with Injecting with GuiceOneAppPerSuite with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val repository: DataRepository = inject[DataRepository]

  override def beforeEach(): Unit = {
    await(repository.deleteAll())
    super.beforeEach()
  }

  "DataRepository" should {

    // Create tests
    "successfully create and retrieve a DataModel" in {
      val dataModel = DataModel(
        _username = "test_user",
        dateCreated = LocalDate.now().toString,
        location = "London",
        numFollowers = 150,
        numFollowing = 75,
        repoUrl = "https://github.com/test_user",
        name = "Test User"
      )

      val createResult = await(repository.create(dataModel))
      createResult mustBe Right(dataModel)

      val readResult = await(repository.read("test_user"))
      readResult mustBe Right(Some(dataModel))
    }

    "return an error when creating a DataModel with a duplicate username" in {
      val dataModel1 = DataModel(
        _username = "duplicate_user",
        dateCreated = LocalDate.now().toString,
        location = "London",
        numFollowers = 150,
        numFollowing = 75,
        repoUrl = "https://github.com/duplicate_user",
        name = "Duplicate User"
      )
      await(repository.create(dataModel1)) mustBe Right(dataModel1)

      val dataModel2 = dataModel1.copy(location = "Paris")
      val createResult = await(repository.create(dataModel2))

      createResult match {
        case Left(APIError.BadAPIResponse(409, "Username already exists")) => succeed
        case _ => fail("Expected an error for duplicate username")
      }
    }

    // Read tests
    "return None for a non-existent DataModel" in {
      val readResult = await(repository.read("non_existent_user"))
      readResult mustBe Right(None)
    }

    "return a DataModel if it exists" in {
      val dataModel = DataModel(
        _username = "test_user",
        dateCreated = LocalDate.now().toString,
        location = "London",
        numFollowers = 150,
        numFollowing = 75,
        repoUrl = "https://github.com/test_user",
        name = "Test User"
      )

      await(repository.create(dataModel))

      val readResult = await(repository.read("test_user"))
      readResult mustBe Right(Some(dataModel))
    }

    // Update tests
    "update a DataModel by username" in {
      val dataModel = DataModel(
        _username = "test_user",
        dateCreated = LocalDate.now().toString,
        location = "London",
        numFollowers = 150,
        numFollowing = 75,
        repoUrl = "https://github.com/test_user",
        name = "Test User"
      )

      await(repository.create(dataModel))

      val updatedDataModel = dataModel.copy(
        location = "New York",
        numFollowers = 200
      )

      val updateResult = await(repository.update("test_user", updatedDataModel))
      updateResult.isRight mustBe true

      val readUpdatedResult = await(repository.read("test_user"))
      readUpdatedResult mustBe Right(Some(updatedDataModel))
    }

//    "return an error when updating a non-existent DataModel" in {
//      val dataModel = DataModel(
//        _username = "non_existent_user",
//        dateCreated = LocalDate.now().toString,
//        location = "London",
//        numFollowers = 150,
//        numFollowing = 75,
//        repoUrl = "https://github.com/non_existent_user",
//        name = "Non Existent User"
//      )
//
//      val updateResult = await(repository.update("non_existent_user", dataModel))
//      updateResult match {
//        case Left(APIError.BadAPIResponse(404, "No item found with id: non_existent_user")) => succeed
//        case _ => fail("Expected an error for non-existent username")
//      }
//    }

    // Delete tests
    "delete a DataModel by username" in {
      val dataModel = DataModel(
        _username = "test_user",
        dateCreated = LocalDate.now().toString,
        location = "London",
        numFollowers = 150,
        numFollowing = 75,
        repoUrl = "https://github.com/test_user",
        name = "Test User"
      )

      await(repository.create(dataModel))

      val deleteResult = await(repository.delete("test_user"))
      deleteResult.isRight mustBe true

      val readDeletedResult = await(repository.read("test_user"))
      readDeletedResult mustBe Right(None)
    }

//    "return an error when deleting a non-existent DataModel" in {
//      val deleteResult = await(repository.delete("non_existent_user"))
//
//      deleteResult match {
//        case Left(APIError.BadAPIResponse(404, "No item found with id: non_existent_user")) =>
//          // The error response is as expected
//          succeed
//        case Left(error) =>
//          // Fail if the error is not as expected
//          fail(s"Expected error 'BadAPIResponse(404, No item found with id: non_existent_user)' but got: $error")
//        case Right(_) =>
//          // Fail if no error was returned
//          fail("Expected an error for non-existent username but got a successful delete result")
//      }
//    }



    // Index tests
    "return an empty sequence when index is called and there are no records" in {
      val indexResult = await(repository.index())
      indexResult mustBe Right(Seq.empty)
    }

    "return a sequence of DataModels when index is called and there are records" in {
      val dataModel1 = DataModel(
        _username = "test_user1",
        dateCreated = LocalDate.now().toString,
        location = "London",
        numFollowers = 150,
        numFollowing = 75,
        repoUrl = "https://github.com/test_user1",
        name = "Test User 1"
      )
      val dataModel2 = DataModel(
        _username = "test_user2",
        dateCreated = LocalDate.now().toString,
        location = "Paris",
        numFollowers = 200,
        numFollowing = 100,
        repoUrl = "https://github.com/test_user2",
        name = "Test User 2"
      )

      await(repository.create(dataModel1))
      await(repository.create(dataModel2))

      val indexResult = await(repository.index())
      indexResult mustBe Right(Seq(dataModel1, dataModel2))
    }
  }
}
