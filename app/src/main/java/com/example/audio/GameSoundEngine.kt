package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class GameSoundEngine(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true
    var vibrationEnabled: Boolean = true

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    // Pre-computed audio buffers for low latency
    private val clickSoundBuffer: ShortArray by lazy { generateTone(800.0, 45, 0.4) }
    private val coinSoundBuffer: ShortArray by lazy { generateArpeggio(listOf(988.0, 1318.0), 90, 0.6) }
    private val countdownLowBuffer: ShortArray by lazy { generateTone(440.0, 120, 0.5) }
    private val countdownGoBuffer: ShortArray by lazy { generateTone(880.0, 250, 0.8) }
    private val crashSoundBuffer: ShortArray by lazy { generateNoise(150, 0.8) }
    private val nitroSoundBuffer: ShortArray by lazy { generateNitroWhoosh(220, 0.7) }
    private val victorySoundBuffer: ShortArray by lazy { generateFanfare() }

    fun playButtonClick() {
        if (!soundEnabled) return
        playSoundBuffer(clickSoundBuffer)
        vibrate(15)
    }

    fun playCoinCollect() {
        if (!soundEnabled) return
        playSoundBuffer(coinSoundBuffer)
        vibrate(20)
    }

    fun playCountdownBeep(isGo: Boolean) {
        if (!soundEnabled) return
        if (isGo) {
            playSoundBuffer(countdownGoBuffer)
            vibrate(80)
        } else {
            playSoundBuffer(countdownLowBuffer)
            vibrate(35)
        }
    }

    fun playCrash() {
        if (!soundEnabled) return
        playSoundBuffer(crashSoundBuffer)
        vibrate(180)
    }

    fun playNitro() {
        if (!soundEnabled) return
        playSoundBuffer(nitroSoundBuffer)
        vibrate(70)
    }

    fun playVictory() {
        if (!soundEnabled) return
        playSoundBuffer(victorySoundBuffer)
        vibrate(100)
    }

    fun vibrate(durationMs: Long) {
        if (!vibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun playSoundBuffer(buffer: ShortArray) {
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                delay((buffer.size * 1000L / sampleRate) + 20)
                track.release()
            } catch (_: Exception) {}
        }
    }

    private fun generateTone(frequency: Double, durationMs: Int, volume: Double): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = 1.0 - (i.toDouble() / numSamples)
            val sample = sin(2.0 * PI * frequency * t) * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateArpeggio(notes: List<Double>, durationMs: Int, volume: Double): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val noteLength = numSamples / notes.size
        for (i in 0 until numSamples) {
            val noteIndex = minOf(i / noteLength, notes.size - 1)
            val freq = notes[noteIndex]
            val t = i.toDouble() / sampleRate
            val envelope = 1.0 - (i.toDouble() / numSamples) * 0.4
            val sample = (sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)) * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateNoise(durationMs: Int, volume: Double): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var last = 0.0
        for (i in 0 until numSamples) {
            val envelope = 1.0 - (i.toDouble() / numSamples)
            val white = (Math.random() * 2.0 - 1.0)
            // Low pass filtered crunch
            last = last * 0.7 + white * 0.3
            val sample = last * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateNitroWhoosh(durationMs: Int, volume: Double): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val pitch = 150.0 + 350.0 * (1.0 - t)
            val noise = (Math.random() * 2.0 - 1.0) * 0.4
            val tone = sin(2.0 * PI * pitch * (i.toDouble() / sampleRate)) * 0.6
            val env = if (t < 0.2) t / 0.2 else (1.0 - t) / 0.8
            val sample = (tone + noise) * env * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateFanfare(): ShortArray {
        val notes = listOf(523.25, 659.25, 783.99, 1046.5) // C5, E5, G5, C6
        val totalMs = 380
        val numSamples = (sampleRate * (totalMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val noteSamples = numSamples / notes.size
        for (i in 0 until numSamples) {
            val noteIdx = minOf(i / noteSamples, notes.size - 1)
            val freq = notes[noteIdx]
            val t = i.toDouble() / sampleRate
            val sample = sin(2.0 * PI * freq * t) * 0.5
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    fun release() {
        // Audio resources are released per-playback in scope
    }
}
