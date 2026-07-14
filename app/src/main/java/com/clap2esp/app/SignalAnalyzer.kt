package com.clap2esp.app

import kotlin.math.abs
import kotlin.math.sqrt

class SignalAnalyzer {

    fun analyze(buffer: ShortArray): SignalFeatures {

        var peak = 0

        var energy = 0.0

        var zeroCrossings = 0

        var attack = 0

        var decay = 0

        var impulseWidth = 0

        var previous = 0

        var reachedPeak = false

        var peakIndex = 0

        var lastAboveThreshold = 0

        val threshold = 2500

        for (i in buffer.indices) {

            val sample = abs(buffer[i].toInt())

            if (sample > peak) {

                peak = sample

                peakIndex = i

            }

            energy += sample * sample.toDouble()

            if (i > 0) {

                if (
                    (buffer[i] >= 0 && buffer[i - 1] < 0) ||
                    (buffer[i] < 0 && buffer[i - 1] >= 0)
                ) {
                    zeroCrossings++
                }

            }

            if (!reachedPeak) {

                if (sample > previous) {

                    attack++

                } else {

                    reachedPeak = true

                }

            } else {

                if (sample < previous) {

                    decay++

                }

            }

            if (sample > threshold) {

                lastAboveThreshold = i

            }

            previous = sample

        }

        impulseWidth = lastAboveThreshold - peakIndex

        if (impulseWidth < 0) {

            impulseWidth = 0

        }

        val rms = sqrt(
            energy / buffer.size
        )

        return SignalFeatures(

            peak = peak,

            rms = rms,

            zeroCrossings = zeroCrossings,

            attack = attack,

            decay = decay,

            impulseWidth = impulseWidth

        )

    }

}
