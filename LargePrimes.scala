import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

class PrimeChecker(host: ActorRef) extends Actor {
    val One = BigInt(1)
    val Zero = BigInt(0)
    
    def randomBigInt(value: BigInt): BigInt = {
        val byteArray = (1 to value.bitLength).map(x => Byte.MaxValue).toArray
        BigInt(value.bitLength, new Random) - (BigInt(byteArray) - value)
    }

    def mpow(N: BigInt, K: BigInt, M: BigInt): BigInt = (N,K,M) match {
        case (N, One, _) => N
        case _ => mpow(K % 2, N, K, M)
    }

    def mpow(R: BigInt, N: BigInt, K: BigInt, M: BigInt): BigInt = (R, N, K, M) match {
        case (Zero, N, K, M) => {
            val X = mpow(N, K / 2, M)
            (X * X) % M
        }
        case (_, N, K, M) => {
            val X = mpow(N, K - 1, M)
            (X * N) % M
        }
    }

    def fermat(P: BigInt): Boolean = P match {
        case One => true
        case _ => mpow( randomBigInt(P) , P-1, P) == 1
    }

    def test(toTest: BigInt, numTests: BigInt): Boolean = (toTest, numTests) match {
        case (_, Zero) => true
        case _ => fermat(toTest) match {
            case true => test(toTest, numTests-1)
            case false => false
        }
    }

    def receive = {
        case number: BigInt => {
            if(test(number, 15))
                host ! number
            host ! "done"
        }
    }
}

class HighestPrimes extends Actor {
    val primes = ArrayBuffer[BigInt]()
    var nextNumber: BigInt = 756545969
    nextNumber = nextNumber pow 20

    def receive = {
        case number: BigInt => {
            println("Recieved probable prime: " + number)
            primes += number
        }
        case "done" => {
            sender() ! nextNumber
            // println("Assigning number to check: " + nextNumber)
            nextNumber += 1
        }
    }
}

object LargePrimes extends App {
    val system = ActorSystem()
    val host = system.actorOf(Props[HighestPrimes])
    var initialNum: BigInt = 756545969
    initialNum = initialNum pow 20

    val actors = (1 to 9).map( x => system.actorOf(Props(new PrimeChecker(host))))

    import system.dispatcher
    system.scheduler.scheduleOnce(500 millis) {
      for ( (actor, num) <- actors.zipWithIndex)
        actor ! (initialNum - num - 1)
    }
}