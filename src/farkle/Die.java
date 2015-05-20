package farkle;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author jterry
 */
public class Die {
    
    private static final Random random = new Random();
    private static final Image[] diceImages = new Image[7];
    static {
        for(int i = 1; i <= diceImages.length; ++i) {
            BufferedInputStream stream;
            try {
                stream = new BufferedInputStream(new FileInputStream("src/imgs/" + i + ".png"));
                diceImages[i - 1] = new Image(stream);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Die.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private boolean isPermanentlySaved;
    private boolean isTentativelySaved;
    private int value;
    private ImageView imageView;

    public Die(ImageView iv) {
        value = 6;
        isPermanentlySaved = false;
        isTentativelySaved = false;
        imageView = iv;
        imageView.setEffect(new DropShadow(2, javafx.scene.paint.Color.BLACK));
    }
    
    public void roll() {
        setValue(random.nextInt(6));
    }
    
    public boolean click() {
        if(isPermanentlySaved)
            return false;
        isTentativelySaved = !isTentativelySaved;
        if(isTentativelySaved()) {
            getImageView().setEffect(new DropShadow(14, javafx.scene.paint.Color.BLACK));
        } else {
            getImageView().setEffect(new DropShadow(2, javafx.scene.paint.Color.BLACK));
        }
        return true;
    }
    
    public void resetClicked() {
        getImageView().setEffect(new DropShadow(2, javafx.scene.paint.Color.BLACK));
    }
    
    public boolean isPermanentlySaved() {
        return isPermanentlySaved;
    }

    public void savePermanently() {
        this.isPermanentlySaved = true;
    }
    
    public void resetPermanentlySaved() {
        this.isPermanentlySaved = false;
    }

    public boolean isTentativelySaved() {
        return isTentativelySaved;
    }
    
    public void resetTentativelySaved() {
        isTentativelySaved = false;
    }

    /**
     * 
     * @return A value between 0 and 5 (inclusive)
     */
    public int getValue() {
        return value;
    }

    /**
     * 
     * @param value A value between 0 and 5 (inclusive)
     */
    public void setValue(int value) {
        this.value = value;
        this.getImageView().setImage(diceImages[value]);
    }
    
    public void resetValue() {
        this.value = 6;
        this.getImageView().setImage(diceImages[value]);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
}
