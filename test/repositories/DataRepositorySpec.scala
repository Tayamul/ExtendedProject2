//package repositories
//
//import models.{APIError, DataModel}
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.wordspec.AnyWordSpec
//
//import java.time.LocalDate
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//class DataRepositorySpec extends AnyWordSpec with Matchers {
//
//
//  "DataRepository" should {
//
//    "successfully create a DataModel" in {
//      val dataModel = DataModel(
//        _username = "test_user",
//        dateCreated = LocalDate.now(),
//        location = Some("London"),
//        followers = 150,
//        following = 75
//      )
//
//      val result = repository.create(dataModel).futureValue
//      result shouldBe Right(dataModel)
//    }
//
//    "retrieve a DataModel by username" in {
//      val dataModel = DataModel(
//        _username = "test_user",
//        dateCreated = LocalDate.now(),
//        location = Some("London"),
//        followers = 150,
//        following = 75
//      )
//      repository.create(dataModel).futureValue
//
//      val result = repository.read("test_user").futureValue
//      result shouldBe Right(Some(dataModel))
//    }
//
//    "update a DataModel by username" in {
//      val dataModel = DataModel(
//        _username = "test_user",
//        dateCreated = LocalDate.now(),
//        location = Some("London"),
//        followers = 150,
//        following = 75
//      )
//      repository.create(dataModel).futureValue
//
//      val updatedDataModel = dataModel.copy(
//        location = Some("New York"),
//        followers = 200
//      )
//      val result = repository.update("test_user", updatedDataModel).futureValue
//
//      result shouldBe a[Right[_, _]]
//      result.isRight shouldBe true
//
//      val updatedResult = repository.read("test_user").futureValue
//      updatedResult shouldBe Right(Some(updatedDataModel))
//    }
//
//    "delete a DataModel by username" in {
//      val dataModel = DataModel(
//        _username = "test_user",
//        dateCreated = LocalDate.now(),
//        location = Some("London"),
//        followers = 150,
//        following = 75
//      )
//      repository.create(dataModel).futureValue
//
//      val result = repository.delete("test_user").futureValue
//      result shouldBe a[Right[_, _]]
//      result.isRight shouldBe true
//
//      val deletedResult = repository.read("test_user").futureValue
//      deletedResult shouldBe Right(None)
//    }
//
//    "return an empty sequence when index is called and there are no records" in {
//      val result = repository.index().futureValue
//      result shouldBe Right(Seq.empty)
//    }
//
//    "return a sequence of DataModels when index is called and there are records" in {
//      val dataModel1 = DataModel(
//        _username = "test_user1",
//        dateCreated = LocalDate.now(),
//        location = Some("London"),
//        followers = 150,
//        following = 75
//      )
//      val dataModel2 = DataModel(
//        _username = "test_user2",
//        dateCreated = LocalDate.now(),
//        location = Some("Paris"),
//        followers = 200,
//        following = 100
//      )
//
//      repository.create(dataModel1).futureValue
//      repository.create(dataModel2).futureValue
//
//      val result = repository.index().futureValue
//      result shouldBe Right(Seq(dataModel1, dataModel2))
//    }
//  }
//}
