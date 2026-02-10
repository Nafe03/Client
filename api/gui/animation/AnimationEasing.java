package dev.anarchy.waifuhax.api.gui.animation;

public enum AnimationEasing {
    LINEAR(t -> t),
    EASE_IN(t -> t * t),
    EASE_OUT(t -> t * (2 - t)),
    EASE_IN_OUT(t -> t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t),
    EASE_IN_CUBIC(t -> t * t * t),
    EASE_OUT_CUBIC(t -> {
        float f = t - 1;
        return f * f * f + 1;
    }),
    EASE_IN_OUT_CUBIC(t -> t < 0.5f ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1),
    EASE_IN_QUAD(t -> t * t),
    EASE_OUT_QUAD(t -> t * (2 - t)),
    EASE_IN_OUT_QUAD(t -> t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t),
    BOUNCE_OUT(t -> {
        if (t < (1 / 2.75f)) {
            return 7.5625f * t * t;
        } else if (t < (2 / 2.75f)) {
            t -= (1.5f / 2.75f);
            return 7.5625f * t * t + 0.75f;
        } else if (t < (2.5f / 2.75f)) {
            t -= (2.25f / 2.75f);
            return 7.5625f * t * t + 0.9375f;
        } else {
            t -= (2.625f / 2.75f);
            return 7.5625f * t * t + 0.984375f;
        }
    }),
    ELASTIC_OUT(t -> {
        if (t == 0 || t == 1) return t;
        float p = 0.3f;
        return (float) (Math.pow(2, -10 * t) * Math.sin((t - p / 4) * (2 * Math.PI) / p) + 1);
    }),
    BACK_OUT(t -> {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    });
    
    private final EasingFunction function;
    
    AnimationEasing(EasingFunction function) {
        this.function = function;
    }
    
    public float apply(float t) {
        return function.apply(t);
    }
    
    @FunctionalInterface
    private interface EasingFunction {
        float apply(float t);
    }
}