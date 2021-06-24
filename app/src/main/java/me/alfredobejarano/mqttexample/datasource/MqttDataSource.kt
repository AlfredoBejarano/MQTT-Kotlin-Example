package me.alfredobejarano.mqttexample.datasource

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.alfredobejarano.mqttexample.model.MqttResult
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

private const val MQTT_DISCONNECT_BUFFER_SIZE = 100

class MqttDataSource(app: Application, private val serverUri: String) {

    private val mqttAndroidClient: MqttAndroidClient =
        MqttAndroidClient(app, "tcp://$serverUri", "android-test-client")

    private val _mqttStatusLiveData: MutableLiveData<MqttResult> = MutableLiveData()
    val mqttStatusLiveData: LiveData<MqttResult> = _mqttStatusLiveData

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
                    _mqttStatusLiveData.postValue(MqttResult.Success(asyncActionToken?.response?.payload))
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    _mqttStatusLiveData.postValue(MqttResult.Failure(exception))
                }
            })
        } catch (ex: MqttException) {
            _mqttStatusLiveData.postValue(MqttResult.Failure(ex))
        }
    }

    fun subscribeToTopic(topic: String) {
        mqttAndroidClient.subscribe(topic, 0, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                _mqttStatusLiveData.postValue(MqttResult.Success(asyncActionToken?.response?.payload))
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                _mqttStatusLiveData.postValue(MqttResult.Failure(exception))
            }
        })
    }

    fun publishToTopic(topic: String, payload: String) {
        mqttAndroidClient.publish(topic, MqttMessage(payload.toByteArray(Charsets.UTF_8)))
    }
}