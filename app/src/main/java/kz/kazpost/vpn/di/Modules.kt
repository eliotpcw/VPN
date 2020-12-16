package kz.kazpost.vpn.di

import android.annotation.SuppressLint
import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Dispatchers
import kz.kazpost.vpn.data.network.api.IApi
import kz.kazpost.vpn.ui.acceptinvoice.AcceptInvoiceRepository
import kz.kazpost.vpn.ui.acceptinvoice.AcceptInvoiceViewModel
import kz.kazpost.vpn.utils.RELEASE_URL
import kz.kazpost.vpn.utils.TEST_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*
import kotlin.coroutines.CoroutineContext

val networkModule = module {

    factory { provideDefaultOkHttpClient() }

    single { retrofit(client = get()) }

    single{ get<Retrofit>().create(IApi::class.java) }
}

val appModule = module {
    single<CoroutineContext>(named(NAME_IO)) { Dispatchers.IO }
    single<CoroutineContext>(named(NAME_MAIN)) { Dispatchers.Main }

    single{ AcceptInvoiceRepository(api = get()) }

    viewModel{ AcceptInvoiceViewModel(repository = get()) }
}

private fun retrofit(client: OkHttpClient) = Retrofit.Builder()
    .client(client)
    .baseUrl(TEST_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .build()

private fun provideDefaultOkHttpClient(): OkHttpClient = try {

    /*
    Create a trust manager that does not validate certificate chains
    * Install the all-trusting trust manager
    * Create an ssl socket factory with our all-trusting manager
    * okHttpClient.connectTimeoutMillis();
    */

    val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {}
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )

    // Install the all-trusting trust manager
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())

    // Create an ssl socket factory with our all-trusting manager
    val sslSocketFactory = sslContext.socketFactory
    val builder = OkHttpClient.Builder()
    builder.sslSocketFactory(
        sslSocketFactory,
        trustAllCerts[0] as X509TrustManager
    )
    builder.hostnameVerifier(HostnameVerifier { _, _ ->  true })


    val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            Log.d("okHttp3: ", message)
        }
    })

    interceptor.level = HttpLoggingInterceptor.Level.BODY

    builder
        .addInterceptor(interceptor)
        .build()

} catch (e: Exception) {
    throw RuntimeException(e)
}

const val NAME_IO = "io"
const val NAME_MAIN = "main"