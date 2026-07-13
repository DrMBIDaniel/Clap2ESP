package com.clap2esp.app


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log



class AudioService : Service() {



    private var audioRecord: AudioRecord? = null


    private var isRecording = false



    private val clapDetector =
        ClapDetector()



    private val channelId =
        "Clap2ESP_Channel"






    override fun onCreate() {

        super.onCreate()



        Logger.log(
            "AudioService created"
        )



        createNotificationChannel()



        val notification =
            Notification.Builder(
                this,
                channelId
            )
                .setContentTitle(
                    "Clap2ESP"
                )
                .setContentText(
                    "Listening for claps..."
                )
                .setSmallIcon(
                    android.R.drawable.ic_menu_info_details
                )
                .build()



        startForeground(
            1,
            notification
        )


        Logger.log(
            "Foreground service started"
        )

    }







    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {


        startListening()


        return START_NOT_STICKY

    }








    private fun startListening() {



        if (isRecording) {

            return

        }




        val bufferSize =
            AudioRecord.getMinBufferSize(
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )





        audioRecord =
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )





        audioRecord?.startRecording()



        isRecording = true



        Logger.log(
            "Microphone recording started"
        )






        Thread {



            val buffer =
                ShortArray(bufferSize)





            while(isRecording) {



                val read =
                    audioRecord?.read(
                        buffer,
                        0,
                        buffer.size
                    )





                if (
                    read != null &&
                    read > 0
                ) {



                    when(
                        clapDetector.detect(buffer)
                    ) {


                        ClapType.DOUBLE_CLAP -> {


                            Logger.log(
                                "DOUBLE CLAP EVENT"
                            )


                            sendBroadcast(
                                Intent(
                                    "DOUBLE_CLAP"
                                )
                            )


                        }



                        else -> {

                        }

                    }






                    when(
                        clapDetector.checkSingleClapTimeout()
                    ) {



                        ClapType.SINGLE_CLAP -> {


                            Logger.log(
                                "SINGLE CLAP EVENT"
                            )


                            sendBroadcast(
                                Intent(
                                    "SINGLE_CLAP"
                                )
                            )


                        }



                        else -> {

                        }


                    }



                }


            }



        }.start()



    }








    private fun createNotificationChannel() {



        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {


            val channel =
                NotificationChannel(
                    channelId,
                    "Clap2ESP Listener",
                    NotificationManager.IMPORTANCE_LOW
                )



            val manager =
                getSystemService(
                    NotificationManager::class.java
                )



            manager.createNotificationChannel(
                channel
            )


        }


    }








    override fun onDestroy() {


        Logger.log(
            "AudioService stopped"
        )



        isRecording = false




        try {

            audioRecord?.stop()


        } catch(e: Exception) {


            Log.e(
                "CLAP",
                "Stop error"
            )

        }




        audioRecord?.release()


        audioRecord = null



        super.onDestroy()


    }







    override fun onBind(intent: Intent?): IBinder? {

        return null

    }


}
