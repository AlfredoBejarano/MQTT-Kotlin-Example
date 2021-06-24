# MQTT Kotlin Example

MQTT is a lightweight pub/sub is a network protocol used primarily for IoT devices, one of it's characteristics is that it can survive in low-quality data connections and doesn't goes that hard into the device energy source, this allows other uses outside IoT like instant messaging.

## Paho MQTT library

A library developed by Paho exists to implement MQTT in Java and Android but the library examples don't dwell that heavily into Kotlin or integrations  with Android Jetpack libraries.

## About this repo
This repo serves like a quick example of how to implement MQTT in Android but this code is not that reliable for production enviroments _as-is_, for that there're a lot of improvements that can be made to better adapt into your own projects architecture like dependency injection and the usage of other tools like _Coroutines, Flows_ or _Rx_.