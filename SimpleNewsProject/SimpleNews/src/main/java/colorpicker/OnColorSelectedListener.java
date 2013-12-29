package colorpicker;

/**
 * Created by Daniel on 29.12.13.
 */

/**
 * Interface for a callback when a color square is selected.
 */
public interface OnColorSelectedListener {

    /**
     * Called when a specific color square has been selected.
     */
    public void onColorSelected(int color);
}