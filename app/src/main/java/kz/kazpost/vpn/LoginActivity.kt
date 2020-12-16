package kz.kazpost.vpn

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toast_red.view.*
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.models.Auth
import kz.kazpost.vpn.models.RequestAuthorisation
import kz.kazpost.vpn.ui.acceptinvoice.AcceptInvoiceActivity
import kz.kazpost.vpn.utils.HTTPHelper
import kz.kazpost.vpn.utils.InternetHelper
import kz.kazpost.vpn.utils.PreferenceHelper
import retrofit2.Response
import java.lang.ref.WeakReference

class LoginActivity: AppCompatActivity(), InternetHelper.Consumer {

    private lateinit var dialog: AlertDialog
    private val taskType = "login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName
        InternetHelper(this, "auth")
        version_tv.text = getString(R.string.current_version, version)
        alogin_tlogin.isErrorEnabled = false
        alogin_tpassword.isErrorEnabled = false
        alogin_signIn.setOnClickListener {
            InternetHelper(this, taskType)
        }
    }

    override fun accept(internet: Boolean, type: String) {
        if (internet) {
            if (type == taskType) {
                if (alogin_login.text.toString() != "" && alogin_password.text.toString() != "") {
                    val request = RequestAuthorisation(alogin_login.text.toString(), alogin_password.text.toString())
                    AuthAsync(this).execute(request)
                } else {
                    if (alogin_login.text.toString() == "") {
                        alogin_tlogin.error = "Пустое поле"
                        alogin_tlogin.isErrorEnabled = true
                        alogin_login.requestFocus()
                    }
                    if (alogin_password.text.toString() == "") {
                        alogin_tpassword.error = "Пустое поле"
                        alogin_tpassword.isErrorEnabled = true
                        alogin_password.requestFocus()
                    }
                }
            } else {
                if (PreferenceHelper.getAuth(this) == "auth") {
                    go()
                }
            }
        } else {
            val toast = Toast(this)
            toast.duration = Toast.LENGTH_LONG
            val v = layoutInflater.inflate(R.layout.toast_red, null)
            v.red_text.text = "Нет интернета"
            toast.view = v
            toast.show()
        }
    }

    companion object {
        class AuthAsync internal constructor(context: LoginActivity)
            : AsyncTask<RequestAuthorisation, Void, Response<Auth>>() {
            private val activityReference: WeakReference<LoginActivity> = WeakReference(context)
            override fun doInBackground(vararg params: RequestAuthorisation?): Response<Auth>? {
                return params[0]?.let {
                    HTTPHelper.authorisation().post(it).execute()
                }
            }
            override fun onPreExecute() {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    val builder = AlertDialog.Builder(context)
                    val view = context.layoutInflater.inflate(R.layout.progress, null)
                    builder.setView(view)
                    context.dialog = builder.create()
                    context.dialog.setCancelable(false)
                    context.dialog.setCanceledOnTouchOutside(false)
                    context.dialog.show()
                }
            }
            override fun onPostExecute(result: Response<Auth>?) {
                activityReference.get()?.let { context ->
                    if (context.isFinishing) return
                    context.dialog.dismiss()
                    result?.body()?.let {
                        if (it.sessioID != null) {
                            PreferenceHelper.saveLogin(context, context.alogin_login.text.toString())
                            context.go()
                        } else {
                            val toast = Toast(context)
                            toast.duration = Toast.LENGTH_LONG
                            val v = context.layoutInflater.inflate(R.layout.toast_red, null)
                            v.red_text.text = "Логин или пароль не правильный"
                            toast.view = v
                            toast.show()
                        }
                    }
                }
            }
        }
    }

    private fun go() {
        val intent = when (PreferenceHelper.getStep(this)) {
            Step.ACCEPT_INVOICE -> Intent(this, AcceptInvoiceActivity::class.java)
            Step.ACCEPT_START -> Intent(this, AcceptStartActivity::class.java)
            Step.ACCEPT_SCAN -> Intent(this, AcceptScanActivity::class.java)
            Step.ACCEPT_SCAN2 -> Intent(this, AcceptScan2Activity::class.java)
            Step.ACCEPT_BG -> Intent(this, AcceptBGActivity::class.java)
            Step.ACCEPT_ACT -> Intent(this, AcceptActsActivity::class.java)
            Step.ACCEPT_END -> Intent(this, AcceptEndActivity::class.java)
            Step.TRANSFER_SCAN -> Intent(this, TransferScanActivity::class.java)
            Step.TRANSFER_SCAN2 -> Intent(this, TransferScan2Activity::class.java)
            Step.TRANSFER_BG -> Intent(this, TransferBGActivity::class.java)
            Step.TRANSFER_INVOICE -> Intent(this, TransferInvoiceActivity::class.java)
            Step.TRANSFER_END -> Intent(this, TransferEndActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}