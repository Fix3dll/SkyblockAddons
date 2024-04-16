package codes.biscuit.skyblockaddons.utils;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * System exit call and show message for reason
 * @see <a href="https://github.com/Skytils/SkytilsMod/blob/293ebf80522daf105da19ddb8ad27fa4fc5f9af9/src/main/java/gg/skytils/skytilsmod/tweaker/TweakerUtil.java">Skytils TweakerUtil</a>
 */
public class TweakerUtils {
    public static void exit() {
        FMLCommonHandler.instance().handleExit(-1);
        FMLCommonHandler.instance().expectServerStopped();
    }

    public static void showMessage(String errorMessage, JButton... options) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // This makes the JOptionPane show on taskbar and stay on top
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Icon icon = null;
        try {
            URL url = TweakerUtils.class.getResource("/assets/skyblockaddons/logo.png");
            if (url != null) {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(url).getScaledInstance(99, 50, Image.SCALE_DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JButton[] allOptions = ArrayUtils.addAll(new JButton[]{}, options);
        JOptionPane.showOptionDialog(
                frame,
                errorMessage,
                "SkyblockAddons Unofficial",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                icon,
                options.length == 0 ? null : allOptions,
                options.length == 0 ? null : allOptions[0]
        );
    }
}
