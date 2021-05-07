package com.example.fingerprint

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat

object FingerprintHelper {

    fun checkFingerprintAvailable(context: Context) =
        when{
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M ->{
                false
            }
            ContextCompat.checkSelfPermission(context,Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_DENIED ->{
                false
            }
            (context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)?.isKeyguardSecure != true ->{
                false
            }
            FingerprintManagerCompat.from(context).run { !(isHardwareDetected&&hasEnrolledFingerprints()) } ->{
                false
            }
            else ->{
                true
            }
        }
}