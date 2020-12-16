package kz.kazpost.vpn.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import kz.kazpost.vpn.data.network.RequestResult
import kz.kazpost.vpn.di.CoroutineProvider
import kz.kazpost.vpn.utils.EventWrapper
import kz.kazpost.vpn.utils.ResourceString
import kz.kazpost.vpn.utils.TextResourceString

interface UiProvider {
    val statusLiveData: LiveData<Status>
    val errorLiveData: LiveData<EventWrapper<ResourceString>>
}

interface UiCaller : UiProvider {
    override val statusLiveData: MutableLiveData<Status>

    fun <T> makeRequest(
        call: suspend CoroutineScope.() -> T,
        statusLD: MutableLiveData<Status>? = statusLiveData,
        resultBlock: (suspend (T) -> Unit)? = null
    )

    fun <T> unwrap(
        result: RequestResult<T>,
        errorBlock: ((ResourceString) -> Unit)? = { setError(it) },
        successBlock: (T) -> Unit
    ): Unit?

    fun set(status: Status, statusLD: MutableLiveData<Status>? = statusLiveData)

    fun setError(error: ResourceString)
}

class UiCallerImpl(
    private val scope: CoroutineScope,
    private val scopeProvider: CoroutineProvider,
    _statusLiveData: MutableLiveData<Status>,
    _errorLiveData: MutableLiveData<EventWrapper<ResourceString>>
) : UiCaller {
    override val statusLiveData: MutableLiveData<Status> = _statusLiveData
    override val errorLiveData: MutableLiveData<EventWrapper<ResourceString>> = _errorLiveData

    /**
     * Presentation-layer-обработчик для запросов через `kotlin coroutines`:
     * запускает [Job] в [scope],
     * вызывает прогресс на [statusLiveData]
     *
     * [call] - `suspend`-функция запроса из репозитория
     * [statusLD] - можно подставлять разные liveData для разных прогрессов (или null)
     * [resultBlock] - функция, которую нужно выполнить по завершении запроса в UI-потоке
     */
    override fun <T> makeRequest(
        call: suspend CoroutineScope.() -> T,
        statusLD: MutableLiveData<Status>?,
        resultBlock: (suspend (T) -> Unit)?
    ) {
        scope.launch(scopeProvider.Main) {
            set(Status.SHOW_LOADING, statusLD)
            try {
                val result = withContext(scopeProvider.IO, call)
                resultBlock?.invoke(result)
            } catch (e: Exception) {
                if (e !is CancellationException) setError(TextResourceString(e.message.orEmpty()))
            }
            set(Status.HIDE_LOADING, statusLD)
        }
    }

    /**
     * Чтобы не терять прогрессбар на нескольких запросах
     */
    private var requestCounter = 0

    /**
     * Выставляем статус
     * по дефолту выставлен [statusLiveData]
     * можно подставить свою лайвдату или [null]
     */
    override fun set(status: Status, statusLD: MutableLiveData<Status>?) {
        statusLD ?: return
        if (statusLD === statusLiveData) {
            when (status) {
                Status.SHOW_LOADING -> {
                    requestCounter++
                }
                Status.HIDE_LOADING -> {
                    requestCounter--
                    if (requestCounter > 0) return
                    requestCounter = 0
                }
            }
        }
        scope.launch(scopeProvider.Main) {
            statusLD.value = status
        }
    }

    override fun setError(error: ResourceString) {
        scope.launch(scopeProvider.Main) {
            errorLiveData.value = EventWrapper(error)
        }
    }

    /**
     * Обработчик для ответов [RequestResult] репозитория.
     * [errorBlock] - функция обработки ошибок, можно передать `null`, чтобы никак не обрабатывать.
     * [successBlock] - обработка непустого результата
     */
    override fun <T> unwrap(
        result: RequestResult<T>,
        errorBlock: ((ResourceString) -> Unit)?,
        successBlock: (T) -> Unit
    ) = when (result) {
        is RequestResult.Success -> successBlock(result.result)
        is RequestResult.Error -> errorBlock?.invoke(result.error)
    }
}