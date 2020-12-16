package kz.kazpost.vpn.ui.acceptinvoice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kz.kazpost.vpn.data.network.RequestResult
import kz.kazpost.vpn.models.RequestLabels
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.models.VPNData
import kz.kazpost.vpn.ui.BaseViewModel
import kz.kazpost.vpn.utils.EventWrapper
import kz.kazpost.vpn.utils.PreferenceHelper

class AcceptInvoiceViewModel(
    private val repository: AcceptInvoiceRepository
): BaseViewModel(){

    private val _responseVpnData: MutableLiveData<EventWrapper<VPNData>> = MutableLiveData()
    internal val responseVpnData: LiveData<EventWrapper<VPNData>>
        get() = _responseVpnData

    private val _responseStationData: MutableLiveData<EventWrapper<StationData>> = MutableLiveData()
    internal val responseStationData: LiveData<EventWrapper<StationData>>
        get() = _responseStationData

    private val _responseSendLabels: MutableLiveData<EventWrapper<Boolean>> = MutableLiveData()
    internal val responseSendLabels: LiveData<EventWrapper<Boolean>>
        get() = _responseSendLabels

    fun getVpnData(login: String){
        uiCaller.makeRequest({
            repository.getVpnData(login)
        }){
            when(it){
                is RequestResult.Success -> _responseVpnData.postValue(EventWrapper(it.result))
                is RequestResult.Error -> uiCaller.setError(it.error)
            }
        }
    }

    fun getDataOfStation(
        tid: String?,
        index: Int?
    ){
        uiCaller.makeRequest({
            repository.getDataOfStation(tid, index)
        }){
            when(it){
                is RequestResult.Success -> {
                    _responseStationData.postValue(EventWrapper(it.result))
                }
                is RequestResult.Error -> uiCaller.setError(it.error)
            }
        }
    }

    fun verifyLabels(requestLabels: RequestLabels){
        uiCaller.makeRequest({
            repository.verifyLabels(requestLabels)
        }){
            when(it){
                is RequestResult.Success -> _responseSendLabels.postValue(EventWrapper(true))
                is RequestResult.Error -> uiCaller.setError(it.error)
            }
        }
    }
}