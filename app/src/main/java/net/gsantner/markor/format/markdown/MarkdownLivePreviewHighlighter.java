/*#######################################################
 *
 *   Maintained 2026 by Codex
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.markor.format.markdown;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;

import net.gsantner.markor.frontend.textview.HighlightingEditor;
import net.gsantner.markor.frontend.textview.SyntaxHighlighterBase;
import net.gsantner.markor.model.AppSettings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownLivePreviewHighlighter extends SyntaxHighlighterBase implements HighlightingEditor.SelectionAwareHighlighter {
    private static final Pattern HEADING = Pattern.compile("(?m)^(#{1,6})(\\s+)(.+)$");
    private static final Pattern BOLD = Pattern.compile("(?<!\\*)\\*\\*(?=\\S)(.+?\\S)\\*\\*(?!\\*)");
    private static final Pattern ITALIC = Pattern.compile("(?<!\\*)\\*(?=\\S)(.+?\\S)\\*(?!\\*)");
    private static final Pattern STRIKETHROUGH = Pattern.compile("~~(?=\\S)(.+?\\S)~~");
    private static final Pattern INLINE_CODE = Pattern.compile("`([^`\\n]+)`");
    private static final Pattern LINK = Pattern.compile("(!)?\\[([^\\]\\n]*)]\\(([^)\\n]+)\\)");
    private static final Pattern TASK = Pattern.compile("(?m)^(\\s*[-*+]\\s)(\\[([ xX])]\\s)(.+)$");

    private static final int SELECTION_DELAY_MS = 90;
    private static final int COLOR_HEADING = 0xffef6d00;
    private static final int COLOR_LINK = 0xff1ea3fe;
    private static final int COLOR_TASK = 0xffdaa521;
    private static final int COLOR_INLINE_CODE = 0x40afafaf;

    private boolean _highlightCodeChangeFont;
    private boolean _highlightCodeBlock;
    private int _selectionStart;
    private int _selectionEnd;
    private int _markerMode;
    private int _quietMarkerColor;

    public MarkdownLivePreviewHighlighter(final AppSettings as) {
        super(as);
    }

    @Override
    public SyntaxHighlighterBase configure(final Paint paint) {
        _highlightCodeChangeFont = _appSettings.isHighlightCodeMonospaceFont();
        _highlightCodeBlock = _appSettings.isHighlightCodeBlock();
        _delay = Math.max(120, _appSettings.getMarkdownHighlightingDelay());
        _markerMode = _appSettings.getMarkdownLivePreviewSyntaxMarkersMode();
        _quietMarkerColor = getMarkerColor();
        return super.configure(paint);
    }

    @Override
    protected void generateSpans() {
        createTabSpans(_tabSize);
        createUnderlineHexColorsSpans();
        createSmallBlueLinkSpans();

        applyHeadings();
        applyTasks();
        applyBold();
        applyItalic();
        applyStrikeThrough();
        applyInlineCode();
        applyLinks();
    }

    @Override
    public boolean updateSelection(final int selStart, final int selEnd) {
        if (_selectionStart == selStart && _selectionEnd == selEnd) {
            return false;
        }
        _selectionStart = Math.max(0, selStart);
        _selectionEnd = Math.max(0, selEnd);
        return true;
    }

    @Override
    public int getSelectionUpdateDelay() {
        return SELECTION_DELAY_MS;
    }

    private void applyHeadings() {
        final Matcher matcher = HEADING.matcher(_spannable);
        while (matcher.find()) {
            final int markerStart = matcher.start(1);
            final int markerEnd = matcher.end(1);
            final int lineEnd = matcher.end(0);
            final int textStart = matcher.start(3);
            final int level = markerEnd - markerStart;
            final boolean active = isLineActive(matcher.start(0), lineEnd);

            addSpanGroup(new HighlightSpan().setBold(true).setForeColor(COLOR_HEADING), textStart, lineEnd);
            addSpanGroup(new RelativeSizeSpan(getHeadingScale(level)), textStart, lineEnd);
            applyQuietMarker(markerStart, markerEnd, active);
        }
    }

    private void applyTasks() {
        final Matcher matcher = TASK.matcher(_spannable);
        while (matcher.find()) {
            final boolean checked = !TextUtils.isEmpty(matcher.group(3)) && !" ".equals(matcher.group(3));
            final int checkboxStart = matcher.start(2);
            final int checkboxEnd = matcher.end(2);
            final int textStart = matcher.start(4);
            final int lineEnd = matcher.end(0);

            addSpanGroup(new HighlightSpan().setForeColor(COLOR_TASK).setBold(true), matcher.start(1), checkboxEnd);
            addSpanGroup(new HighlightSpan().setBackColor(0x15daa521), checkboxStart, checkboxEnd);
            if (checked) {
                addSpanGroup(new HighlightSpan().setStrike(true).setForeColor(adjustAlpha(_textColor, 0.8f)), textStart, lineEnd);
            }
        }
    }

    private void applyBold() {
        final Matcher matcher = BOLD.matcher(_spannable);
        while (matcher.find()) {
            final int start = matcher.start(0);
            final int end = matcher.end(0);
            final int contentStart = matcher.start(1);
            final int contentEnd = matcher.end(1);
            final boolean active = isActive(start, end);

            addSpanGroup(new HighlightSpan().setBold(true), contentStart, contentEnd);
            applyQuietMarker(start, contentStart, active);
            applyQuietMarker(contentEnd, end, active);
        }
    }

    private void applyItalic() {
        final Matcher matcher = ITALIC.matcher(_spannable);
        while (matcher.find()) {
            final int start = matcher.start(0);
            final int end = matcher.end(0);
            final int contentStart = matcher.start(1);
            final int contentEnd = matcher.end(1);
            if (contentStart <= start || contentEnd >= end) {
                continue;
            }

            final boolean active = isActive(start, end);
            addSpanGroup(new HighlightSpan().setItalic(true), contentStart, contentEnd);
            applyQuietMarker(start, contentStart, active);
            applyQuietMarker(contentEnd, end, active);
        }
    }

    private void applyStrikeThrough() {
        final Matcher matcher = STRIKETHROUGH.matcher(_spannable);
        while (matcher.find()) {
            final int start = matcher.start(0);
            final int end = matcher.end(0);
            final int contentStart = matcher.start(1);
            final int contentEnd = matcher.end(1);
            final boolean active = isActive(start, end);

            addSpanGroup(new HighlightSpan().setStrike(true), contentStart, contentEnd);
            applyQuietMarker(start, contentStart, active);
            applyQuietMarker(contentEnd, end, active);
        }
    }

    private void applyInlineCode() {
        final Matcher matcher = INLINE_CODE.matcher(_spannable);
        while (matcher.find()) {
            final int start = matcher.start(0);
            final int end = matcher.end(0);
            final int contentStart = matcher.start(1);
            final int contentEnd = matcher.end(1);
            final boolean active = isActive(start, end);

            addSpanGroup(new HighlightSpan().setBackColor(COLOR_INLINE_CODE), contentStart, contentEnd);
            if (_highlightCodeChangeFont) {
                addSpanGroup(new TypefaceSpan("monospace"), contentStart, contentEnd);
            }
            if (_highlightCodeBlock) {
                addSpanGroup(new HighlightSpan().setForeColor(adjustAlpha(_textColor, 0.95f)), contentStart, contentEnd);
            }
            applyQuietMarker(start, contentStart, active);
            applyQuietMarker(contentEnd, end, active);
        }
    }

    private void applyLinks() {
        final Matcher matcher = LINK.matcher(_spannable);
        while (matcher.find()) {
            final int wholeStart = matcher.start(0);
            final int wholeEnd = matcher.end(0);
            final int titleStart = matcher.start(2);
            final int titleEnd = matcher.end(2);
            final int urlStart = matcher.start(3);
            final int urlEnd = matcher.end(3);
            final boolean active = isActive(wholeStart, wholeEnd);

            addSpanGroup(new HighlightSpan().setForeColor(COLOR_LINK).setUnderline(true).setBold(true), titleStart, titleEnd);
            if (matcher.group(1) != null) {
                applyQuietMarker(wholeStart, wholeStart + 1, active);
            }
            applyQuietMarker(wholeStart + (matcher.group(1) != null ? 1 : 0), titleStart, active);
            applyQuietMarker(titleEnd, urlStart, active);
            addSpanGroup(new HighlightSpan().setForeColor(adjustAlpha(COLOR_LINK, active ? 0.9f : 0.45f)), urlStart, urlEnd);
            applyQuietMarker(urlEnd, wholeEnd, active);
        }
    }

    private void applyQuietMarker(final int start, final int end, final boolean active) {
        if (active || _markerMode == AppSettings.MARKDOWN_LIVE_PREVIEW_MARKERS_SHOW || end <= start) {
            return;
        }
        addSpanGroup(new HighlightSpan().setForeColor(_quietMarkerColor), start, end);
    }

    private boolean isActive(final int start, final int end) {
        return (_selectionStart == _selectionEnd && _selectionStart >= start && _selectionStart <= end)
                || (_selectionEnd > start && _selectionStart < end);
    }

    private boolean isLineActive(final int start, final int end) {
        return isActive(start, end) || (_selectionStart >= start && _selectionStart <= end);
    }

    private float getHeadingScale(final int level) {
        switch (level) {
            case 1:
                return 1.45f;
            case 2:
                return 1.32f;
            case 3:
                return 1.2f;
            case 4:
                return 1.1f;
            default:
                return 1.0f;
        }
    }

    private int getMarkerColor() {
        switch (_markerMode) {
            case AppSettings.MARKDOWN_LIVE_PREVIEW_MARKERS_HIDE:
                return Color.TRANSPARENT;
            case AppSettings.MARKDOWN_LIVE_PREVIEW_MARKERS_SHOW:
                return _textColor;
            case AppSettings.MARKDOWN_LIVE_PREVIEW_MARKERS_DIM:
            default:
                return adjustAlpha(_textColor, _isDarkMode ? 0.35f : 0.45f);
        }
    }

    private int adjustAlpha(final int color, final float factor) {
        return Color.argb(
                Math.min(255, Math.max(0, Math.round(Color.alpha(color) * factor))),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }
}
