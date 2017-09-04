import java.util.Calendar

import io.grpc.stub.StreamObserver
import mu.node.echo.{Message, SendMessageRequest}
import mu.node.echod.EchodTestModule
import mu.node.echod.grpc.{AccessTokenCallCredentials, EchoClient}
import mu.node.echod.models.UserContext

import scala.concurrent.duration.{Duration, MINUTES}

object RunStreamClient extends App with EchodTestModule {

  import scala.concurrent.ExecutionContext.Implicits.global

  val jwtSigningKey = loadPkcs8PrivateKey(pathForTestResourcePath(config.getString("jwt.signing-key")))

  val stub = EchoClient.buildServiceStub(config, fileForTestResourcePath)
  val futureExpiry = Calendar.getInstance().getTimeInMillis + Duration(5, MINUTES).toMillis
  val userId = "8d5921be-8f85-11e6-ae22-56b6b6499611"
  val req = SendMessageRequest("sweet stream!")
  val jwt = UserContext(userId).toJwt(futureExpiry, jwtSigningKey)
  val observer = new StreamObserver[Message] {
    override def onError(t: Throwable): Unit = println(s"failed with $t")

    override def onCompleted(): Unit = println(s"completed succesfully!")

    override def onNext(value: Message): Unit = println(s"got message $value")
  }
  stub.withCallCredentials(new AccessTokenCallCredentials(jwt)).sendStream(req, observer)

  System.in.read()
}



