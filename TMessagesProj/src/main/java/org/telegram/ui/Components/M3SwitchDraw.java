package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

/**
 * GhostGram (PLAN.md 27): Material 3 switch rendering ported from inugram's
 * M3SwitchHelper, applied inside the shared Switch component so every
 * switch in the app draws the same way.
 *
 * Geometry mirrors the MDC MaterialSwitch spec in track-viewport units
 * (track 52x32, radius 16, thumb radius 8 off / 12 on, thumb center 16 off /
 * 36 on), rendered at a target track width of 36dp (growing toward the full
 * 52dp spec in wider host frames), centered in the host frame. Off state:
 * 2dp outline in the track color, thumb filled with the track color. On
 * state: track filled with the checked color, thumb filled with the thumb
 * checked color. Track/thumb colors snap with the checked state; only the
 * thumb position and radius animate with progress. Touch, accessibility
 * and state handling stay fully stock.
 */
public final class M3SwitchDraw {

    private static final float TRACK_W = 52f;
    private static final float TRACK_H = 32f;
    private static final float TARGET_TRACK_W = 36f;
    private static final float FULL_TRACK_W = TRACK_W;
    private static final float RADIUS = 16f;
    private static final float THUMB_R_OFF = 8f;
    private static final float THUMB_R_ON = 12f;
    private static final float THUMB_CX_OFF = 16f;
    private static final float THUMB_CX_ON = 36f;
    private static final float ICON_SCALE = 0.7f;
    private static final float ICON_NUDGE_UP = 0.5f;
    private static final float CHECK_SCALE = 0.8f;
    private static final float CHECK_NUDGE_RIGHT = 0.5f;
    private static final float ICON_STROKE = 2f;
    private static final float CROSS_STROKE = 1.5f;

    private static final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF rectF = new RectF();
    private static final PorterDuffColorFilter whiteFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

    static {
        strokePaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeWidth(AndroidUtilities.dpf2(ICON_STROKE));
    }

    private M3SwitchDraw() {
    }

    /**
     * Draws the M3 switch; returns the thumb center {cx, cy} in view
     * coordinates so the caller can position its ripple drawable.
     */
    public static float[] draw(Canvas canvas, int measuredWidth, int measuredHeight, float progress, boolean isChecked,
                               int drawIconType, float iconProgress, Drawable iconDrawable,
                               int trackColorKey, int trackCheckedColorKey, int thumbCheckedColorKey,
                               Theme.ResourcesProvider resourcesProvider) {
        final int offColor = Theme.getColor(trackColorKey, resourcesProvider);
        final int onColor = Theme.getColor(trackCheckedColorKey, resourcesProvider);
        final int thumbOnColor = Theme.getColor(thumbCheckedColorKey, resourcesProvider);

        final float frameWidthDp = measuredWidth / AndroidUtilities.density;
        final float targetW = Math.max(TARGET_TRACK_W, Math.min(FULL_TRACK_W, frameWidthDp - 1f));
        final float scale = AndroidUtilities.dpf2(targetW) / TRACK_W;
        final float trackW = TRACK_W * scale;
        final float h = TRACK_H * scale;
        final float left = (measuredWidth - trackW) / 2f;
        final float top = (measuredHeight - h) / 2f;

        rectF.set(left, top, left + trackW, top + h);
        final float radius = RADIUS * scale;
        if (isChecked) {
            fillPaint.setColor(onColor);
            canvas.drawRoundRect(rectF, radius, radius, fillPaint);
        } else {
            final float sw = AndroidUtilities.dpf2(2f);
            strokePaint.setColor(offColor);
            strokePaint.setStrokeWidth(sw);
            rectF.inset(sw / 2f, sw / 2f);
            canvas.drawRoundRect(rectF, radius - sw / 2f, radius - sw / 2f, strokePaint);
        }

        final float cx = left + scale * (THUMB_CX_OFF + (THUMB_CX_ON - THUMB_CX_OFF) * progress);
        final float cy = top + h / 2f;
        final float rest = scale * (THUMB_R_OFF + (THUMB_R_ON - THUMB_R_OFF) * progress);
        thumbPaint.setColor(isChecked ? thumbOnColor : offColor);
        canvas.drawCircle(cx, cy, rest, thumbPaint);

        final int iconColor = isChecked ? onColor : Color.WHITE;
        if (iconDrawable != null) {
            // Thumb icons (e.g. the locked-switch padlock) are forced white;
            // the stock track-color tint is grey, invisible on the grey thumb.
            if (iconDrawable.getColorFilter() != whiteFilter) {
                iconDrawable.setColorFilter(whiteFilter);
            }
            final int ix = Math.round(cx);
            final int iy = Math.round(cy - AndroidUtilities.dpf2(ICON_NUDGE_UP));
            final int hw = Math.round(iconDrawable.getIntrinsicWidth() * ICON_SCALE / 2f);
            final int hh = Math.round(iconDrawable.getIntrinsicHeight() * ICON_SCALE / 2f);
            iconDrawable.setBounds(ix - hw, iy - hh, ix + hw, iy + hh);
            iconDrawable.draw(canvas);
        } else if (drawIconType == 1) {
            drawCheckmark(canvas, Math.round(cx), Math.round(cy), isChecked, progress, iconColor);
        } else if (drawIconType == 2) {
            drawDot(canvas, Math.round(cx), Math.round(cy), iconProgress, iconColor);
        }
        return new float[]{cx, cy};
    }

