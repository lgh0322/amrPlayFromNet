package com.vaca.amrplayfromnet

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.vaca.amrplayfromnet.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding

    lateinit var mAudioTrack: AudioTrack

    lateinit  var channel: DatagramChannel
    private val bufReceive: ByteBuffer = ByteBuffer.allocate(12000)

    private val recorderThread by lazy {
        Executors.newFixedThreadPool(1000)
    }
    fun bytebuffer2ByteArray(buffer: ByteBuffer): ByteArray? {
        buffer.flip()
        val len = buffer.limit() - buffer.position()
        val bytes = ByteArray(len)
        for (i in bytes.indices) {
            bytes[i] = buffer.get()
        }
        return bytes
    }


    fun initUdp(){
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(InetSocketAddress(8888));
        } catch (e: IOException) {

            e.printStackTrace();
        }
    }

    fun startListen() {
        while (true) {
            try {
                bufReceive.clear()
                channel.receive(bufReceive)
                val receiveByteArray=bytebuffer2ByteArray(bufReceive)
                if (receiveByteArray != null) {
                    Log.e("fu8ck",receiveByteArray.size.toString())
                   mAudioTrack.write(receiveByteArray,0,receiveByteArray.size)



                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val buf: ByteBuffer = ByteBuffer.allocate(600)
    fun send(message: String) {
        try {
            val configInfo = message.toByteArray()
            buf.clear()
            buf.put(configInfo)
            buf.flip()
            channel.send(buf, InetSocketAddress("192.168.6.110", 8888))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUdp()
        Thread{
            send("{}")
        }.start()
        Thread{
            Thread.sleep(100)
            startListen()
        }.start()
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            8000, AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT, 160000, AudioTrack.MODE_STREAM
        )
        mAudioTrack.play()
    }
}