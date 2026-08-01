import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class SkyblockAddonsInstallerFrame extends JFrame implements ActionListener {

    private static final OperatingSystem CURRENT_OS = detectOperatingSystem();
    private static final Pattern IN_MODS_SUBFOLDER = Pattern.compile("1\\.21\\.11[/\\\\]?$");
    private static final List<File> LAUNCHER_CANDIDATE_DIRS = buildCandidateInstanceDirs();

    private static final int TOTAL_HEIGHT = 435;
    private static final int TOTAL_WIDTH = 404;

    private JLabel logo = null;
    private JLabel versionInfo = null;
    private JLabel labelFolder = null;

    private JPanel panelCenter = null;
    private JPanel panelBottom = null;
    private JPanel totalContentPane = null;

    private JTextArea descriptionText = null;
    private JTextArea forgeDescriptionText = null;

    private JTextField textFieldFolderLocation = null;
    private JButton buttonChooseFolder = null;

    private JButton buttonInstall = null;
    private JButton buttonOpenFolder = null;
    private JButton buttonClose = null;

    private int x = 0;
    private int y = 0;

    private int w = TOTAL_WIDTH;
    private int h;
    private int margin;

    public SkyblockAddonsInstallerFrame() {
        try {
            setName("SkyblockAddonsInstallerFrame");
            setTitle("SkyblockAddons Installer");
            setResizable(false);
            setSize(TOTAL_WIDTH, TOTAL_HEIGHT);
            setContentPane(getPanelContentPane());

            getButtonFolder().addActionListener(this);
            getButtonInstall().addActionListener(this);
            getButtonOpenFolder().addActionListener(this);
            getButtonClose().addActionListener(this);
            getFabricTextArea().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://fabricmc.net/use/installer/"));
                    } catch (IOException | URISyntaxException ex) {
                        showErrorPopup(ex);
                    }
                }
            });

            pack();
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            getFieldFolder().setText(getModsFolder().getPath());
            getButtonInstall().setEnabled(true);
            SwingUtilities.invokeLater(() -> getButtonInstall().requestFocusInWindow());
        } catch (Exception ex) {
            showErrorPopup(ex);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SkyblockAddonsInstallerFrame frame = new SkyblockAddonsInstallerFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception ex) {
            showErrorPopup(ex);
        }
    }

    private JPanel getPanelContentPane() {
        if (totalContentPane == null) {
            try {
                totalContentPane = new JPanel();
                totalContentPane.setName("PanelContentPane");
                totalContentPane.setLayout(new BorderLayout(5, 5));
                totalContentPane.setPreferredSize(new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT));
                totalContentPane.add(getPanelCenter(), "Center");
                totalContentPane.add(getPanelBottom(), "South");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return totalContentPane;
    }

    private JPanel getPanelCenter() {
        if (panelCenter == null) {
            try {
                (panelCenter = new JPanel()).setName("PanelCenter");
                panelCenter.setLayout(null);
                panelCenter.add(getPictureLabel(), getPictureLabel().getName());
                panelCenter.add(getVersionInfo(), getVersionInfo().getName());
                panelCenter.add(getTextArea(), getTextArea().getName());
                panelCenter.add(getFabricTextArea(), getFabricTextArea().getName());
                panelCenter.add(getLabelFolder(), getLabelFolder().getName());
                panelCenter.add(getFieldFolder(), getFieldFolder().getName());
                panelCenter.add(getButtonFolder(), getButtonFolder().getName());
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return panelCenter;
    }

    private JLabel getPictureLabel() {
        if (logo == null) {
            try {
                h = w/2;
                margin = 5;

                BufferedImage myPicture = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream("assets/skyblockaddons/logo.png"), "Logo not found."));
                Image scaled = myPicture.getScaledInstance(w-margin*2, h-margin, Image.SCALE_SMOOTH);
                logo = new JLabel(new ImageIcon(scaled));
                logo.setName("Logo");
                logo.setBounds(x+margin, y+margin, w-margin*2, h-margin);
                logo.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
                logo.setHorizontalAlignment(SwingConstants.CENTER);
                logo.setPreferredSize(new Dimension(w, h));

                y += h;
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return logo;
    }

    private JLabel getVersionInfo() {
        if (versionInfo == null) {
            try {
                h = 25;

                versionInfo = new JLabel();
                versionInfo.setName("LabelMcVersion");
                versionInfo.setBounds(x, y, w, h);
                versionInfo.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
                versionInfo.setHorizontalAlignment(SwingConstants.CENTER);
                versionInfo.setPreferredSize(new Dimension(w, h));
                versionInfo.setText("v"+ this.getStringFieldFromModInfo("version")+" reborn by Fix3dll - for Minecraft 1.21.11");

                y += h;
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return versionInfo;
    }

    private JTextArea getTextArea() {
        if (descriptionText == null) {
            try {
                h = 60;
                margin = 10;

                descriptionText = new JTextArea();
                descriptionText.setName("TextArea");
                setTextAreaProperties(descriptionText);
                descriptionText.setText(
                        "This installer will copy SkyblockAddons into your mods folder for you, and replace any old versions that already exist. " +
                        "Close this if you prefer to do this yourself!"
                );
                descriptionText.setWrapStyleWord(true);

                y += h;
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return descriptionText;
    }

    private void setTextAreaProperties(JTextArea textArea) {
        textArea.setBounds(x+margin, y+margin, w-margin*2, h-margin);
        textArea.setEditable(false);
        textArea.setHighlighter(null);
        textArea.setEnabled(true);
        textArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setPreferredSize(new Dimension(w-margin*2, h-margin));
    }

    private JTextArea getFabricTextArea() {
        if (forgeDescriptionText == null) {
            try {
                h = 55;
                margin = 10;

                forgeDescriptionText = new JTextArea();
                forgeDescriptionText.setName("TextAreaForge");
                setTextAreaProperties(forgeDescriptionText);
                forgeDescriptionText.setText(
                        "However, you still need to install Fabric client in order to be able to run this mod. " +
                        "Click here to visit the download page for FabricMC!"
                );
                forgeDescriptionText.setForeground(Color.BLUE.darker());
                forgeDescriptionText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                forgeDescriptionText.setWrapStyleWord(true);

                y += h;
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return forgeDescriptionText;
    }

    private JLabel getLabelFolder() {
        if (labelFolder == null) {
            h = 16;
            w = 65;

            x += 10; // Padding

            try {
                labelFolder = new JLabel();
                labelFolder.setName("LabelFolder");
                labelFolder.setBounds(x, y+2, w, h);
                labelFolder.setPreferredSize(new Dimension(w, h));
                labelFolder.setText("Mods Folder");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }

            x += w;
        }
        return labelFolder;
    }

    private JTextField getFieldFolder() {
        if (textFieldFolderLocation == null) {
            h = 20;
            w = 287;

            try {
                textFieldFolderLocation = new JTextField();
                textFieldFolderLocation.setName("FieldFolder");
                textFieldFolderLocation.setBounds(x, y, w, h);
                textFieldFolderLocation.setEditable(true);
                textFieldFolderLocation.setPreferredSize(new Dimension(w, h));
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }

            x += w;
        }
        return textFieldFolderLocation;
    }

    private JButton getButtonFolder() {
        if (buttonChooseFolder == null) {
            h = 20;
            w = 25;

            x += 10; // Padding

            try {
                BufferedImage myPicture = ImageIO.read(Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream("assets/skyblockaddons/gui/folder.png"),
                        "Folder icon not found."
                ));
                Image scaled = myPicture.getScaledInstance(w-8, h-6, Image.SCALE_SMOOTH);
                buttonChooseFolder = new JButton(new ImageIcon(scaled));
                buttonChooseFolder.setName("ButtonFolder");
                buttonChooseFolder.setBounds(x, y, w, h);
                buttonChooseFolder.setPreferredSize(new Dimension(w, h));
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return buttonChooseFolder;
    }

    private JPanel getPanelBottom() {
        if (panelBottom == null) {
            try {
                panelBottom = new JPanel();
                panelBottom.setName("PanelBottom");
                panelBottom.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
                panelBottom.setPreferredSize(new Dimension(390, 55));
                panelBottom.add(getButtonInstall(), getButtonInstall().getName());
                panelBottom.add(getButtonOpenFolder(), getButtonOpenFolder().getName());
                panelBottom.add(getButtonClose(), getButtonClose().getName());
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return panelBottom;
    }

    private JButton getButtonInstall() {
        if (buttonInstall == null) {
            w = 100;
            h = 26;

            try {
                buttonInstall = new JButton();
                buttonInstall.setName("ButtonInstall");
                buttonInstall.setPreferredSize(new Dimension(w, h));
                buttonInstall.setText("Install");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return buttonInstall;
    }

    private JButton getButtonOpenFolder() {
        if (buttonOpenFolder == null) {
            w = 130;
            h = 26;

            try {
                buttonOpenFolder = new JButton();
                buttonOpenFolder.setName("ButtonOpenFolder");
                buttonOpenFolder.setPreferredSize(new Dimension(w, h));
                buttonOpenFolder.setText("Open Mods Folder");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return buttonOpenFolder;
    }

    private JButton getButtonClose() {
        if (buttonClose == null) {
            w = 100;
            h = 26;

            try {
                (buttonClose = new JButton()).setName("ButtonClose");
                buttonClose.setPreferredSize(new Dimension(w, h));
                buttonClose.setText("Cancel");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return buttonClose;
    }

    public void onFolderSelect() {
        File currentDirectory = new File(getFieldFolder().getText().trim());
        if (!currentDirectory.isDirectory()) {
            currentDirectory = currentDirectory.getParentFile();
        }
        if (currentDirectory == null || !currentDirectory.exists()) {
            currentDirectory = new File(System.getProperty("user.home"));
        }

        JFileChooser chooser = new JFileChooser(currentDirectory);
        chooser.setDialogTitle("Select Mods Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (selected != null) {
                getFieldFolder().setText(selected.getAbsolutePath());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == getButtonClose()) {
            dispose();
            System.exit(0);
        }
        if (e.getSource() == getButtonFolder()) {
            onFolderSelect();
        }
        if (e.getSource() == getButtonInstall()) {
            onInstall();
        }
        if (e.getSource() == getButtonOpenFolder()) {
            onOpenFolder();
        }
    }

    public void onInstall() {
        try {
            File modsFolder = new File(getFieldFolder().getText().trim());
            if (!modsFolder.exists() && !modsFolder.mkdirs()) {
                showErrorMessage("Folder could not be created: " + modsFolder.getPath());
                return;
            }
            if (!modsFolder.isDirectory()) {
                showErrorMessage("Not a folder: " + modsFolder.getPath());
                return;
            }
            tryInstall(modsFolder);
        } catch (Exception e) {
            showErrorPopup(e);
        }
    }

    private void tryInstall(File modsFolder) {
        File thisFile = getThisFile();

        if (thisFile != null) {
            boolean inSubFolder = IN_MODS_SUBFOLDER.matcher(modsFolder.getPath()).find();

            String targetJarName = this.getStringFieldFromModInfo("sbaJarName");
            if (targetJarName.isBlank() || targetJarName.startsWith("${")) {
                targetJarName = thisFile.getName();
            }

            File newFile = new File(modsFolder, targetJarName);
            if (thisFile.equals(newFile)) {
                showErrorMessage("You are opening this file from where the file should be installed... there's nothing to be done!");
                return;
            }

            boolean deletingFailure = false;
            if (modsFolder.isDirectory()) { // Delete in this current folder.
                boolean failed = findSkyblockAddonsAndDelete(modsFolder.listFiles());
                if (failed) deletingFailure = true;
            }
            if (inSubFolder) { // We are in the 1.21.11 folder, delete in the parent folder as well.
                if (modsFolder.getParentFile().isDirectory()) {
                    boolean failed = findSkyblockAddonsAndDelete(modsFolder.getParentFile().listFiles());
                    if (failed) deletingFailure = true;
                }
            } else { // We are in the main mods folder, but the 1.21.11 subfolder exists... delete in there too.
                File subFolder = new File(modsFolder, "1.21.11");
                if (subFolder.exists() && subFolder.isDirectory()) {
                    boolean failed = findSkyblockAddonsAndDelete(subFolder.listFiles());
                    if (failed) deletingFailure = true;
                }
            }

            if (deletingFailure) return;

            if (thisFile.isDirectory()) {
                showErrorMessage("This file is a directory... Are we in a development environment?");
                return;
            }

            try {
                Files.copy(thisFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                showErrorPopup(ex);
                return;
            }

            showMessage("SkyblockAddons has been successfully installed into your mods folder.");
            dispose();
            System.exit(0);
        }
    }

    private boolean findSkyblockAddonsAndDelete(File[] files) {
        if (files == null) return false;

        for (File file : files) {
            if (isSkyblockAddonsJar(file)) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showErrorMessage(
                            "Was not able to delete the other SkyblockAddons files found in your mods folder!" + System.lineSeparator() +
                            "Please make sure that your minecraft is currently closed and try again, or feel" + System.lineSeparator() +
                            "free to open your mods folder and delete those files manually."
                    );
                    return true;
                }
            }
        }
        return false;
    }

    public void onOpenFolder() {
        try {
            File modsFolder = new File(getFieldFolder().getText().trim());
            if (!modsFolder.exists()) {
                showErrorMessage("Target mods directory does not exist: " + modsFolder.getPath());
                return;
            }
            Desktop.getDesktop().open(modsFolder);
        } catch (Exception e) {
            showErrorPopup(e);
        }
    }

    public File getModsFolder() {
        String userHome = System.getProperty("user.home", ".");

        File firstExistingLauncher = null;

        // 1. Search launchers for an instance mods folder that contains SkyblockAddons
        for (File candidateDir : LAUNCHER_CANDIDATE_DIRS) {
            if (candidateDir != null && candidateDir.exists() && candidateDir.isDirectory()) {
                if (firstExistingLauncher == null) {
                    firstExistingLauncher = candidateDir; // Cache the first existing launcher
                }
                File sbaMods = findInstanceWithSkyblockAddons(candidateDir);
                if (sbaMods != null) {
                    return sbaMods;
                }
            }
        }

        // 2. Search Vanilla Minecraft for mods folder that contains SkyblockAddons
        File subFolderMods = getFile(userHome, "minecraft/mods/1.21.11");
        if (hasSkyblockAddonsInModsFolder(subFolderMods)) {
            return subFolderMods;
        }

        File vanillaMods = getFile(userHome, "minecraft/mods");
        if (hasSkyblockAddonsInModsFolder(vanillaMods)) {
            return vanillaMods;
        }

        // 3. No SkyblockAddons found anywhere: return first existing launcher instances root directory
        if (firstExistingLauncher != null) {
            return firstExistingLauncher;
        }

        // 4. Fallback to Vanilla Minecraft mods directory
        if (subFolderMods.exists() && subFolderMods.isDirectory()) {
            return subFolderMods;
        }
        return vanillaMods;
    }

    private File findInstanceWithSkyblockAddons(File instancesDir) {
        if (instancesDir != null && instancesDir.exists() && instancesDir.isDirectory()) {
            File[] instances = instancesDir.listFiles();
            if (instances != null) {
                for (File inst : instances) {
                    if (inst.isDirectory()) {
                        File m1 = new File(inst, "mods");
                        if (hasSkyblockAddonsInModsFolder(m1)) return m1;

                        File m2 = new File(inst, ".minecraft/mods");
                        if (hasSkyblockAddonsInModsFolder(m2)) return m2;

                        File m3 = new File(inst, "minecraft/mods");
                        if (hasSkyblockAddonsInModsFolder(m3)) return m3;
                    }
                }
            }
        }
        return null;
    }

    private static boolean hasSkyblockAddonsInModsFolder(File modsDir) {
        if (modsDir == null || !modsDir.exists() || !modsDir.isDirectory()) return false;
        File[] files = modsDir.listFiles();
        if (files == null) return false;

        for (File file : files) {
            if (isSkyblockAddonsJar(file)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSkyblockAddonsJar(File file) {
        if (file == null || file.isDirectory() || !file.getName().toLowerCase(Locale.ENGLISH).endsWith(".jar")) {
            return false;
        }
        try (JarFile jarFile = new JarFile(file)) {
            ZipEntry entry = jarFile.getEntry("fabric.mod.json");
            if (entry != null) {
                try (InputStream is = jarFile.getInputStream(entry)) {
                    return "skyblockaddons".equalsIgnoreCase(readFieldFromStream(is, "id"));
                }
            }
        } catch (Exception ignored) {
            // Not a valid mod JAR.
        }
        return false;
    }

    public File getFile(String userHome, String minecraftPath) {
        File workingDirectory;
        switch (CURRENT_OS) {
            case LINUX:
            case SOLARIS: {
                workingDirectory = new File(userHome, '.' + minecraftPath + '/');
                break;
            }
            case WINDOWS: {
                String applicationData = System.getenv("APPDATA");
                if (applicationData != null) {
                    workingDirectory = new File(applicationData, "." + minecraftPath + '/');
                    break;
                }
                workingDirectory = new File(userHome, '.' + minecraftPath + '/');
                break;
            }
            case MACOS: {
                workingDirectory = new File(userHome, "Library/Application Support/" + minecraftPath);
                break;
            }
            default: {
                workingDirectory = new File(userHome, minecraftPath + '/');
                break;
            }
        }
        return workingDirectory;
    }

    private static OperatingSystem detectOperatingSystem() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);
        if (osName.contains("win")) return OperatingSystem.WINDOWS;
        if (osName.contains("mac")) return OperatingSystem.MACOS;
        if (osName.contains("solaris") || osName.contains("sunos")) return OperatingSystem.SOLARIS;
        if (osName.contains("linux") || osName.contains("unix")) return OperatingSystem.LINUX;
        return OperatingSystem.UNKNOWN;
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "SkyblockAddons", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "SkyblockAddons - Error", JOptionPane.ERROR_MESSAGE);
    }

    public enum OperatingSystem {
        LINUX,
        SOLARIS,
        WINDOWS,
        MACOS,
        UNKNOWN
    }

    private static String getStacktraceText(Throwable ex) {
        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().replace("\t", "  ");
    }

    private static void showErrorPopup(Throwable ex) {
        ex.printStackTrace();

        JTextArea textArea = new JTextArea(getStacktraceText(ex));
        textArea.setEditable(false);
        Font currentFont = textArea.getFont();
        Font newFont = new Font(Font.MONOSPACED, currentFont.getStyle(), currentFont.getSize());
        textArea.setFont(newFont);

        JScrollPane errorScrollPane = new JScrollPane(textArea);
        errorScrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(null, errorScrollPane, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private File getThisFile() {
        try {
            return new File(SkyblockAddonsInstallerFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            showErrorPopup(ex);
        }
        return null;
    }

    private String getStringFieldFromModInfo(String fieldName) {
        return readFieldFromStream(getClass().getClassLoader().getResourceAsStream("fabric.mod.json"), fieldName);
    }

    private static String readFieldFromStream(InputStream is, String fieldName) {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("\"" + fieldName + "\": \"")) {
                    String val = line.split(Pattern.quote("\"" + fieldName + "\": \""))[1];
                    boolean endsWithComma = val.contains(",");
                    return val.substring(0, val.length() - (endsWithComma ? 2 : 1));
                }
            }
        } catch (Exception ignored) {
            // Ignore invalid or unreadable JARs.
        }
        return "";
    }

    private static List<File> buildCandidateInstanceDirs() {
        List<File> list = new ArrayList<>();
        String userHome = System.getProperty("user.home", ".");
        String appData = System.getenv("APPDATA");
        String localAppData = System.getenv("LOCALAPPDATA");

        switch (CURRENT_OS) {
            case WINDOWS: {
                if (appData != null) {
                    list.add(new File(appData, "PrismLauncher/instances"));
                    list.add(new File(appData, "Prism/instances"));
                    list.add(new File(appData, "MultiMC/instances"));
                    list.add(new File(appData, "Modrinth App/profiles"));
                    list.add(new File(appData, "com.modrinth.themely/profiles"));
                    list.add(new File(appData, "curseforge/minecraft/Instances"));
                }
                if (localAppData != null) {
                    list.add(new File(localAppData, "PrismLauncher/instances"));
                    list.add(new File(localAppData, "Prism/instances"));
                    list.add(new File(localAppData, "MultiMC/instances"));
                    list.add(new File(localAppData, "Modrinth App/profiles"));
                    list.add(new File(localAppData, "com.modrinth.themely/profiles"));
                    list.add(new File(localAppData, "curseforge/minecraft/Instances"));
                }
                list.add(new File(userHome, "PrismLauncher/instances"));
                list.add(new File(userHome, "Desktop/PrismLauncher/instances"));
                list.add(new File(userHome, "curseforge/minecraft/Instances"));
                list.add(new File("C:/PrismLauncher/instances"));
                list.add(new File("D:/PrismLauncher/instances"));
                break;
            }
            case MACOS: {
                list.add(new File(userHome, "Library/Application Support/PrismLauncher/instances"));
                list.add(new File(userHome, "Library/Application Support/MultiMC/instances"));
                list.add(new File(userHome, "Library/Application Support/Modrinth App/profiles"));
                list.add(new File(userHome, "Library/Application Support/com.modrinth.themely/profiles"));
                list.add(new File(userHome, "Library/Application Support/curseforge/minecraft/Instances"));
                list.add(new File(userHome, "curseforge/minecraft/Instances"));
                break;
            }
            default: {
                list.add(new File(userHome, ".local/share/PrismLauncher/instances"));
                list.add(new File(userHome, ".var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances"));
                list.add(new File(userHome, ".local/share/MultiMC/instances"));
                list.add(new File(userHome, ".local/share/Modrinth App/profiles"));
                list.add(new File(userHome, ".local/share/com.modrinth.themely/profiles"));
                list.add(new File(userHome, ".var/app/com.modrinth.themely/data/Modrinth App/profiles"));
                list.add(new File(userHome, ".local/share/curseforge/minecraft/Instances"));
                list.add(new File(userHome, "curseforge/minecraft/Instances"));
                break;
            }
        }

        return Collections.unmodifiableList(list);
    }

}