    // Ported from inugram's helper (itself a port of stock Switch.onDraw
    // drawIconType 1): an X (off) / checkmark (on), scaled for the M3 thumb.
    private static void drawCheckmark(Canvas canvas, int cx0, int cy0, boolean checked, float sizeProgress, int color) {
        final float shape = checked ? 1f : 0f;
        iconPaint.setColor(color);
        iconPaint.setAlpha(255);
        iconPaint.setStrokeWidth(AndroidUtilities.dpf2(CROSS_STROKE + (ICON_STROKE - CROSS_STROKE) * shape));
        final float s = CHECK_SCALE * (THUMB_R_OFF + (THUMB_R_ON - THUMB_R_OFF) * sizeProgress) / THUMB_R_ON;
        final float nudge = AndroidUtilities.dpf2(CHECK_NUDGE_RIGHT) * shape;
        final int tx = cx0 - (int) (AndroidUtilities.dp(10.8f) - AndroidUtilities.dp(1.3f) * shape);
        final int ty = cy0 - (int) (AndroidUtilities.dp(8.5f) - AndroidUtilities.dp(0.5f) * shape);

        int startX2 = (int) AndroidUtilities.dpf2(4.6f) + tx;
        int startY2 = (int) (AndroidUtilities.dpf2(9.5f) + ty);
        int endX2 = startX2 + AndroidUtilities.dp(2);
        int endY2 = startY2 + AndroidUtilities.dp(2);

        int startX = (int) AndroidUtilities.dpf2(7.5f) + tx;
        int startY = (int) AndroidUtilities.dpf2(5.4f) + ty;
        int endX = startX + AndroidUtilities.dp(7);
        int endY = startY + AndroidUtilities.dp(7);

        startX = (int) (startX + (startX2 - startX) * shape);
        startY = (int) (startY + (startY2 - startY) * shape);
        endX = (int) (endX + (endX2 - endX) * shape);
        endY = (int) (endY + (endY2 - endY) * shape);
        canvas.drawLine(fx(startX, cx0, s, nudge), fy(startY, cy0, s), fx(endX, cx0, s, nudge), fy(endY, cy0, s), iconPaint);

        startX = (int) AndroidUtilities.dpf2(7.5f) + tx;
        startY = (int) AndroidUtilities.dpf2(12.5f) + ty;
        endX = startX + AndroidUtilities.dp(7);
        endY = startY - AndroidUtilities.dp(7);
        canvas.drawLine(fx(startX, cx0, s, nudge), fy(startY, cy0, s), fx(endX, cx0, s, nudge), fy(endY, cy0, s), iconPaint);
    }

    private static float fx(int x, int cx0, float s, float nudge) {
        return cx0 + (x - cx0) * s + nudge;
    }

    private static float fy(int y, int cy0, float s) {
        return cy0 + (y - cy0) * s;
    }

    // Ported from inugram's helper (stock drawIconType 2): a dot folding into an L.
    private static void drawDot(Canvas canvas, int cx, int cy, float iconProgress, int color) {
        iconPaint.setColor(color);
        iconPaint.setAlpha((int) (255 * (1f - iconProgress)));
        iconPaint.setStrokeWidth(AndroidUtilities.dpf2(ICON_STROKE));
        canvas.drawLine(cx, cy, cx, cy - AndroidUtilities.dp(5), iconPaint);
        canvas.save();
        canvas.rotate(-90 * iconProgress, cx, cy);
        canvas.drawLine(cx, cy, cx + AndroidUtilities.dp(4), cy, iconPaint);
        canvas.restore();
    }
}
