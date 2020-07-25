# LargePrimes
A distributed system to find large prime numbers using the Fermat primality test

An actor based distributed system that uses one thread to keep track of found primes and the next number to check. The main thread sends messages to any of 9 other threads that check if the current number is prime, if it is, a message is sent to the main thread where it is recorded in a running list. The main thread also assigns new numbers to check whenever a thread finishes checking a number.
