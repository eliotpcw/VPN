package kz.kazpost.vpn.utils

import kz.kazpost.vpn.acts.models.RequestAct30
import kz.kazpost.vpn.acts.models.RequestAct51
import kz.kazpost.vpn.acts.models.RequestActLabel
import kz.kazpost.vpn.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class HTTPHelper {

    interface Authorisation {
        @Headers("Content-Type: application/json")
        @POST("security/login")
        fun post(@Body body: RequestAuthorisation): Call<Auth>
    }

    interface DataForStation {
        @GET("wagon-items/getDataForStation")
        fun get(@Query("tid") tid: String,
                @Query("index") index: Int): Call<StationData>
    }

    interface VerifyItems {
        @Headers("Content-Type: application/json")
        @POST("wagon-items/verifyItemsForVPN")
        fun post(@Body body: RequestItems): Call<ResponseBody>
    }

    interface SendItems {
        @Headers("Content-Type: application/json")
        @POST("wagon-items/sendInfoForVPN")
        fun post(@Body body: RequestItems): Call<ResponseBody>
    }

    interface ActLabels{
        @Headers("Content-Type: application/json")
        @POST("wagon-items/create-actlabel/")
        fun post(@Body body: RequestActLabel): Call<ResponseBody>
    }

    interface Act30{
        @Headers("Content-Type: application/json")
        @POST("wagon-items/create-mail-act30/")
        fun post(@Body body: RequestAct30): Call<ResponseBody>
    }

    interface Act51{
        @Headers("Content-Type: application/json")
        @POST("wagon-items/create-mail-act51/")
        fun post(@Body body: RequestAct51): Call<ResponseBody>
    }

    companion object Factory {

        private val retrofit = Retrofit.Builder()
                .baseUrl(TEST_URL)
                .client(UnsafeOkHttpClient.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        fun authorisation(): Authorisation {
            return retrofit.create(Authorisation::class.java)
        }
        fun getStationData() : DataForStation {
            return retrofit.create(DataForStation::class.java)
        }
        fun postVerifyItems() : VerifyItems {
            return retrofit.create(VerifyItems::class.java)
        }
        fun postSendItems() : SendItems {
            return retrofit.create(SendItems::class.java)
        }
        fun postActLabels() : ActLabels {
            return retrofit.create(ActLabels::class.java)
        }
        fun postAct30() : Act30 {
            return retrofit.create(Act30::class.java)
        }
        fun postAct51() : Act51 {
            return retrofit.create(Act51::class.java)
        }
    }
}