package `in`.ecsolution.logosquiz

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var bgmm: MediaPlayer? = null
    private var activityCount = 1

    private fun playMusic(context: Context) {
        if (bgmm == null) {
            bgmm = MediaPlayer.create(context, R.raw.home_page_music).apply {
                isLooping = true
                setVolume(1f, 1f)
                start()
            }
        } else {
            bgmm?.start()
        }
    }

    fun stopMusic() {
        bgmm?.release()
        bgmm = null
        activityCount=0
    }

    fun pauseMusic() {
        bgmm?.pause()
    }

    fun resumeMusic() {
        bgmm?.start()
    }

    fun onActivityCreated(playMusic: Boolean, context: Context) {
        activityCount++
        if (playMusic) {
            playMusic(context)
        }
    }

    fun onActivityDestroyed(playMusic: Boolean) {
        activityCount--
        if (playMusic) {
            if (activityCount == -1) {
                stopMusic()
            }
        }
    }
}
