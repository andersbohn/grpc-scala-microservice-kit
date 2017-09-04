import java.util.Calendar

import com.google.protobuf.duration._
import com.typesafe.config.ConfigFactory
import mu.node.echo.SendMessageRequest
import mu.node.echod.EchodTestModule
import mu.node.echod.grpc.{AccessTokenCallCredentials, EchoClient}
import mu.node.echod.models.UserContext
import mu.node.echod.util.FileUtils

import scala.concurrent.duration.{Duration, MINUTES}

object RunClient extends App with EchodTestModule {


  import scala.concurrent.ExecutionContext.Implicits.global

  val jwtSigningKey = loadPkcs8PrivateKey(
    pathForTestResourcePath(config.getString("jwt.signing-key")))


  val stub = EchoClient.buildServiceStub(config, fileForTestResourcePath)
  val futureExpiry = Calendar.getInstance().getTimeInMillis + Duration(5, MINUTES).toMillis
  val userId = "8d5921be-8f85-11e6-ae22-56b6b6499611"
  val req = SendMessageRequest("sweet!")
  val jwt = UserContext(userId).toJwt(futureExpiry, jwtSigningKey)
  val futResponse = stub.withCallCredentials(new AccessTokenCallCredentials(jwt)).send(req)
  futResponse.onComplete { x =>
    println(s"DONE $x")
  }

  System.in.read()
}



