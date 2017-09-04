package mu.node.echod.services

import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

import io.grpc.Status
import io.grpc.stub.StreamObserver
import mu.node.echo.{EchoServiceGrpc, Message, SendMessageRequest}
import mu.node.echod.grpc.UserContextServerInterceptor

import scala.concurrent.Future
import scala.util.{Failure, Success}

class EchoService extends EchoServiceGrpc.EchoService {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def send(request: SendMessageRequest): Future[Message] = {
    Option(UserContextServerInterceptor.userContextKey.get) match {
      case Some(userContext) =>
        Future.successful(Message(UUID.randomUUID().toString, userContext.userId, request.content))
      case None =>
        Future.failed(Status.UNAUTHENTICATED.asException())
    }
  }

  override def sendStream(request: SendMessageRequest, responseObserver: StreamObserver[Message]): Unit = {
    Option(UserContextServerInterceptor.userContextKey.get) match {
      case Some(userContext) =>
        Future {
          (1 to 3) foreach { ix =>
            responseObserver.onNext(Message(UUID.randomUUID().toString, userContext.userId, request.content + "-#" + ix))
            Thread.sleep(2000)
          }
        }.onComplete {
          case Success(ok) =>
            responseObserver.onCompleted()
          case Failure(err) =>
            responseObserver.onError(err)
        }

      case None =>
        responseObserver.onError(Status.UNAUTHENTICATED.asException())
    }
  }

  override def sendStreamStream(responseObserver: StreamObserver[Message]): StreamObserver[SendMessageRequest] = {
    var cnt = 1
    var done = new AtomicBoolean(false)
    var processing = new AtomicBoolean(false)
    new StreamObserver[SendMessageRequest] {
      override def onError(t: Throwable): Unit = {
        done.set(true)
        responseObserver.onError(t)
      }

      override def onCompleted(): Unit = done.set(true)

      override def onNext(request: SendMessageRequest): Unit = {
        Option(UserContextServerInterceptor.userContextKey.get) match {
          case Some(userContext) =>
            println(s"received '${request.content}'")
            Future {
              (1 to 3) foreach { ix =>
                println(s" - responding $ix")
                responseObserver.onNext(Message(UUID.randomUUID().toString, userContext.userId, request.content + "-in:$cnt-out:#" + ix))
                cnt += 1
                Thread.sleep(2000)
              }
              if (done.get) {
                println(s"all done !")
                responseObserver.onCompleted()
              }
            }
          case None =>
            responseObserver.onError(Status.UNAUTHENTICATED.asException())
        }
      }

    }
  }
}
