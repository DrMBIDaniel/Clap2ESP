package com.clap2esp.app


class ClapDetector {


    private var waitingForSecondClap = false


    private var firstClapTime = 0L



    // Максимальное время между двумя хлопками
    private val doubleWindow = 500L





    fun detect(
        features: SignalFeatures
    ): ClapType {



        val now = System.currentTimeMillis()





        /*
        Фильтр голоса

        Голос обычно:
        - больше длительность
        - медленнее атака
        - меньше импульсность
        */


        if (

            features.attack > 120 ||

            features.impulseWidth > 350

        ) {


            return ClapType.NONE

        }





        /*
        Основной фильтр хлопка


        Хлопок:
        - высокий пик
        - быстрый фронт
        - много переходов через ноль
        - короткий импульс

        */


        val clapCandidate =


            features.peak > 8000 &&

            features.attack < 100 &&

            features.impulseWidth < 250 &&

            features.zeroCrossings > 20





        if (!clapCandidate) {


            return ClapType.NONE

        }





        /*
        Первый хлопок
        */


        if (!waitingForSecondClap) {


            waitingForSecondClap = true


            firstClapTime = now



            Logger.log(
                "First clap candidate peak=${features.peak}"
            )



            return ClapType.NONE

        }





        /*
        Второй хлопок
        */


        val delay =
            now - firstClapTime



        waitingForSecondClap = false





        if (delay <= doubleWindow) {


            Logger.log(
                "Double clap delay=${delay}ms"
            )


            return ClapType.DOUBLE_CLAP

        }




        return ClapType.NONE

    }








    fun checkSingleClapTimeout(): ClapType {



        if (

            waitingForSecondClap &&

            System.currentTimeMillis() -
            firstClapTime >
            doubleWindow

        ) {



            waitingForSecondClap = false



            Logger.log(
                "Single clap detected"
            )



            return ClapType.SINGLE_CLAP

        }



        return ClapType.NONE

    }


}
