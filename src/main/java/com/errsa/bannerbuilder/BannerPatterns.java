package com.errsa.bannerbuilder;

import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;

import java.util.List;

public final class BannerPatterns {

    private BannerPatterns() {
    }

    /*
     * Page 1:
     * Corners, stripes, crosses and triangles.
     */
    public static final List<BannerOption> PAGE_ONE = List.of(
            normal("Bottom Left Corner", PatternType.SQUARE_BOTTOM_LEFT),
            normal("Bottom Right Corner", PatternType.SQUARE_BOTTOM_RIGHT),
            normal("Top Left Corner", PatternType.SQUARE_TOP_LEFT),
            normal("Top Right Corner", PatternType.SQUARE_TOP_RIGHT),

            normal("Bottom Stripe", PatternType.STRIPE_BOTTOM),
            normal("Top Stripe", PatternType.STRIPE_TOP),
            normal("Left Stripe", PatternType.STRIPE_LEFT),
            normal("Right Stripe", PatternType.STRIPE_RIGHT),
            normal("Center Stripe", PatternType.STRIPE_CENTER),
            normal("Middle Stripe", PatternType.STRIPE_MIDDLE),
            normal("Down Right Stripe", PatternType.STRIPE_DOWNRIGHT),
            normal("Down Left Stripe", PatternType.STRIPE_DOWNLEFT),
            normal("Small Stripes", PatternType.SMALL_STRIPES),

            normal("Diagonal Cross", PatternType.CROSS),
            normal("Straight Cross", PatternType.STRAIGHT_CROSS),

            normal("Bottom Triangle", PatternType.TRIANGLE_BOTTOM),
            normal("Top Triangle", PatternType.TRIANGLE_TOP),
            normal("Bottom Teeth", PatternType.TRIANGLES_BOTTOM)
    );

    /*
     * Page 2:
     * Diagonals, halves, shapes and gradients.
     */
    public static final List<BannerOption> PAGE_TWO = List.of(
            normal("Top Teeth", PatternType.TRIANGLES_TOP),

            normal("Diagonal Left", PatternType.DIAGONAL_LEFT),
            normal("Diagonal Up Right", PatternType.DIAGONAL_UP_RIGHT),
            normal("Diagonal Up Left", PatternType.DIAGONAL_UP_LEFT),
            normal("Diagonal Right", PatternType.DIAGONAL_RIGHT),

            normal("Circle", PatternType.CIRCLE),
            normal("Rhombus", PatternType.RHOMBUS),

            normal("Left Half", PatternType.HALF_VERTICAL),
            normal("Top Half", PatternType.HALF_HORIZONTAL),
            normal("Right Half", PatternType.HALF_VERTICAL_RIGHT),
            normal("Bottom Half", PatternType.HALF_HORIZONTAL_BOTTOM),

            normal("Border", PatternType.BORDER),
            normal("Gradient Down", PatternType.GRADIENT),
            normal("Gradient Up", PatternType.GRADIENT_UP)
    );

    /*
     * Page 3:
     * Patterns that require a banner-pattern item in a vanilla loom.
     */
    public static final List<BannerOption> PAGE_THREE = List.of(
            special(
                    "Creeper Charge",
                    PatternType.CREEPER,
                    Material.CREEPER_BANNER_PATTERN
            ),
            special(
                    "Skull Charge",
                    PatternType.SKULL,
                    Material.SKULL_BANNER_PATTERN
            ),
            special(
                    "Flower Charge",
                    PatternType.FLOWER,
                    Material.FLOWER_BANNER_PATTERN
            ),
            special(
                    "Thing",
                    PatternType.MOJANG,
                    Material.MOJANG_BANNER_PATTERN
            ),
            special(
                    "Globe",
                    PatternType.GLOBE,
                    Material.GLOBE_BANNER_PATTERN
            ),
            special(
                    "Snout",
                    PatternType.PIGLIN,
                    Material.PIGLIN_BANNER_PATTERN
            ),
            special(
                    "Flow",
                    PatternType.FLOW,
                    Material.FLOW_BANNER_PATTERN
            ),
            special(
                    "Guster",
                    PatternType.GUSTER,
                    Material.GUSTER_BANNER_PATTERN
            ),
            special(
                    "Field Masoned",
                    PatternType.BRICKS,
                    Material.FIELD_MASONED_BANNER_PATTERN
            ),
            special(
                    "Bordure Indented",
                    PatternType.CURLY_BORDER,
                    Material.BORDURE_INDENTED_BANNER_PATTERN
            )
    );

    public static List<BannerOption> getPage(int page) {
        return switch (page) {
            case 2 -> PAGE_TWO;
            case 3 -> PAGE_THREE;
            default -> PAGE_ONE;
        };
    }

    public static int getPageCount() {
        return 3;
    }

    private static BannerOption normal(
            String name,
            PatternType patternType
    ) {
        return new BannerOption(name, patternType, null);
    }

    private static BannerOption special(
            String name,
            PatternType patternType,
            Material patternItem
    ) {
        return new BannerOption(name, patternType, patternItem);
    }
}