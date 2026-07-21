package com.clap2esp.app

class ClapDetector(
    private val noiseEstimator: NoiseEstimator
) {

    private var waitingSecondClap = false
    private var pendingSingle = false
    private var firstClapTime = 0L

    private val minDoubleDelay = 90L
    private val maxDoubleDelay = 450L
    private val singleTimeout = 550L

    fun detect(features: SignalFeatures): ClapType {

        if (!isClap(features)) {
            return ClapType.NONE
        }

        val now = System.currentTimeMillis()

        if (!waitingSecondClap) {

            waitingSecondClap = true
            pendingSingle = true
            firstClapTime = now

            Logger.log(
                "CLAP CANDIDATE score=${calculateScore(features)}"
            )

            return ClapType.NONE
        }

        val delay = now - firstClapTime

        if (delay in minDoubleDelay..maxDoubleDelay) {

            waitingSecondClap = false
            pendingSingle = false

            Logger.log("DOUBLE CLAP")

            return ClapType.DOUBLE_CLAP
        }

        firstClapTime = now

        return ClapType.NONE
    }

    fun checkSingleClapTimeout(): ClapType {

        if (!waitingSecondClap)
            return ClapType.NONE

        if (
            pendingSingle &&
            System.currentTimeMillis() - firstClapTime > singleTimeout
        ) {

            waitingSecondClap = false
            pendingSingle = false

            Logger.log("SINGLE CLAP")

            return ClapType.SINGLE_CLAP
        }

        return ClapType.NONE
    }

    private fun calculateScore(f: SignalFeatures): Int {

        if (!noiseEstimator.isInitialized())
            return 0

        var score = 0

        val rmsLimit =
            noiseEstimator.noiseRms() * 2.0

        val peakLimit =
            noiseEstimator.noisePeak() * 2.0

        if (f.rms > rmsLimit)
            score++

        if (f.peak > peakLimit)
            score++

        if (
            f.highFrequencyRatio >
            noiseEstimator.noiseHighRatio() + 0.08
        )
            score++

        if (f.zeroCrossings > 20)
            score++

        if (f.impulseWidth < 450)
            score++

        if (f.attack < 200)
            score++

        if (f.clapFrequencyScore > 0.55)
            score++

        if (f.highBandEnergy > f.midBandEnergy)
            score++

        if (f.midBandEnergy > f.lowBandEnergy)
            score++

        if (f.spectralPeak > 1000)
            score++

        if (
            f.spectralCentroid > 1800 &&
            f.spectralCentroid < 8000
        )
            score++

        if (
            f.spectralFlatness > 0.18 &&
            f.spectralFlatness < 0.90
        )
            score++

        return score
    }

    private fun isClap(f: SignalFeatures): Boolean {

        val score = calculateScore(f)

        Logger.log(
            "score=$score " +
            "peak=${f.peak} " +
            "rms=${f.rms.toInt()} " +
            "centroid=${f.spectralCentroid.toInt()} " +
            "flatness=${"%.2f".format(f.spectralFlatness)}"
        )

        return score >= 8
    }
}
