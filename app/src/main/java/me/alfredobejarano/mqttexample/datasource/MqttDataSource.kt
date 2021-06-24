package me.alfredobejarano.mqttexample.datasource

import android.app.Application
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

private const val LOG_TAG = "ANDROID-MQTT"
private const val MQTT_DISCONNECT_BUFFER_SIZE = 100

class MqttDataSource(app: Application, private val serverUri: String, private val topic: String) {

    private val mqttAndroidClient: MqttAndroidClient =
        MqttAndroidClient(app, "tcp://$serverUri", "android-test-client")

    init {
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                Log.d(LOG_TAG, "MQTT connection lost, caused by:", cause)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(LOG_TAG, "Message from $topic has arrived: ${message.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(LOG_TAG, "Message delivered correctly.")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.d(LOG_TAG, "Connected to $serverURI")
            }
        })
        connect()
    }

    private fun connect() {
        val mqttConnectionOptions = MqttConnectOptions()
        mqttConnectionOptions.isAutomaticReconnect = true
        mqttConnectionOptions.isCleanSession = false

        try {
            mqttAndroidClient.connect(mqttConnectionOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {

                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = MQTT_DISCONNECT_BUFFER_SIZE
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false

                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                    subscribeToTopic()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(LOG_TAG, "Failed to connect to $serverUri. caused by:", exception)
                }
            })
        } catch (ex: MqttException) {
            Log.e(LOG_TAG, "Failed to connect to $serverUri. caused by:", ex)
        }
    }

    private fun subscribeToTopic() {
        mqttAndroidClient.subscribe(topic, 0, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d(LOG_TAG, "Subscribed to: $topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e(LOG_TAG, "Subscription to $topic failed, caused by", exception)
            }
        })
    }

    fun publishToTopic(payload: String) {
        mqttAndroidClient.publish(topic, MqttMessage(payload.toByteArray(Charsets.UTF_8)))
    }
}