/**
 * GhostGram C6 (PLAN.md 16.3): LOCAL implementation.
 *
 * The donor's CameraPreferencesEntry references
 * uz.unnarsx.cherrygram.camera.roundvideo.VideoMessagesHelper, but that class
 * is absent from the donor's published source tree (the same situation as the
 * classes locally implemented in C2/C3). Only the surface the ported code
 * actually calls is implemented here: isCamera60Fps() reports false because
 * the base has no 60fps round-video flag, so the FPS row shows "30".
 * No network, no remote data.
 */
package uz.unnarsx.cherrygram.camera.roundvideo;

public final class VideoMessagesHelper {

    private VideoMessagesHelper() {
    }

    public static boolean isCamera60Fps() {
        return false;
    }
}
