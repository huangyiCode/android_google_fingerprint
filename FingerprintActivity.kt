package com.example.fingerprint

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class FingerprintActivity : AppCompatActivity() {

    var cancellationSignal: CancellationSignal? = CancellationSignal()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)
    }


    override fun onContentChanged() {
        findViewById<Button>(R.id.btn_touch_sensor).setOnClickListener {
            if (FingerprintHelper.checkFingerprintAvailable(this)) {
                (getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager)?.let {
                    it.authenticate(
                        FingerprintManager.CryptoObject(
                            FingerprintCryptographyManager.getCipher(this)), cancellationSignal, 0,
                        MikeFingerprintAuthCallback(
                            this
                        ), null
                    )
                }
            }
        }

        cancellationSignal?.setOnCancelListener {

        }
    }

    class MikeFingerprintAuthCallback(val context: Context) : FingerprintManager.AuthenticationCallback() {
        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
            super.onAuthenticationHelp(helpCode, helpString)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
            super.onAuthenticationError(errorCode, errString)
        }

        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            result?.cryptoObject?.cipher?.let {
                if(FingerprintCryptographyManager.getBindFingerprintStatus(context)){
                    val cipherTextFromSharedPreference =
                        FingerprintCryptographyManager.getCipherTextFromSharedPreference(context)
                }else{
                    FingerprintCryptographyManager.saveCipherTextToSharedPreference(context,FingerprintCryptographyManager.encyption("abc:123",it))
                    FingerprintCryptographyManager.setBindFingerPrintStatus(context,true)
                }
            }

        }
    }
}