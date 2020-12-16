package kz.kazpost.vpn.data.network.api

import kz.kazpost.vpn.models.RequestLabels
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.models.VPNData
import okhttp3.ResponseBody
import retrofit2.http.*

interface IApi{
    @GET("wagon-items/getVPNdata")
    suspend fun getVpnData(@Query("login") login: String): VPNData

    @GET("wagon-items/getDataForStation")
    suspend fun getDataOfStation(@Query("tid") tid: String?,
            @Query("index") index: Int?): StationData

    @Headers("Content-Type: application/json")
    @POST("wagon-items/verifyLabelsForVPN")
    suspend fun verifyLabels(@Body body: RequestLabels): ResponseBody
}