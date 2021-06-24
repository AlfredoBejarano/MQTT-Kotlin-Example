package me.alfredobejarano.mqttexample

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import me.alfredobejarano.mqttexample.databinding.ActivityMainBinding
import me.alfredobejarano.mqttexample.datasource.MqttDataSource

const val MQTT_SERVER = "YOUR SERVER URL HERE"
const val MQTT_TOPIC = "YOUR TOPIC HERE"

class MainActivity : AppCompatActivity() {
    private var mqttDataSource: MqttDataSource? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        mqttDataSource = MqttDataSource(application, MQTT_SERVER)

        binding.sendMqttMessageButton.setOnClickListener {
            mqttDataSource?.publishToTopic(MQTT_TOPIC, binding.mqttMessageEditText.text.toString())
        }
    }

    override fun onDestroy() {
        mqttDataSource = null
        super.onDestroy()
    }
}