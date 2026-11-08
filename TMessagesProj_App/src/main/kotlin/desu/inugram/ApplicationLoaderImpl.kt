package desu.inugram

import desu.inugram.helpers.maps.MapLibreMapsProvider
import org.telegram.messenger.ApplicationLoaderImpl as BaseApplicationLoaderImpl
import org.telegram.messenger.GoogleMapsProvider
import org.telegram.messenger.IMapsProvider

class ApplicationLoaderImpl : BaseApplicationLoaderImpl() {
    override fun isStandalone(): Boolean = true

    // GhostGram 2.0 phase 2 (PLAN.md 29.4): inugram's custom updater — the
    // MTProto @InugramCI channel check plus its own download/install UI
    // (UpdateAppAlertDialog / UpdateLayout / ApkInstaller) — is stripped.
    // Every update hook (checkUpdate, getUpdate, isCustomUpdate,
    // showUpdateAppPopup, openApkInstall, takeUpdateLayout,
    // checkApkInstallPermissions) inherits the stock standalone
    // implementation: only Telegram's own server-driven update flow remains.

    override fun onCreateMapsProvider(): IMapsProvider? {
        return when (InuConfig.MAP_PROVIDER.value) {
            InuConfig.MapProviderItem.OSM -> MapLibreMapsProvider()
            else -> GoogleMapsProvider()
        }
    }
}
