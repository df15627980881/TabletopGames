package guide;

import javax.swing.*;
import java.awt.*;

public class DialogUtils {

    public static JDialog create(Frame frame, String title, boolean modal, Integer width, Integer height, String labelText) {
        JDialog tutorialDialog = new JDialog(frame, title, modal);
        tutorialDialog.setSize(width, height);
        tutorialDialog.setLayout(new BorderLayout());
        tutorialDialog.setModal(true);
        Font font = new Font("Arial", Font.BOLD, 20);
        JLabel label = new JLabel(labelText);
        label.setFont(font);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tutorialDialog.add(label, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> tutorialDialog.dispose());
        tutorialDialog.add(closeButton, BorderLayout.SOUTH);

        tutorialDialog.pack();
        tutorialDialog.setLocationRelativeTo(frame);
//        tutorialDialog.setVisible(true);
        return tutorialDialog;
    }

    public static JDialog createWithoutPack(Frame frame, String title, boolean modal, Integer width, Integer height, String labelText) {
        JDialog tutorialDialog = new JDialog(frame, title, modal);
        tutorialDialog.setSize(width, height);
        tutorialDialog.setLayout(new BorderLayout());
        tutorialDialog.setModal(true);

        JLabel label = new JLabel(labelText);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tutorialDialog.add(label, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> tutorialDialog.dispose());
        tutorialDialog.add(closeButton, BorderLayout.SOUTH);

        tutorialDialog.setLocationRelativeTo(frame);
//        tutorialDialog.setVisible(true);
        return tutorialDialog;
    }

    public static JDialog createFirstStep(Frame frame, String title, boolean modal, Integer width, Integer height, String labelText, String gameName, String ruleText) {
        JDialog tutorialDialog = new JDialog(frame, title, modal);
        tutorialDialog.setSize(width, height);
        tutorialDialog.setLayout(new BorderLayout());
        tutorialDialog.setModal(true);

        JLabel label = new JLabel(labelText);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tutorialDialog.add(label, BorderLayout.CENTER);

        Container contentPane = tutorialDialog.getContentPane();

        Font font = new Font("Arial", Font.BOLD, 20);
        label.setFont(font);

        JLabel label1 = new JLabel("Now Let's start. Here'are the rules for game " + gameName);
        JLabel label2 = new JLabel(ruleText);
        label1.setAlignmentX(Component.CENTER_ALIGNMENT);
        label2.setAlignmentX(Component.CENTER_ALIGNMENT);
        label1.setFont(font);
        label2.setFont(font);

        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(label1);
        contentPane.add(Box.createVerticalStrut(10));
        contentPane.add(label2);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> tutorialDialog.dispose());
        contentPane.add(closeButton, BorderLayout.SOUTH);
        tutorialDialog.setLocationRelativeTo(frame);
//        tutorialDialog.setVisible(true);
        return tutorialDialog;
    }

    public static void show(JDialog jDialog) {
        jDialog.setVisible(Boolean.TRUE);
    }
}
