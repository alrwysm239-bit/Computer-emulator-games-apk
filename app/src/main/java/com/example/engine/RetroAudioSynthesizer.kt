package com.example.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Lightweight PCM synthesizer emulating AdLib OPL3 / Sound Blaster / PC Speaker chimes & tones.
 */
class RetroAudioSynthesizer {
    private val sampleRate = 22050
    private var audioJob: Job? = null

    fun playTone(frequencyHz: Double, durationMs: Int, volume: Float, enabled: Boolean) {
        if (!enabled || volume <= 0.01f) return
        audioJob?.cancel()
        audioJob = CoroutineScope(Dispatchers.Default).launch {
            var track: AudioTrack? = null
            try {
                val sampleCount = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(256)
                val samples = ShortArray(sampleCount)
                val amp = (Short.MAX_VALUE * 0.25f * volume.coerceIn(0f, 1f)).toInt()
                for (i in 0 until sampleCount) {
                    val env = 1.0 - (i.toDouble() / sampleCount)
                    val wave = sin(2.0 * Math.PI * i * frequencyHz / sampleRate)
                    samples[i] = (wave * amp * env).toInt().toShort()
                }
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
                track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(maxOf(minBuf, samples.size * 2))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.play()
                delay(durationMs.toLong() + 30L)
            } catch (_: Throwable) {
                // Ignore audio hardware limitations in headless environments
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Throwable) {
                }
            }
        }
    }

    fun release() {
        audioJob?.cancel()
        audioJob = null
    }
}
