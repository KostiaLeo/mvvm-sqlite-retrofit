package com.example.restkotlinized.model

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.LifecycleOwner
import com.example.restkotlinized.model.remote.ArtistsRemoteSource
import com.example.restkotlinized.model.remote.OnDataRemoteReadyCallback
import com.example.restkotlinized.model.sqlite.ArtistsLocalSource
import com.example.restkotlinized.model.sqlite.OnDataLocalReadyCallback
import com.example.restkotlinized.model.sqlite.ArtistViewModel
import kotlinx.coroutines.CoroutineScope

class ModelRepository(
    private val applicationContext: Context,
    val lifecycleOwner: LifecycleOwner,
    val coroutineScope: CoroutineScope
) {
    private val remoteSource = ArtistsRemoteSource()
    private val artistsViewModel: ArtistViewModel = ArtistViewModel(applicationContext, coroutineScope)

    private lateinit var localSource: ArtistsLocalSource
    private lateinit var onDataReadyCallback: OnDataReadyCallback

    fun retrieveData(onDataReadyCallback: OnDataReadyCallback) {
        this.onDataReadyCallback = onDataReadyCallback

        NetManager(applicationContext).isConnectedToInternet?.let {
            if (it) {
                retrieveRemoteData()
            } else {
                retrieveLocalData()
            }
        }
    }

    private fun retrieveRemoteData(){
        remoteSource.retrieveData(object : OnDataRemoteReadyCallback {
            override fun onRemoteDataReady(artists: ArrayList<Results>) {
                onDataReadyCallback.onDataReady(artists)
                localSource.saveData(artists)
            }
        })
    }

    private fun retrieveLocalData(){
        localSource = ArtistsLocalSource(artistsViewModel, lifecycleOwner)
        localSource.retrieveData(object : OnDataLocalReadyCallback {
            override fun onLocalDataReady(artists: ArrayList<Results>) {
                onDataReadyCallback.onDataReady(artists)
            }
        })
    }
}

interface OnDataReadyCallback {
    fun onDataReady(artists: ArrayList<Results>)
}

private class NetManager(private val applicationContext: Context) {

    val isConnectedToInternet: Boolean?
        @SuppressLint("MissingPermission")
        get() {
            val conManager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = conManager.activeNetworkInfo
            return network != null && network.isConnectedOrConnecting
        }
}