//import java.awt.Image;
//import java.net.URL;
//import javax.swing.ImageIcon;
//
//public class ImageLoader {
//
//    public static Image load(String path) {
//        try {
//            URL url = ImageLoader.class.getResource(path);
//            if (url == null) {
//                System.err.println("[ImageLoader] Resource not found: " + path);
//                return null;
//            }
//            return new ImageIcon(url).getImage();
//        } catch (Exception e) {
//            System.err.println("[ImageLoader] Failed to load: " + path + " — " + e.getMessage());
//            return null;
//        }
//    }
//}