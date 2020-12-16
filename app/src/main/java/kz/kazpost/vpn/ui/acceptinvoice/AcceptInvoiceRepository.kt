package kz.kazpost.vpn.ui.acceptinvoice

import kz.kazpost.vpn.data.network.ApiCaller
import kz.kazpost.vpn.data.network.ApiCallerInterface
import kz.kazpost.vpn.data.network.api.IApi
import kz.kazpost.vpn.models.RequestLabels

class AcceptInvoiceRepository(
    private val api: IApi
): ApiCallerInterface by ApiCaller {

    suspend fun getVpnData(login: String) = apiCall{
        api.getVpnData(login)
    }

    suspend fun getDataOfStation(
        tid: String?,
        index: Int?
    ) = apiCall {
        api.getDataOfStation(tid, index)
    }

    suspend fun verifyLabels(requestLabels: RequestLabels) = apiCall {
        api.verifyLabels(requestLabels)
    }
}