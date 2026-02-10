package dev.anarchy.waifuhax.api.gui.styling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UIStyle {
    
    // Colors
    @Builder.Default
    private int backgroundColor = 0x00000000;
    @Builder.Default
    private int foregroundColor = 0xFFFFFFFF;
    @Builder.Default
    private int borderColor = 0xFF888888;
    @Builder.Default
    private int hoverColor = 0xFF00FF00;
    @Builder.Default
    private int activeColor = 0xFF0088FF;
    @Builder.Default
    private int disabledColor = 0xFF444444;
    
    // Border
    @Builder.Default
    private float borderWidth = 0;
    @Builder.Default
    private float borderRadius = 0;
    
    // Shadow
    @Builder.Default
    private boolean hasShadow = false;
    @Builder.Default
    private int shadowColor = 0x80000000;
    @Builder.Default
    private float shadowOffsetX = 0;
    @Builder.Default
    private float shadowOffsetY = 2;
    @Builder.Default
    private float shadowBlur = 4;
    
    // Typography
    @Builder.Default
    private float fontSize = 1.0f;
    @Builder.Default
    private boolean boldText = false;
    @Builder.Default
    private boolean italicText = false;
    @Builder.Default
    private boolean shadowedText = true;
    
    // Animation
    @Builder.Default
    private long hoverTransitionDuration = 150;
    @Builder.Default
    private long clickTransitionDuration = 100;
    
    // Opacity
    @Builder.Default
    private float opacity = 1.0f;
    
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
}