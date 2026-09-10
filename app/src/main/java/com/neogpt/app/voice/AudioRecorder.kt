package com.neogpt.app.voice

import android.media.MediaRecorder
import android.content.Context
import java.io.File

class AudioRecorder(context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(outputDir: File) {
        outputFile = File(outputDir, "recording_${System.currentTimeMillis()}.m4a")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile?.absolutePath)
            prepare()
            start()
        }
    }

    fun stop(): File? {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        return outputFile
    }
}
