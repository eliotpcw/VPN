package kz.kazpost.vpn.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kz.kazpost.vpn.di.CoroutineProvider
import kz.kazpost.vpn.utils.EventWrapper
import kz.kazpost.vpn.utils.ResourceString
import org.koin.core.KoinComponent

abstract class BaseViewModel(
    protected val coroutineProvider: CoroutineProvider = CoroutineProvider(),
    protected val coroutineJob: Job = SupervisorJob(),
    protected val scope: CoroutineScope = CoroutineScope(coroutineJob + coroutineProvider.IO),

    private val _statusLiveData: MutableLiveData<Status> = MutableLiveData(),
    private val _errorLiveData: MutableLiveData<EventWrapper<ResourceString>> = MutableLiveData(),

    protected val uiCaller: UiCaller = UiCallerImpl(
        scope,
        coroutineProvider,
        _statusLiveData,
        _errorLiveData
    )
) : ViewModel(), UiProvider by uiCaller, KoinComponent {
    override fun onCleared() {
        super.onCleared()
        coroutineJob.cancel()
    }
}