package com.clap2esp.app

class NoiseEstimator {

    private var initialized = false

    private var frames = 0

    private var rms = 0.0
    private var peak = 0.0
    private var highRatio = 0.0

    private val learningFrames = 80

    private val alpha = 0.03

    fun update(features: SignalFeatures) {

        if (!initialized) {

            rms += features.rms
            peak += features.peak
            highRatio += features.highFrequencyRatio

            frames++

            if (frames >= learningFrames) {

                rms /= frames
                peak /= frames
                highRatio /= frames

                initialized = true

                Logger.log(
                    "Noise profile initialized " +
                    "RMS=${rms.toInt()} " +
                    "Peak=${peak.toInt()}"
                )
            }

            return
        }

        /*
         * Не обучаемся на явных хлопках.
         */

        if (
            features.peak > peak * 3.0 ||
            features.rms > rms * 3.0
        ) {
            return
        }

        /*
         * Медленно адаптируемся
         * к изменению окружающего шума.
         */

        rms =
            rms * (1.0 - alpha) +
            features.rms * alpha

        peak =
            peak * (1.0 - alpha) +
            features.peak * alpha

        highRatio =
            highRatio * (1.0 - alpha) +
            features.highFrequencyRatio * alpha
    }

    fun isInitialized(): Boolean {
        return initialized
    }

    fun noiseRms(): Double {
        return rms
    }

    fun noisePeak(): Double {
        return peak
    }

    fun noiseHighRatio(): Double {
        return highRatio
    }
}
