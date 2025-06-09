package ai.robocoders.control.shelly.model;

/**
 * Data transfer object for RGB color data.
 */
public class ColorData {
    private int red;
    private int green;
    private int blue;

    // Default constructor required for JSON deserialization
    public ColorData() {
    }

    public ColorData(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    /**
     * Calculate the white value as the minimum of R, G, and B.
     * This helps reduce color saturation.
     *
     * @return The calculated white value
     */
    public int calculateWhite() {
        return Math.min(Math.min(red, green), blue);
    }

    @Override
    public String toString() {
        return "ColorData{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                '}';
    }
}