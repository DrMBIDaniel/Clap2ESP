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

        if (!isClap(features)) {
            return ClapType.NONE
        }

        val now = System.currentTimeMillis()

        if (!waitingSecondClap) {

            waitingSecondClap = true
            pendingSingle = true
            firstClapTime = now

            Logger.log("CLAP")

            return ClapType.NONE
        }

        val delay = now - firstClapTime

        if (delay in minDoubleDelay..maxDoubleDelay) {

            waitingSecondClap = false
            pendingSingle = false
            decisionSmoother.clear()

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
            decisionSmoother.clear()

            Logger.log("SINGLE CLAP")

            return ClapType.SINGLE_CLAP
        }

        return ClapType.NONE
    }

    private fun isClap(f: SignalFeatures): Boolean {

        if (!noiseEstimator.isInitialized())
            return false

        var score = 0

        if (f.rms > noiseEstimator.noiseRms() * 2.0)
            score++

        if (f.peak > noiseEstimator.noisePeak() * 2.0)
            score++

        if (f.highFrequencyRatio >
            noiseEstimator.noiseHighRatio() + 0.08)
            score++

        if (f.highBandEnergy > f.midBandEnergy)
            score++

        if (f.highBandEnergy > f.lowBandEnergy)
            score++

        if (f.zeroCrossings > 20)
            score++

        if (f.impulseWidth < 450)
            score++

        if (f.attack < 200)
            score++

        if (f.spectralFlatness > 0.18)
            score++

        if (f.spectralPeak > 1500)
            score++

        if (f.spectralCentroid > 1200)
            score++

        adaptiveThreshold.update(score)

        val candidate =
            score >= adaptiveThreshold.currentThreshold()

        val accepted =
            decisionSmoother.accept(candidate)

        Logger.log(
            "Score=$score Threshold=${adaptiveThreshold.currentThreshold()} Accepted=$accepted"
        )

        return accepted
    }
}
