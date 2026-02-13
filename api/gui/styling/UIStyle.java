package dev.anarchy.waifuhax.api.gui.styling;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UIStyle {
    
    // Colors
    private int backgroundColor = 0x00000000;
    private int foregroundColor = 0xFFFFFFFF;
    private int borderColor = 0xFF888888;
    private int hoverColor = 0xFF00FF00;
    private int activeColor = 0xFF0088FF;
    private int disabledColor = 0xFF444444;
    
    // Border
    private float borderWidth = 0;
    private float borderRadius = 0;
    
    // Shadow
    private boolean hasShadow = false;
    private int shadowColor = 0x80000000;
    private float shadowOffsetX = 0;
    private float shadowOffsetY = 2;
    private float shadowBlur = 4;
    
    // Typography
    private float fontSize = 1.0f;
    private boolean boldText = false;
    private boolean italicText = false;
    private boolean shadowedText = true;
    
    // Animation
    private long hoverTransitionDuration = 150;
    private long clickTransitionDuration = 100;
    
    // Opacity
    private float opacity = 1.0f;

    // Getters
    public int getBackgroundColor() { return backgroundColor; }
    public int getForegroundColor() { return foregroundColor; }
    public int getBorderColor() { return borderColor; }
    public int getHoverColor() { return hoverColor; }
    public int getActiveColor() { return activeColor; }
    public int getDisabledColor() { return disabledColor; }
    public float getBorderWidth() { return borderWidth; }
    public float getBorderRadius() { return borderRadius; }
    public boolean isHasShadow() { return hasShadow; }
    public int getShadowColor() { return shadowColor; }
    public float getShadowOffsetX() { return shadowOffsetX; }
    public float getShadowOffsetY() { return shadowOffsetY; }
    public float getShadowBlur() { return shadowBlur; }
    public float getFontSize() { return fontSize; }
    public boolean isBoldText() { return boldText; }
    public boolean isItalicText() { return italicText; }
    public boolean isShadowedText() { return shadowedText; }
    public long getHoverTransitionDuration() { return hoverTransitionDuration; }
    public long getClickTransitionDuration() { return clickTransitionDuration; }
    public float getOpacity() { return opacity; }

    // Builder pattern implementation
    public static UIStyleBuilder builder() {
        return new UIStyleBuilder();
    }

    public UIStyleBuilder toBuilder() {
        return new UIStyleBuilder()
            .backgroundColor(this.backgroundColor)
            .foregroundColor(this.foregroundColor)
            .borderColor(this.borderColor)
            .hoverColor(this.hoverColor)
            .activeColor(this.activeColor)
            .disabledColor(this.disabledColor)
            .borderWidth(this.borderWidth)
            .borderRadius(this.borderRadius)
            .hasShadow(this.hasShadow)
            .shadowColor(this.shadowColor)
            .shadowOffsetX(this.shadowOffsetX)
            .shadowOffsetY(this.shadowOffsetY)
            .shadowBlur(this.shadowBlur)
            .fontSize(this.fontSize)
            .boldText(this.boldText)
            .italicText(this.italicText)
            .shadowedText(this.shadowedText)
            .hoverTransitionDuration(this.hoverTransitionDuration)
            .clickTransitionDuration(this.clickTransitionDuration)
            .opacity(this.opacity);
    }

    public static class UIStyleBuilder {
        private int backgroundColor = 0x00000000;
        private int foregroundColor = 0xFFFFFFFF;
        private int borderColor = 0xFF888888;
        private int hoverColor = 0xFF00FF00;
        private int activeColor = 0xFF0088FF;
        private int disabledColor = 0xFF444444;
        private float borderWidth = 0;
        private float borderRadius = 0;
        private boolean hasShadow = false;
        private int shadowColor = 0x80000000;
        private float shadowOffsetX = 0;
        private float shadowOffsetY = 2;
        private float shadowBlur = 4;
        private float fontSize = 1.0f;
        private boolean boldText = false;
        private boolean italicText = false;
        private boolean shadowedText = true;
        private long hoverTransitionDuration = 150;
        private long clickTransitionDuration = 100;
        private float opacity = 1.0f;

        public UIStyleBuilder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public UIStyleBuilder foregroundColor(int foregroundColor) {
            this.foregroundColor = foregroundColor;
            return this;
        }

        public UIStyleBuilder borderColor(int borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public UIStyleBuilder hoverColor(int hoverColor) {
            this.hoverColor = hoverColor;
            return this;
        }

        public UIStyleBuilder activeColor(int activeColor) {
            this.activeColor = activeColor;
            return this;
        }

        public UIStyleBuilder disabledColor(int disabledColor) {
            this.disabledColor = disabledColor;
            return this;
        }

        public UIStyleBuilder borderWidth(float borderWidth) {
            this.borderWidth = borderWidth;
            return this;
        }

        public UIStyleBuilder borderRadius(float borderRadius) {
            this.borderRadius = borderRadius;
            return this;
        }

        public UIStyleBuilder hasShadow(boolean hasShadow) {
            this.hasShadow = hasShadow;
            return this;
        }

        public UIStyleBuilder shadowColor(int shadowColor) {
            this.shadowColor = shadowColor;
            return this;
        }

        public UIStyleBuilder shadowOffsetX(float shadowOffsetX) {
            this.shadowOffsetX = shadowOffsetX;
            return this;
        }

        public UIStyleBuilder shadowOffsetY(float shadowOffsetY) {
            this.shadowOffsetY = shadowOffsetY;
            return this;
        }

        public UIStyleBuilder shadowBlur(float shadowBlur) {
            this.shadowBlur = shadowBlur;
            return this;
        }

        public UIStyleBuilder fontSize(float fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public UIStyleBuilder boldText(boolean boldText) {
            this.boldText = boldText;
            return this;
        }

        public UIStyleBuilder italicText(boolean italicText) {
            this.italicText = italicText;
            return this;
        }

        public UIStyleBuilder shadowedText(boolean shadowedText) {
            this.shadowedText = shadowedText;
            return this;
        }

        public UIStyleBuilder hoverTransitionDuration(long hoverTransitionDuration) {
            this.hoverTransitionDuration = hoverTransitionDuration;
            return this;
        }

        public UIStyleBuilder clickTransitionDuration(long clickTransitionDuration) {
            this.clickTransitionDuration = clickTransitionDuration;
            return this;
        }

        public UIStyleBuilder opacity(float opacity) {
            this.opacity = opacity;
            return this;
        }

        public UIStyle build() {
            return new UIStyle(
                backgroundColor, foregroundColor, borderColor, hoverColor, activeColor, disabledColor,
                borderWidth, borderRadius, hasShadow, shadowColor, shadowOffsetX, shadowOffsetY, shadowBlur,
                fontSize, boldText, italicText, shadowedText, hoverTransitionDuration, clickTransitionDuration, opacity
            );
        }
    }

    public static UIStyle getDefault() {
        return UIStyle.builder().build();
    }
    
    public static UIStyle getModernDark() {
        return UIStyle.builder()
            .backgroundColor(0xDD1A1A1A)
            .foregroundColor(0xFFEEEEEE)
            .borderColor(0xFF333333)
            .hoverColor(0xFF00DDFF)
            .activeColor(0xFF00AAFF)
            .borderWidth(1)
            .borderRadius(6)
            .hasShadow(true)
            .shadowColor(0x40000000)
            .shadowOffsetY(3)
            .shadowBlur(6)
            .build();
    }
    
    public static UIStyle getGlass() {
        return UIStyle.builder()
            .backgroundColor(0x40FFFFFF)
            .foregroundColor(0xFFFFFFFF)
            .borderColor(0x60FFFFFF)
            .hoverColor(0xFF00FFAA)
            .borderWidth(1)
            .borderRadius(12)
            .hasShadow(true)
            .shadowColor(0x60000000)
            .shadowOffsetY(4)
            .shadowBlur(12)
            .opacity(0.9f)
            .build();
    }
    
    public static UIStyle getMinimal() {
        return UIStyle.builder()
            .backgroundColor(0x00000000)
            .foregroundColor(0xFFFFFFFF)
            .borderColor(0x00000000)
            .hoverColor(0xFFCCCCCC)
            .activeColor(0xFFFFFFFF)
            .borderWidth(0)
            .borderRadius(0)
            .hasShadow(false)
            .build();
    }
    
    public static UIStyle getNeon() {
        return UIStyle.builder()
            .backgroundColor(0xDD0A0A0A)
            .foregroundColor(0xFF00FFFF)
            .borderColor(0xFFFF00FF)
            .hoverColor(0xFFFFFF00)
            .activeColor(0xFF00FF00)
            .borderWidth(2)
            .borderRadius(4)
            .hasShadow(true)
            .shadowColor(0xFFFF00FF)
            .shadowOffsetY(0)
            .shadowBlur(8)
            .build();
    }

    public static UIStyle getPurpleSmooth() {
        return UIStyle.builder()
            .backgroundColor(0xFF2A1B3D) // Solid dark purple background
            .foregroundColor(0xFFE0E0E0) // Light gray text
            .borderColor(0xFF6A5ACD) // Slate blue border
            .hoverColor(0xFF8A2BE2) // Blue violet hover
            .activeColor(0xFFDA70D6) // Orchid active
            .disabledColor(0xFF4B0082) // Indigo disabled
            .borderWidth(2.0f) // Thicker border for definition
            .borderRadius(16) // More rounded for smoothness
            .hasShadow(true)
            .shadowColor(0x80000000) // Semi-transparent shadow
            .shadowOffsetX(0)
            .shadowOffsetY(6)
            .shadowBlur(16) // More blur for premium look
            .fontSize(1.0f)
            .boldText(false)
            .italicText(false)
            .shadowedText(true)
            .hoverTransitionDuration(250) // Smoother transitions
            .clickTransitionDuration(200)
            .opacity(1.0f) // Full opacity
            .build();
    }
}