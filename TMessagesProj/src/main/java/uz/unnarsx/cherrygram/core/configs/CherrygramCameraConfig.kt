/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 *
 * GhostGram 骨架说明（地基批第二轮，详见 PLAN.md 第十节）：
 * 键名/默认值与供体逐项一致（脚本核对），仅一处默认值替代：
 * cameraType 的供体默认值是「相机能力工具类（未到批次）的运行时判定式」，
 * 骨架改本地中性常量 TELEGRAM_CAMERA，C6 相机批随该工具类落地后接回判定。
 * 视频消息分辨率在供体侧曾可被远程配置改写（PLAN.md 8.1 C1），
 * 本骨架只保留本地常量默认 512，无任何远程引用。
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.float
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.camera.CameraXUtils
import java.util.Calendar

object CherrygramCameraConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Camera type start */
    const val TELEGRAM_CAMERA = 0
    const val CAMERA_X = 1
    const val CAMERA_2 = 2
    const val SYSTEM_CAMERA = 3
    // GhostGram C6 (PLAN.md 16.3): donor default restored now that CameraXUtils landed
    var cameraType by sharedPreferences.int("CP_CameraType1", if (CameraXUtils.isCameraXSupported()) CAMERA_X else TELEGRAM_CAMERA)
    /** Camera type finish */

    /** Camera start */
    var disableAttachCamera by sharedPreferences.boolean("CP_DisableAttachCam", true)
    var useDualCamera by sharedPreferences.boolean("CP_UseDualCamera", false)

    const val Camera16to9 = 0
    const val Camera4to3 = 1
    const val Camera1to1 = 2
    const val CameraAspectDefault = 3
    var cameraAspectRatio by sharedPreferences.int("CP_CameraAspectRatio", CameraAspectDefault)
    /** Camera finish */

    /** Videomessages start */
    var useNewEncoder by sharedPreferences.boolean("CP_CameraNewEncoder", false)
    var cameraResolution by sharedPreferences.int("CP_CameraResolution", -1)
    var startFromUltraWideCam by sharedPreferences.boolean("CP_StartFromUltraWideCam", true)

    /** CameraX FPS start */
    const val CameraXFpsRange30 = 0
    const val CameraXFpsRange60 = 1
    var cameraXFpsRange by sharedPreferences.int("CP_CameraFpsRange", CameraXFpsRange30)
    /** CameraX FPS finish */

    var videoStabilisation by sharedPreferences.boolean("CP_VideoStabilisation", false)
    var opticalStabilisation by sharedPreferences.boolean("CP_OpticalStabilisation", false)
    var continuousAutofocus by sharedPreferences.boolean("CP_ContinuousAutofocus", false)
    var noiceReduction by sharedPreferences.boolean("CP_NoiceReduction", false)
    var faceDetection by sharedPreferences.boolean("CP_FaceDetection", false)
    var bokehEffect by sharedPreferences.boolean("CP_BokehEffect", false)

    var centerCameraControlButtons by sharedPreferences.boolean("CP_CenterCameraControlButtons", true)

    const val EXPOSURE_SLIDER_NONE = 0
    const val EXPOSURE_SLIDER_BOTTOM = 1
    const val EXPOSURE_SLIDER_RIGHT = 2
    const val EXPOSURE_SLIDER_LEFT = 3
    var exposureSlider by sharedPreferences.int("CP_ExposureSlider", EXPOSURE_SLIDER_RIGHT)

    var rearCam by sharedPreferences.boolean("CP_RearCam", false)

    var videoMessagesResolution by sharedPreferences.int("CG_Round_Video_Resolution", 512)
    var videoMessagesFlashWarmthIntensity by sharedPreferences.float("CG_Round_Flash_Warmth_Intensity", 0.50f)
    var videoMessagesFlashIntensity by sharedPreferences.float("CG_Round_Flash_Intensity", 1f)
    var videoMessagesRearFlashIntensity by sharedPreferences.float("CG_Round_Rear_Flash_Intensity", 1f)
    var videoMessagesHintCount by sharedPreferences.int("CG_Round_Flash_Hint_Count", 0)
    var zoomHintCount by sharedPreferences.int("CG_Round_Zoom_Hint_Count", 0)

    fun checkVideoMessagesHint() {
        try {
            val calendar = Calendar.getInstance()
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            if (dayOfMonth == 1) {
                videoMessagesHintCount = 0
                zoomHintCount = 0
            }
        } catch (_: Exception) {}
    }
    /** Videomessages finish */

}
