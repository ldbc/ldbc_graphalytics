package science.atlarge.graphalytics.domain.graph;

/**
 * @author Wing Lung Ngai
 */
public enum GraphScale {
    XXS("XXS", 6.5, 6.9),
    XS("XS", 7.0, 7.4),
    S("S", 7.5, 7.9),
    M("M", 8.0, 8.4),
    L("L", 8.5, 8.9),
    XL("XL", 9.0, 9.4),
    XXL("XXL", 9.5, 10.0);

    public String text;
    public double minSize;
    public double maxSize;

    GraphScale(String text, double minScale, double maxScale) {
        this.text = text;
        this.minSize = minScale;
        this.maxSize = maxScale;
    }
}
