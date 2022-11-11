import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game extends Canvas implements Runnable {
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    private static final int SPRITES_SIZE = 5;
    private static final int SAMPLE_SIZE = 3;

    private static final int DELTA_X = 100;
    private static final int DELTA_Y = 100;
    private static final int STANDARD_X_FOR_SPRITES = (SCREEN_SIZE.width - SPRITES_SIZE * DELTA_X) / 2;
    private static final int STANDARD_Y_FOR_SPRITES = (SCREEN_SIZE.height - (SPRITES_SIZE + 3) * DELTA_X) / 2;

    private static final int PIC_SIZE = 100;
    private static final Point MIN_CELLS_POINT = new Point(STANDARD_X_FOR_SPRITES,
                                                         STANDARD_Y_FOR_SPRITES + 2 * DELTA_Y);
    private static final Point MAX_CELLS_POINT = new Point(STANDARD_X_FOR_SPRITES + SPRITES_SIZE * DELTA_X,
                                                            STANDARD_Y_FOR_SPRITES + (2 + SPRITES_SIZE) * DELTA_Y);

    private static final Dimension DIALOG_DIM = new Dimension(375, 225);
    private static final Point DIALOG_POS = new Point((SCREEN_SIZE.width - DIALOG_DIM.width) / 2,
                                                    (SCREEN_SIZE.height - DIALOG_DIM.height) / 2);
    private static final Point LABEL_POS = new Point(50, 0);
    private static final Dimension BUTTON_DIM = new Dimension(150, 50);
    private static final Point NEW_GAME_BUTTON_POS = new Point(25, 125);
    private static final Point CLOSE_GAME_BUTTON_POS = new Point(200, 125);

    private static final String[] SPRITE_PATHS_UTIL = {
            "img/block.png",
            "img/empty.png",
            "img/backstage.png",
            "img/dot.png",
    };
    private static final String[] SPRITE_PATHS_BLOCKS = {
            "img/red.png",
            "img/orange.png",
            "img/green.png",
    };
    private static Sprite[] SPRITE_VARIES_UTIL;
    private static Sprite[] SPRITE_VARIES_BLOCKS;
    private final Sprite[][] sprites;
    private final Sprite[] sampleSprites;

    private final Random random;
    private final JFrame frame;
    private boolean isShowedDialog = false;
    private boolean isRunning;

    private String clickedName;
    private boolean clicked = false;
    private int clickedRow = 0;
    private int clickedColumn = 0;

    private boolean gripped = false;
    private int grippedRow = 0;
    private int grippedColumn = 0;

    public Game(JFrame frame) {
        sampleSprites = new Sprite[SAMPLE_SIZE];
        sprites = new Sprite[SPRITES_SIZE][SPRITES_SIZE];
        random = new Random();
        this.frame = frame;

        fillSpriteVaries();
        addMouseListener(new MouseInputHandler());
    }

    @Override
    public void run() {
        reset();

        while (isRunning) {
            render();
            update();
        }
    }

    public void start() {
        isRunning = true;
        new Thread(this).start();
    }

    public void reset() {
        generateSample();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                sprites[i][j * 2 + 1] = Sprite.getSprite(SPRITE_VARIES_UTIL[(i % 2 == 0) ? 0 : 1]);
            }
        }

        fillSprites(getAvailableCells());
    }

    public boolean isAssembled() {
        for (int i = 0; i < sampleSprites.length; i++) {
            for (int j = 0; j < SPRITES_SIZE; j++) {
                if (!sprites[j][i * 2].getName().equals(sampleSprites[i].getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(2);
            requestFocus();
            return;
        }

        Graphics g = bs.getDrawGraphics();
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(SPRITE_VARIES_UTIL[2].getImage(), 0, 0, getWidth(), getHeight(), null);

        paintSprites(g);

        g.dispose();
        bs.show();
    }

    private void update() {
        if (isAssembled() && !isShowedDialog) {
            isShowedDialog = true;
            showDialog();
        }
        if (gripped && clicked) {
            clickedName = sprites[clickedRow][clickedColumn].getName();
            if (isAvailableToSwap()) {
                Sprite temp = sprites[grippedRow][grippedColumn];
                sprites[grippedRow][grippedColumn] = sprites[clickedRow][clickedColumn];
                sprites[clickedRow][clickedColumn] = temp;
            }
            clicked = false;
            gripped = false;
        } else if (clicked) {
            clickedName = sprites[clickedRow][clickedColumn].getName();
            if (!isBlockClicked()) {
                gripped = true;
                grippedColumn = clickedColumn;
                grippedRow = clickedRow;
            }
            clicked = false;
        }
    }

    private void showDialog() {
        JDialog dialog = new JDialog(frame, "Congratulations");
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setLocation(DIALOG_POS);
        dialog.setSize(DIALOG_DIM);

        JButton newGameButton = new JButton("New game");
        newGameButton.addActionListener(e -> {
            reset();
            dialog.dispose();
            isShowedDialog = false;
        });
        newGameButton.setSize(BUTTON_DIM);
        newGameButton.setLocation(NEW_GAME_BUTTON_POS);
        dialog.getContentPane().add(newGameButton, BorderLayout.SOUTH);

        JButton closeGameButton = new JButton("Close game");
        closeGameButton.addActionListener(e -> System.exit(0));
        closeGameButton.setSize(BUTTON_DIM);
        closeGameButton.setLocation(CLOSE_GAME_BUTTON_POS);
        dialog.getContentPane().add(closeGameButton, BorderLayout.SOUTH);

        String textForLabel = "You can start another new game!";
        JLabel label = new JLabel(textForLabel);
        label.setLocation(LABEL_POS);
        dialog.setLayout(new BorderLayout());
        dialog.add(label, BorderLayout.NORTH);

        dialog.setVisible(true);
    }

    private void paintSprites(Graphics g) {
        int x = STANDARD_X_FOR_SPRITES;
        int y = STANDARD_Y_FOR_SPRITES;

        for (Sprite sprite: sampleSprites) {
            sprite.draw(g, x, y);
            x += 2 * DELTA_X;
        }
        y += 2 * DELTA_Y;
        x = STANDARD_X_FOR_SPRITES;

        for (Sprite[] spritesRow: sprites) {
            for (Sprite sprite: spritesRow) {
                sprite.draw(g, x, y);
                x += DELTA_X;
            }
            x = STANDARD_X_FOR_SPRITES;
            y += DELTA_Y;
        }

        if (gripped) {
            int xDot = STANDARD_X_FOR_SPRITES + grippedColumn * DELTA_X + 40;
            int yDot = STANDARD_Y_FOR_SPRITES + 2 * DELTA_Y + grippedRow * DELTA_Y + 40;
            SPRITE_VARIES_UTIL[3].draw(g, xDot, yDot);
        }
    }

    private void fillSpriteVaries() {
        SPRITE_VARIES_UTIL = new Sprite[SPRITE_PATHS_UTIL.length];
        for (int i = 0; i < SPRITE_PATHS_UTIL.length; i++) {
            SPRITE_VARIES_UTIL[i] = Sprite.getSprite(SPRITE_PATHS_UTIL[i], SPRITE_PATHS_UTIL[i].split("[/.]")[1]);
        }

        SPRITE_VARIES_BLOCKS = new Sprite[SPRITE_PATHS_BLOCKS.length];
        for (int i = 0; i < SPRITE_PATHS_BLOCKS.length; i++) {
            SPRITE_VARIES_BLOCKS[i] = Sprite.getSprite(SPRITE_PATHS_BLOCKS[i], SPRITE_PATHS_BLOCKS[i].split("[/.]")[1]);
        }
    }

    private void generateSample() {
        int[] randomIndex = new int[SAMPLE_SIZE];
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            randomIndex[i] = random.nextInt(SPRITE_VARIES_BLOCKS.length);
            sampleSprites[i] = Sprite.getSprite(SPRITE_VARIES_BLOCKS[randomIndex[i]]);
        }
        for (int i = 1; i < SAMPLE_SIZE; i++) {
            if (randomIndex[0] != randomIndex[i]) {
                break;
            } else if (i == SAMPLE_SIZE - 1) {
                generateSample();
            }
        }
    }

    private List<Integer> getAvailableCells() {
        List<Integer> availableCells = new ArrayList<>();
        for (int i = 0; i < SPRITES_SIZE; i++) {
            for (int j = 0; j < SPRITES_SIZE; j++) {
                if (j != 1 && j != 3) {
                    availableCells.add(j + i * SPRITES_SIZE);
                }
            }
        }
        return availableCells;
    }

    private void fillSprites(List<Integer> availableCells) {
        List<Integer> copy = new ArrayList<>(availableCells);
        for (Sprite ss : sampleSprites) {
            for (int k = 0; k < SPRITES_SIZE; k++) {
                int randomIndex = copy.get(random.nextInt(copy.size()));
                int i = randomIndex / SPRITES_SIZE;
                int j = randomIndex % SPRITES_SIZE;
                sprites[i][j] = Sprite.getSprite(ss);
                copy.remove((Integer) randomIndex);
            }
        }
        if (isAssembled()) {
            fillSprites(availableCells);
        }
    }

    private boolean isBlockClicked() {
        return clickedName.equals("block");
    }

    private boolean isEmptyCell(int row, int column) {
        return sprites[row][column].getName().equals("empty");
    }

    private boolean isAvailableToSwap() {
        boolean isClickedNearGripped = (Math.abs(clickedRow - grippedRow + clickedColumn -  grippedColumn) == 1);
        return !isBlockClicked()
                && (isEmptyCell(clickedRow, clickedColumn) ^ isEmptyCell(grippedRow, grippedColumn))
                && isClickedNearGripped;
    }

    private class MouseInputHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point point = e.getPoint();
                if (point.x <= MAX_CELLS_POINT.x && point.y <= MAX_CELLS_POINT.y
                    && point.x >= MIN_CELLS_POINT.x && point.y >= MIN_CELLS_POINT.y) {
                    clickedColumn = (point.x != MAX_CELLS_POINT.x)
                                    ? (point.x - MIN_CELLS_POINT.x) / PIC_SIZE
                                    : SPRITES_SIZE - 1;
                    clickedRow = (point.y != MAX_CELLS_POINT.y)
                                    ? (point.y - MIN_CELLS_POINT.y) / PIC_SIZE
                                    : SPRITES_SIZE - 1;
                    clicked = true;
                }
            }
        }
    }
}
