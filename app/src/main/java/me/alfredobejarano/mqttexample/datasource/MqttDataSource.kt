package me.alfredobejarano.mqttexample.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.UUID
import me.alfredobejarano.mqttexample.model.MqttResult
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

private const val MQTT_DISCONNECT_BUFFER_SIZE = 100

class MqttDataSource(serverUri: String) {

    private val mqttAndroidClient: MqttAsyncClient =
        MqttAsyncClient("tcp://$serverUri", UUID.randomUUID().toString(), MemoryPersistence())

    private val _mqttStatusLiveData: MutableLiveData<MqttResult> = MutableLiveData()

    init {
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                _mqttStatusLiveData.postValue(MqttResult.Failure(cause))
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                _mqttStatusLiveData.postValue(MqttResult.Success(message?.payload))
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                _mqttStatusLiveData.postValue(MqttResult.Success(token?.message?.payload))
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                _mqttStatusLiveData.postValue(MqttResult.Success(serverURI?.toByteArray()))
            }
        })
        _mqttStatusLiveData.postValue(MqttResult.Waiting)
    }

    fun connect(onConnected: () -> Unit, onError: MqttResult.Failure.() -> Unit = {}) {
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
                    onConnected()
                    _mqttStatusLiveData.postValue(MqttResult.Success("Connected".toByteArray()))
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    MqttResult.Failure(exception).onError()
                }
            })
        } catch (ex: MqttException) {
            _mqttStatusLiveData.postValue(MqttResult.Failure(ex))
        }
    }

    fun disconnect() {
        mqttAndroidClient.disconnect()
    }

    fun subscribeToTopic(topic: String): LiveData<MqttResult> {
        _mqttStatusLiveData.postValue(MqttResult.Waiting)
        mqttAndroidClient.subscribe(topic, 0, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                _mqttStatusLiveData.postValue(MqttResult.Success("Subscribed!".toByteArray()))
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                _mqttStatusLiveData.postValue(MqttResult.Failure(exception))
            }
        })

        return _mqttStatusLiveData
    }

    fun publishToTopic(topic: String, payload: String) {
        _mqttStatusLiveData.postValue(MqttResult.Waiting)
        mqttAndroidClient.publish(topic, MqttMessage(payload.toByteArray(Charsets.UTF_8)))
    }
}