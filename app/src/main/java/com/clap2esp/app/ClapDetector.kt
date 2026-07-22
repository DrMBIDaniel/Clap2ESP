package com.clap2esp.app

class ClapDetector(
    private val noiseEstimator: NoiseEstimator,
    private val adaptiveThreshold: AdaptiveThreshold,
    private val decisionSmoother: DecisionSmoother
) {

    private var waitingSecondClap = false
    private var pendingSingle = false
    private var firstClapTime = 0L

    private val minDoubleDelay = 90L
    private val maxDoubleDelay = 450L
    private val singleTimeout = 550L

    fun detect(features: SignalFeatures): ClapType {

       val score = calculateScore(features)

adaptiveThreshold.update(features)

val clapDetected =
    decisionSmoother.accept(
        features.peak > adaptiveThreshold.currentThreshold()
    )

        if (!clapDetected) {
            return ClapType.NONE
        }

        val now = System.currentTimeMillis()

        if (!waitingSecondClap) {

            waitingSecondClap = true
            pendingSingle = true
            firstClapTime = now

            Logger.log(
                "Candidate score=$score threshold=${adaptiveThreshold.currentThreshold()}"
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

        if (f.rms > noiseEstimator.noiseRms() * 2.2)
            score++

        if (f.peak > noiseEstimator.noisePeak() * 2.0)
            score++

        if (f.highFrequencyRatio >
            noiseEstimator.noiseHighRatio() + 0.08)
            score++

        if (f.highBandEnergy > f.lowBandEnergy)
            score++

        if (f.zeroCrossings > 20)
            score++

        if (f.impulseWidth < 450)
            score++

        if (f.attack < 200)
            score++

        if (f.clapFrequencyScore > 0.55)
            score++

        if (f.spectralPeak > 1800)
            score++

        if (f.spectralCentroid > 1500)
            score++

        if (f.spectralFlatness > 0.15)
            score++

        Logger.log(
            "Score=$score Threshold=${adaptiveThreshold.currentThreshold()}"
        )

        return score
    }
}
