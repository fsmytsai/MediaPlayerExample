package com.fsmytsai.mediaplayerexample

import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.media.MediaPlayer
import android.os.AsyncTask
import android.widget.*


class MainActivity : AppCompatActivity() {

    private var rv_MusicList: RecyclerView? = null
    private var audioListAdapter: AudioListAdapter? = null
    private var mainActivity: MainActivity? = null
    private var iv_Background: ImageView? = null
    private var tv_NowAudioName: TextView? = null
    private var tv_NowTime: TextView? = null
    private var tv_TotalTime: TextView? = null
    private var sb_Control: SeekBar? = null
    private var ib_StopOrStart: ImageButton? = null

    private var isPlaying = true
    private var nowPosition = 0
    private val audioNameArr = arrayOf("music1", "music3")
    private var mediaPlayer: MediaPlayer? = null
    private var myPlayTask = MyPlayTask()
    private var mediaPlayer2: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        startPlay()

        startBackMusic()
    }

    override fun onStop() {
        super.onStop()
        myPlayTask?.cancel(true)
        mediaPlayer?.release()
        mediaPlayer2?.release()
    }

    private fun initViews() {
        tv_NowAudioName = findViewById(R.id.tv_NowAudioName)

        tv_NowTime = findViewById(R.id.tv_NowTime)
        tv_TotalTime = findViewById(R.id.tv_TotalTime)

        iv_Background = findViewById(R.id.iv_Background)
        iv_Background?.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)

        sb_Control = findViewById(R.id.sb_Control)


        sb_Control?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // 移動音樂到指定的進度
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        rv_MusicList = findViewById(R.id.rv_MusicList)
        rv_MusicList?.setLayoutManager(LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false))
        audioListAdapter = AudioListAdapter()
        rv_MusicList?.setAdapter(audioListAdapter)

        findViewById<ImageButton>(R.id.ib_Previous).setOnClickListener(onClickListener)
        ib_StopOrStart = findViewById(R.id.ib_StopOrStart)
        ib_StopOrStart?.setOnClickListener(onClickListener)
        findViewById<ImageButton>(R.id.ib_Next).setOnClickListener(onClickListener)
    }

    private fun startPlay() {
        val uri = Uri.parse("android.resource://${packageName}/raw/${audioNameArr[nowPosition]}")
        mediaPlayer = MediaPlayer.create(this, uri)
        mediaPlayer?.setOnCompletionListener {
            isPlaying = false
            myPlayTask.cancel(true)
            ib_StopOrStart?.setImageResource(R.drawable.ic_play_arrow_white_48dp)
        }
        mediaPlayer?.start()

        setData()

        myPlayTask = MyPlayTask()
        myPlayTask.execute()
    }

    private fun stopPlay() {
        mediaPlayer?.stop()
        myPlayTask.cancel(true)
    }

    private fun startBackMusic() {
        val uri2 = Uri.parse("android.resource://${packageName}/raw/music2")
        mediaPlayer2 = MediaPlayer.create(this, uri2)
        mediaPlayer2?.setOnCompletionListener {
            mediaPlayer2?.start()
        }
        mediaPlayer2?.start()
    }

    private fun setData() {
        tv_NowAudioName?.text = audioNameArr[nowPosition]
        tv_NowTime?.text = formatTime(0)
        tv_TotalTime?.text = "/${formatTime(mediaPlayer?.duration!!)}"
        sb_Control?.max = mediaPlayer?.duration!!
        sb_Control?.progress = 0
    }

    inner class AudioListAdapter : RecyclerView.Adapter<AudioListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context

            val view = LayoutInflater.from(context).inflate(R.layout.block_audioinfo, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.tv_AudioName.text = audioNameArr[position]
            holder.ll_AudioBlock.setOnClickListener {
                nowPosition = position
                stopPlay()
                startPlay()
            }
        }

        override fun getItemCount(): Int {
            return audioNameArr.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ll_AudioBlock: LinearLayout
            val tv_AudioName: TextView

            init {
                ll_AudioBlock = itemView.findViewById(R.id.ll_AudioBlock)
                tv_AudioName = itemView.findViewById(R.id.tv_AudioName)
            }
        }
    }

    private val onClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.ib_Previous -> {
                if (nowPosition >= 1)
                    nowPosition -= 1
                else
                    nowPosition = audioNameArr.size - 1
                stopPlay()
                startPlay()
            }
            R.id.ib_StopOrStart -> {
                if (isPlaying) {
                    isPlaying = false
                    if (mediaPlayer != null) {
                        mediaPlayer?.pause()
                    }
                    myPlayTask.cancel(true)
                    ib_StopOrStart?.setImageResource(R.drawable.ic_play_arrow_white_48dp)
                } else {
                    isPlaying = true
                    mediaPlayer?.start()
                    myPlayTask = MyPlayTask()
                    myPlayTask.execute()
                    ib_StopOrStart?.setImageResource(R.drawable.ic_pause_white_48dp)
                }
            }
            R.id.ib_Next -> {
                if (nowPosition < audioNameArr.size - 1)
                    nowPosition += 1
                else
                    nowPosition = 0
                stopPlay()
                startPlay()
            }
        }
    }

    private inner class MyPlayTask : AsyncTask<Void, Int, Void>() {
        override fun doInBackground(vararg voids: Void): Void? {
            while (mediaPlayer?.isPlaying()!!) {
                this.publishProgress(mediaPlayer?.getCurrentPosition())
            }
            return null
        }

        // 設定播放進度
        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            tv_NowTime?.text = formatTime(values[0]!!)
            sb_Control?.setProgress(values[0]!!)
        }
    }

    private fun formatTime(time: Int): String {
        val totalSeconds: Int = (time / 1000)
        val seconds: Int = totalSeconds % 60
        val minutes: Int = (totalSeconds / 60) % 60
        val hours: Int = totalSeconds / 3600
        return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes,
                seconds) else String.format("%02d:%02d", minutes, seconds)
    }
}
