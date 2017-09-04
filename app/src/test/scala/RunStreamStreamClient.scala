import java.util.Calendar

import io.grpc.stub.StreamObserver
import mu.node.echo.{Message, SendMessageRequest}
import mu.node.echod.EchodTestModule
import mu.node.echod.grpc.{AccessTokenCallCredentials, EchoClient}
import mu.node.echod.models.UserContext

import scala.concurrent.duration.{Duration, MINUTES}

object RunStreamStreamClient extends App with EchodTestModule {

  val jwtSigningKey = loadPkcs8PrivateKey(pathForTestResourcePath(config.getString("jwt.signing-key")))

  val stub = EchoClient.buildServiceStub(config, fileForTestResourcePath)
  val futureExpiry = Calendar.getInstance().getTimeInMillis + Duration(5, MINUTES).toMillis
  val userId = "8d5921be-8f85-11e6-ae22-56b6b6499611"

  val jwt = UserContext(userId).toJwt(futureExpiry, jwtSigningKey)
  val observer = new StreamObserver[Message] {
    override def onError(t: Throwable): Unit = {
      println(s"failed with $t")
      t.printStackTrace()
    }

    override def onCompleted(): Unit = println(s"completed succesfully!")

    override def onNext(value: Message): Unit = println(s"got message $value")
  }
  val streamer = stub.withCallCredentials(new AccessTokenCallCredentials(jwt)).sendStreamStream(observer)
  (1 to 3) foreach { ix =>
    println(s"sending $ix")
    streamer.onNext(SendMessageRequest(s"sweet stream stream $ix! "))
    Thread.sleep(1000)
  }
  streamer.onCompleted()

  System.in.read()
}



