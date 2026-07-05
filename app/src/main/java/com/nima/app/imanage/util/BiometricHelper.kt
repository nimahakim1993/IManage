package com.nima.app.imanage.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    const val AUTH_TITLE = "Authentication required"
    const val AUTH_SUBTITLE = "Verify your fingerprint to view this password"

    enum class AuthType { BIOMETRIC, DEVICE_CREDENTIAL, NONE }

    /** Best available authentication method on this device. */
    fun availableAuthType(context: Context): AuthType {
        val manager = BiometricManager.from(context)
        return when {
            manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS -> AuthType.BIOMETRIC

            manager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
                BiometricManager.BIOMETRIC_SUCCESS -> AuthType.DEVICE_CREDENTIAL

            else -> AuthType.NONE
        }
    }

    /** True when biometric (fingerprint/face) authentication is available. */
    fun canAuthenticate(context: Context): Boolean =
        availableAuthType(context) == AuthType.BIOMETRIC

    /**
     * Prompts the user for authentication tied to [activity] using the strongest
     * available [AuthType]. When [authType] is NONE the success callback is invoked
     * synchronously (no security on the device).
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = AUTH_TITLE,
        subtitle: String = AUTH_SUBTITLE,
        authType: AuthType = availableAuthType(activity),
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (authType == AuthType.NONE) {
            onSuccess()
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setConfirmationRequired(false)
        when (authType) {
            AuthType.BIOMETRIC -> {
                builder
                    .setNegativeButtonText("Cancel")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            }
            AuthType.DEVICE_CREDENTIAL -> {
                // DEVICE_CREDENTIAL cannot use a negative button; system handles dismiss UI
                builder.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            }
            AuthType.NONE -> Unit
        }
        prompt.authenticate(builder.build())
    }
}