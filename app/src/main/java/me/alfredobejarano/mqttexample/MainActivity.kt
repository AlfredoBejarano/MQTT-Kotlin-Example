package me.alfredobejarano.mqttexample

import android.R.anim.fade_in
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import me.alfredobejarano.mqttexample.databinding.ActivityMainBinding
import me.alfredobejarano.mqttexample.datasource.MqttDataSource
import me.alfredobejarano.mqttexample.model.MqttResult

const val MQTT_SERVER = "YOUR SERVER"
const val MQTT_TOPIC = "YOUR TOPIC"

class MainActivity : AppCompatActivity() {
    private var errorSnackbar: Snackbar? = null
    private var mqttDataSource: MqttDataSource? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        errorSnackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_SHORT)

        mqttDataSource = MqttDataSource(MQTT_SERVER)
        mqttDataSource?.connect(::subscribeToTopic, ::onMqttError)

        binding.sendMqttMessageButton.setOnClickListener {
            mqttDataSource?.publishToTopic(MQTT_TOPIC, binding.mqttMessageEditText.text.toString())
            binding.mqttMessageEditText.setText("")
        }
    }

    private fun onMqttError(result: MqttResult.Failure) {
        binding.mqttProgressBar.visibility = View.GONE
        errorSnackbar?.setText(result.exception?.localizedMessage ?: "")?.show()
    }

    private fun subscribeToTopic() = runOnUiThread {
        mqttDataSource?.subscribeToTopic(MQTT_TOPIC)?.observe(this, { result ->
            when (result) {
                is MqttResult.Failure -> onMqttError(result)
                is MqttResult.Success -> onMqttMessageReceived(result)
                MqttResult.Waiting -> binding.mqttProgressBar.visibility = View.VISIBLE
            }
        })
    }

    private fun onMqttMessageReceived(result: MqttResult.Success) {
        binding.mqttProgressBar.visibility = View.GONE
        result.payload?.run {
            binding.mqttMessageReceivedTextView.text = String(this)
            binding.mqttMessageReceivedTextView.startAnimation(
                AnimationUtils.loadAnimation(this@MainActivity, fade_in)
            )
        }
    }

    override fun onDestroy() {
        mqttDataSource?.disconnect()
        mqttDataSource = null
        errorSnackbar = null
        super.onDestroy()
    }
}