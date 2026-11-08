package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

/**
 * GhostGram (PLAN.md 27): Material 3 slider rendering ported from inugram's
 * M3SliderHelper design parameters, applied at the shared-component layer so
 * every SeekBarView / SlideChooseView / SeekBar in the app draws the same way.
 *
 * inugram geometry (compact M3 proportions, from M3SliderHelper.kt):
 * track height 12dp, outer corner radius 6dp, inner (handle-facing) radius
 * 1.5dp, handle gap 4dp, handle 3dp wide x 24dp high (1.5dp wide pressed),
 * tick / stop-indicator radius 1.5dp, sub-minimum region at 50% alpha.
 * Touch handling, accessibility and progress state stay fully stock; only
 * the drawing changes.
 */
public final class M3SliderDraw {

    public static final float TRACK_HEIGHT = 12f;
    public static final float OUTER_RADIUS = 6f;
    public static final float INNER_RADIUS = 1.5f;
    public static final float GAP = 4f;
    public static final float HANDLE_WIDTH = 3f;
    public static final float HANDLE_WIDTH_PRESSED = 1.5f;
    public static final float HANDLE_HEIGHT = 24f;
    public static final float TICK_RADIUS = 1.5f;
    public static final float DIM_ALPHA = 0.5f;

    private static final float MIN_TICK_SPACING = 16f;
    private static final int MAX_TICK_COUNT = 7;

    private static final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF rect = new RectF();
    private static final Path path = new Path();
    private static final float[] radii = new float[8];

    private M3SliderDraw() {
    }

    /**
     * Number of tick dots for a stepped slider (inugram tickCount): every stop
     * when they fit under MAX_TICK_COUNT / MIN_TICK_SPACING, otherwise thinned
     * to the largest count whose gaps land evenly on real stops. 0 = render as
     * a continuous track with only its stop indicator.
     */
    public static int tickCount(int intervals, float spanPx) {
        if (intervals < 1) {
            return 0;
        }
        float dotSpan = spanPx - 2 * AndroidUtilities.dpf2(OUTER_RADIUS);
        int maxBySpacing = (int) (dotSpan / AndroidUtilities.dpf2(MIN_TICK_SPACING)) + 1;
        int maxDots = Math.min(Math.min(MAX_TICK_COUNT, maxBySpacing), intervals + 1);
        if (maxDots < 3) {
            return 0;
        }
        for (int dots = maxDots; dots >= 3; dots--) {
            if (intervals % (dots - 1) == 0) {
                return dots;
            }
        }
        return 0;
    }

    /** One track span with per-end corner radii; parts before dimUntilX are drawn at half alpha. */
    public static void drawSpan(Canvas canvas, float l, float r, float cy, float radL, float radR, int color, float dimUntilX, float trackHeightDp) {
        drawSpan(canvas, l, r, cy, radL, radR, color, dimUntilX, Float.MAX_VALUE, trackHeightDp);
    }

    /**
     * Full inugram drawSpan: the parts before dimUntilX / after dimFromX are
     * drawn at half alpha (split edges stay square so the track reads
     * continuous).
     */
    public static void drawSpan(Canvas canvas, float l, float r, float cy, float radL, float radR, int color, float dimUntilX, float dimFromX, float trackHeightDp) {
        if (r - l < 1f) {
            return;
        }
        float p1 = Math.max(l, Math.min(r, dimUntilX));
        float p2 = Math.max(p1, Math.min(r, dimFromX));
        float a = l;
        for (float b : new float[]{p1, p2, r}) {
            if (b - a >= 0.5f) {
                boolean dimmed = b <= p1 + 0.01f || a >= p2 - 0.01f;
                trackPaint.setColor(dimmed ? Theme.multAlpha(color, DIM_ALPHA) : color);
                drawRounded(canvas, a, b, cy, a <= l + 0.5f ? radL : 0f, b >= r - 0.5f ? radR : 0f, trackPaint, trackHeightDp);
            }
            a = b;
        }
    }

    public static void drawRounded(Canvas canvas, float l, float r, float cy, float radL, float radR, Paint paint, float trackHeightDp) {
        float half = AndroidUtilities.dpf2(trackHeightDp) / 2f;
        rect.set(l, cy - half, r, cy + half);
        if (radL == radR) {
            canvas.drawRoundRect(rect, radL, radL, paint);
            return;
        }
        radii[0] = radL; radii[1] = radL;
        radii[2] = radR; radii[3] = radR;
        radii[4] = radR; radii[5] = radR;
        radii[6] = radL; radii[7] = radL;
        path.rewind();
        path.addRoundRect(rect, radii, Path.Direction.CW);
        canvas.drawPath(path, paint);
    }

    public static void drawHandle(Canvas canvas, float cx, float cy, float widthPx, float maxHeightPx, int color) {
        float h = Math.min(AndroidUtilities.dpf2(HANDLE_HEIGHT), maxHeightPx);
        float hw = widthPx / 2f;
        rect.set(cx - hw, cy - h / 2f, cx + hw, cy + h / 2f);
        handlePaint.setColor(color);
        canvas.drawRoundRect(rect, hw, hw, handlePaint);
    }

    public static void drawTicks(Canvas canvas, float left, float right, float cy, float cx, float hw, int dots, int intervals, int onActiveColor, int onInactiveColor, float dimUntilX) {
        float tickR = AndroidUtilities.dpf2(TICK_RADIUS);
        float gap = AndroidUtilities.dpf2(GAP);
        float endInset = AndroidUtilities.dpf2(OUTER_RADIUS);
        float l = left + endInset;
        float r = right - endInset;
        for (int j = 0; j < dots; j++) {
            int tickStep = j * intervals / (dots - 1);
            float x = l + (r - l) * tickStep / intervals;
            if (Math.abs(x - cx) < hw + gap + tickR) {
                continue;
            }
            int color = x < cx ? onActiveColor : onInactiveColor;
            if (x < dimUntilX) {
                color = Theme.multAlpha(color, DIM_ALPHA);
            }
            tickPaint.setColor(color);
            canvas.drawCircle(x, cy, tickR, tickPaint);
        }
    }

    public static void drawStopIndicator(Canvas canvas, float right, float cy, float inactiveLeft, int color) {
        float tickR = AndroidUtilities.dpf2(TICK_RADIUS);
        float stopX = right - AndroidUtilities.dpf2(OUTER_RADIUS);
        if (stopX > inactiveLeft + tickR) {
            tickPaint.setColor(color);
            canvas.drawCircle(stopX, cy, tickR, tickPaint);
        }
    }
}
