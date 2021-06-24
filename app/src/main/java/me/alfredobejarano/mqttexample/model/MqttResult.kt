package me.alfredobejarano.mqttexample.model

sealed class MqttResult {
    object Waiting : MqttResult()

    data class Failure(val exception: Throwable?) : MqttResult()

    data class Success(val payload: ByteArray?) : MqttResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            if (payload != null) {
                if (other.payload == null) return false
                if (!payload.contentEquals(other.payload)) return false
            } else if (other.payload != null) return false

            return true
        }

        override fun hashCode(): Int {
            return payload?.contentHashCode() ?: 0
        }
    }
}