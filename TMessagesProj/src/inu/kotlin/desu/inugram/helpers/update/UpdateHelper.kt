package desu.inugram.helpers.update

import android.os.Build
import desu.inugram.helpers.security.ParanoiaHelper
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.BuildVars
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R

/**
 * GhostGram: the inugram self-update mechanism (periodic MTProto check of the
 * developer channel, APK download/install flow, variant switching) has been
 * removed entirely. Only the version-info formatting used by the About page
 * and session cells remains here.
 */
object UpdateHelper {
    val packageInfo by lazy {
        ApplicationLoader.applicationContext.packageManager.getPackageInfo(
            ApplicationLoader.applicationContext.packageName,
            0
        )
    }

    @JvmStatic
    val stockVersionName by lazy {
        packageInfo.versionName?.replace(Regex("-[0-9a-f]{7}$"), "") ?: ""
    }

    fun getVersionInfoString(): String {
        return LocaleController.formatString(
            R.string.InuVersion,
            packageInfo.versionCode,
            stockVersionName,
            BuildConfig.STOCK_VERSION_CODE,
            LocaleController.getString(
                if (BuildConfig.INU_PLUGINLESS) R.string.InuBuildPluginless else R.string.InuBuildFull
            )
        )
    }

    @JvmStatic
    fun getFullVersionInfo(): String {
        if (ParanoiaHelper.isDisguised()) {
            return "Telegram for Android v${stockVersionName} (${BuildConfig.STOCK_VERSION_CODE})\ndirect ${Build.CPU_ABI} ${Build.CPU_ABI2}"
        }
        return "${getVersionInfoString()}\nBuilt on: ${BuildVars.BUILD_DATE}"
    }
}
