package kz.kazpost.vpn.utils

import android.os.AsyncTask
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

internal class InternetHelper(private val mConsumer: Consumer, private val type: String) : AsyncTask<Void, Void, Boolean>() {
    interface Consumer {
        fun accept(internet: Boolean, type: String)
    }
    init {
        execute()
    }
    override fun doInBackground(vararg voids: Void): Boolean {
        return try {
            val sock = Socket()
            sock.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            sock.close()
            true
        } catch (e: IOException) {
            false
        }
    }
    override fun onPostExecute(internet: Boolean) {
        mConsumer.accept(internet, type)
    }
}