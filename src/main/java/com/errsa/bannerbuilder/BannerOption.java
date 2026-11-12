package com.errsa.bannerbuilder;

import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;

public record BannerOption(
        String displayName,
        PatternType patternType,
        Material patternItem
) {
    public boolean requiresPatternItem() {
        return patternItem != null;
    }
}