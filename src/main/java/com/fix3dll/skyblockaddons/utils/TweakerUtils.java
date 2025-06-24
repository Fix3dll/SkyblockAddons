package com.fix3dll.skyblockaddons.utils;

import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class TweakerUtils {
    public static void exit() {
//        FMLCommonHandler.instance().handleExit(-1);
//        FMLCommonHandler.instance().expectServerStopped();
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
                "SkyblockAddons Reborn",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                icon,
                options.length == 0 ? null : allOptions,
                options.length == 0 ? null : allOptions[0]
        );
    }
}
