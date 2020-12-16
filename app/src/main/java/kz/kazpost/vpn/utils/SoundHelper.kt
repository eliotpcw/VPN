package kz.kazpost.vpn.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log


class SoundHelper
private constructor(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private var soundManagerInstance: SoundHelper? = null
        fun getInstance(context: Context): SoundHelper? {
            if (soundManagerInstance == null) {
                soundManagerInstance = SoundHelper(context.applicationContext)
            }
            return soundManagerInstance
        }
    }

    fun playSound() {
        val ringtoneUri = alarmUri
        mediaPlayer = MediaPlayer.create(context, ringtoneUri)
        if (mediaPlayer != null) {
            Log.d("INFOMESSAGE", "MediaPlayer playing.")
            mediaPlayer!!.isLooping = true
            mediaPlayer!!.start()
        }
    }

    fun stopSound() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    private val alarmUri: Uri?
         get() {
            var alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (alert == null) {
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }
            }
            return alert
        }

}