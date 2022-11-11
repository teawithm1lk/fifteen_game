import javax.swing.*;
import java.awt.*;

public class Main {
    private static final Dimension FRAME_DIM = Toolkit.getDefaultToolkit().getScreenSize();

    private static final Dimension BUTTON_DIM = new Dimension(100, 50);
    private static final Point EXIT_BUTTON_POS = new Point(FRAME_DIM.width - BUTTON_DIM.width, 0);
    private static final Point RESTART_BUTTON_POS = new Point(FRAME_DIM.width - BUTTON_DIM.width, 55);

    public static void main(String[] args) {
        JFrame frame = new JFrame("Barley-Break Game");
        Game game = new Game(frame);
        game.setPreferredSize(FRAME_DIM);

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> game.reset());
        restartButton.setSize(BUTTON_DIM);
        restartButton.setLocation(RESTART_BUTTON_POS);
        frame.getContentPane().add(restartButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setSize(BUTTON_DIM);
        exitButton.setLocation(EXIT_BUTTON_POS);
        frame.getContentPane().add(exitButton);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

        game.start();
    }
}