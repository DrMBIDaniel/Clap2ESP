package com.clap2esp.app

import kotlin.math.abs


enum class ClapType {
    NONE,
    SINGLE_CLAP,
    DOUBLE_CLAP
}



class ClapDetector {


    private val threshold = 12000


    private val doubleClapInterval = 800L


    private val minClapInterval = 150L


    private var firstClapTime = 0L


    private var waitingSecondClap = false


    private var lastDetectionTime = 0L



    fun detect(buffer: ShortArray): ClapType {


        var maxAmplitude = 0



        for (sample in buffer) {


            val amplitude =
                abs(sample.toInt())


            if (amplitude > maxAmplitude) {

                maxAmplitude = amplitude

            }

        }



        val currentTime =
            System.currentTimeMillis()



        if (maxAmplitude < threshold) {

            return ClapType.NONE

        }



        if (
            currentTime - lastDetectionTime
            < minClapInterval
        ) {

            return ClapType.NONE

        }



        lastDetectionTime = currentTime





        if (!waitingSecondClap) {


            waitingSecondClap = true


            firstClapTime = currentTime



            Logger.log(
                "First clap amplitude=$maxAmplitude"
            )



            return ClapType.NONE

        }





        val delay =
            currentTime - firstClapTime




        if (delay <= doubleClapInterval) {


            waitingSecondClap = false



            Logger.log(
                "Double clap delay=${delay}ms"
            )



            return ClapType.DOUBLE_CLAP

        }



        firstClapTime = currentTime


        return ClapType.NONE

    }





    fun checkSingleClapTimeout(): ClapType {


        if (
            waitingSecondClap &&
            System.currentTimeMillis()
            - firstClapTime > doubleClapInterval
        ) {


            waitingSecondClap = false



            Logger.log(
                "Single clap detected"
            )


            return ClapType.SINGLE_CLAP

        }


        return ClapType.NONE

    }

}
