package com.clap2esp.app

import kotlin.math.abs

class ClapDetector {


    // Порог громкости хлопка
    private val threshold = 15000


    // Защита от повторных срабатываний
    private var lastClapTime = 0L

    private val cooldown = 500L


    // Чтобы не писать в лог один и тот же шум постоянно
    private var lastLoggedAmplitude = 0


    fun detect(buffer: ShortArray): Boolean {


        var maxAmplitude = 0


        for (sample in buffer) {

            val amplitude = abs(sample.toInt())

            if (amplitude > maxAmplitude) {
                maxAmplitude = amplitude
            }
        }


        // Показываем только заметные изменения громкости
        if (maxAmplitude > threshold / 2 &&
            maxAmplitude != lastLoggedAmplitude) {

            Logger.log(
                "Sound peak: $maxAmplitude"
            )

            lastLoggedAmplitude = maxAmplitude
        }


        val currentTime = System.currentTimeMillis()


        if (
            maxAmplitude > threshold &&
            currentTime - lastClapTime > cooldown
        ) {

            lastClapTime = currentTime


            Logger.log(
                "CLAP DETECTED"
            )


            return true
        }


        return false
    }
}
