import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Sprite {
    private final Image image;
    private final String name;

    public Sprite(Image image, String name) {
        this.image = image;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void draw(Graphics g,int x,int y) {
        g.drawImage(image,x,y,null);
    }

    public Image getImage() {
        return image;
    }

    public static Sprite getSprite(String path, String name) {
        BufferedImage sourceImage = null;

        try {
            URL url = Sprite.class.getClassLoader().getResource(path);
            sourceImage = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Sprite(Toolkit.getDefaultToolkit().createImage(sourceImage.getSource()), name);
    }

    public static Sprite getSprite(Sprite src) {
        return new Sprite(src.getImage(), src.getName());
    }
}