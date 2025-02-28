package com.sonos.volume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sonos.volume.databinding.FragmentFirstBinding

private const val TAG = "Volume"

class FirstFragment : Fragment()
{
    private var _binding: FragmentFirstBinding? = null
    private var volumeObserver: VolumeObserver? = null
    private val volumeReceiver = VolumeChangeReceiver()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var audioManager: AudioManager
    private var uiFlag = AudioManager.FLAG_SHOW_UI

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        binding.buttonRefresh.setOnClickListener {volumeRefresh() }
        binding.chip.setOnClickListener { chipUi() }
        binding.buttonUpAbs.setOnClickListener { volumeUpAbs() }
        binding.buttonUpInc.setOnClickListener { volumeUpInc() }
        binding.buttonMuteAbs.setOnClickListener { volumeMuteAbs() }
        binding.buttonMuteInc.setOnClickListener { volumeMuteInc() }

        // Register the observer in an activity or service
        val handler = Handler(Looper.getMainLooper())
        volumeObserver = VolumeObserver(handler, requireContext())
        requireContext().contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI,
            true,
            volumeObserver!!)
        val intentFilter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        requireContext().registerReceiver(volumeReceiver, intentFilter)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

    private fun chipUi()
    {
        Log.d(TAG, "chipUi")
        if (binding.chip.isChecked)
        {
            uiFlag = AudioManager.FLAG_SHOW_UI
        }
        else
        {
            uiFlag = 0
        }
    }

    private fun volumeRefresh()
    {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val isFixed = audioManager.isVolumeFixed()
        binding.textviewVolume.text = "STREAM MUSIC: $currentVolume"
        binding.textviewFixed.text = "Is Fixed: $isFixed"
    }

    private fun volumeUpAbs()
    {
        Log.d(TAG, "volumeUpAbs 10")
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 10, uiFlag)
    }

    private fun volumeUpInc()
    {
        Log.d(TAG, "volumeUpInc")
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, uiFlag)
    }

    private fun volumeMuteAbs() {
        Log.d(TAG, "volumeMuteAbs")
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, uiFlag)
    }

    private fun volumeMuteInc() {
        Log.d(TAG, "volumeMuteInc")
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, uiFlag)
    }

    inner class VolumeChangeReceiver : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            if (intent.action == "android.media.VOLUME_CHANGED_ACTION")
            {
                val stream = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                val volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1)
//                val prevVolume = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1)
                binding.textviewVolumeBroadcast.text = "Broadcast volume: $stream $volume"
            }
        }
    }

    inner class VolumeObserver(
        handler: Handler,
        private val context: Context) : ContentObserver(handler) {
        private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        override fun onChange(selfChange: Boolean, uri: Uri?)
        {
            super.onChange(selfChange, uri)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            binding.textviewVolumeObserver.text = "Observer volume: $currentVolume"
        }
    }
}