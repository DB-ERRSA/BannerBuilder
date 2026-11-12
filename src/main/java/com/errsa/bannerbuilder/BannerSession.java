package com.errsa.bannerbuilder;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;
import java.util.List;

public class BannerSession {

    private DyeColor selectedColor = DyeColor.WHITE;
    private Material baseBannerMaterial = Material.WHITE_BANNER;

    private final List<Pattern> originalPatterns = new ArrayList<>();
    private final List<Pattern> pendingPatterns = new ArrayList<>();

    private int page = 1;

    public DyeColor getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(DyeColor selectedColor) {
        this.selectedColor = selectedColor;
    }

    public Material getBaseBannerMaterial() {
        return baseBannerMaterial;
    }

    public void setBaseBannerMaterial(Material baseBannerMaterial) {
        this.baseBannerMaterial = baseBannerMaterial;
    }

    public List<Pattern> getOriginalPatterns() {
        return new ArrayList<>(originalPatterns);
    }

    public List<Pattern> getPendingPatterns() {
        return new ArrayList<>(pendingPatterns);
    }

    public List<Pattern> getPreviewPatterns() {
        List<Pattern> combined = new ArrayList<>(originalPatterns);
        combined.addAll(pendingPatterns);
        return combined;
    }

    public void loadBanner(
            Material bannerMaterial,
            List<Pattern> patterns
    ) {
        baseBannerMaterial = bannerMaterial;

        originalPatterns.clear();
        originalPatterns.addAll(patterns);

        pendingPatterns.clear();
        page = 1;
    }

    public void addPattern(PatternType patternType) {
        pendingPatterns.add(
                new Pattern(selectedColor, patternType)
        );
    }

    public void undoPending() {
        if (!pendingPatterns.isEmpty()) {
            pendingPatterns.remove(pendingPatterns.size() - 1);
        }
    }

    public void clearPending() {
        pendingPatterns.clear();
    }

    public void commit() {
        originalPatterns.addAll(pendingPatterns);
        pendingPatterns.clear();
    }

    public void resetAll() {
        originalPatterns.clear();
        pendingPatterns.clear();
    }

    public int originalLayerCount() {
        return originalPatterns.size();
    }

    public int pendingLayerCount() {
        return pendingPatterns.size();
    }

    public int totalLayerCount() {
        return originalPatterns.size() + pendingPatterns.size();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(
                1,
                Math.min(BannerPatterns.getPageCount(), page)
        );
    }

    public void nextPage() {
        setPage(page + 1);
    }

    public void previousPage() {
        setPage(page - 1);
    }
}