package fpsjframe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class FPSJFrame extends JPanel implements KeyListener, Runnable {
    private final int nScreenWidth = 800;
    private final int nScreenHeight = 600;
    private final int nMapWidth = 16;
    private final int nMapHeight = 16;
    private final float fFOV = (float) (Math.PI / 4.0);
    private final float fDepth = 16.0f;
    private final float fSpeed = 5.0f;

    private float fPlayerX = 1.5f; // Starting position
    private float fPlayerY = 1.5f;
    private float fPlayerA = 0.0f;
    private int health = 100; // Player's health

    private boolean[] keys = new boolean[4]; // W, A, S, D
    private String map;

    private enum GameState { STARTUP, IN_GAME, CONGRATS }
    private GameState gameState = GameState.STARTUP;

    // Ending position
    private final float fEndX = 14.5f;
    private final float fEndY = 14.5f;

    public FPSJFrame() {
        setPreferredSize(new Dimension(nScreenWidth, nScreenHeight));
        setFocusable(true);
        addKeyListener(this);

        // Create Map with Start (S) and End (E)
        map = "S.......#.......";
        map += "#...............";
        map += "#.......########";
        map += "#..............#";
        map += "#......##......#";
        map += "#......##......#";
        map += "#..............#";
        map += "###............#";
        map += "##.............#";
        map += "#......####..###";
        map += "#......#.......#";
        map += "#......#.......#";
        map += "#..............#";
        map += "#......#########";
        map += "#..............E";
        map += "################";
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState == GameState.STARTUP) {
            drawStartupScreen(g);
        } else if (gameState == GameState.IN_GAME) {
            drawGame(g);
            drawMap(g);
            drawHealthBar(g); // Draw the health bar
        } else if (gameState == GameState.CONGRATS) {
            drawCongratsScreen(g);
        }
    }

    private void drawHealthBar(Graphics g) {
        int barWidth = 200;
        int barHeight = 20;
        int healthWidth = (int) ((health / 100.0) * barWidth);

        g.setColor(Color.GRAY);
        g.fillRect(10, nScreenHeight - 30, barWidth, barHeight); // Background of the bar
        g.setColor(Color.RED);
        g.fillRect(10, nScreenHeight - 30, healthWidth, barHeight); // Current health level
        g.setColor(Color.BLACK);
        g.drawRect(10, nScreenHeight - 30, barWidth, barHeight); // Border of the health bar
    }

    private void drawStartupScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, nScreenWidth, nScreenHeight);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("FPS Game", nScreenWidth / 2 - 100, nScreenHeight / 2 - 50);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press ENTER to Start", nScreenWidth / 2 - 130, nScreenHeight / 2);
    }

    private void drawCongratsScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, nScreenWidth, nScreenHeight);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Congratulations!", nScreenWidth / 2 - 150, nScreenHeight / 2 - 50);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("You reached the end!", nScreenWidth / 2 - 120, nScreenHeight / 2);
        g.drawString("Press enter to exit", nScreenWidth / 2 - 100, nScreenHeight / 2 + 50);
    }

    private void drawGame(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, nScreenWidth, nScreenHeight);

        drawMap(g);

        for (int x = 0; x < nScreenWidth; x++) {
            float fRayAngle = (fPlayerA - fFOV / 2.0f) + ((float) x / nScreenWidth) * fFOV;
            float fStepSize = 0.1f;
            float fDistanceToWall = 0.0f;
            boolean bHitWall = false;
            float fEyeX = (float) Math.sin(fRayAngle);
            float fEyeY = (float) Math.cos(fRayAngle);

            while (!bHitWall && fDistanceToWall < fDepth) {
                fDistanceToWall += fStepSize;
                int nTestX = (int) (fPlayerX + fEyeX * fDistanceToWall);
                int nTestY = (int) (fPlayerY + fEyeY * fDistanceToWall);

                if (nTestX < 0 || nTestX >= nMapWidth || nTestY < 0 || nTestY >= nMapHeight) {
                    bHitWall = true;
                    fDistanceToWall = fDepth;
                } else if (map.charAt(nTestY * nMapWidth + nTestX) == '#') {
                    bHitWall = true;
                }
            }

            int nCeiling = (int) ((nScreenHeight / 2.0) - nScreenHeight / ((float) fDistanceToWall));
            int nFloor = nScreenHeight - nCeiling;

            for (int y = 0; y < nScreenHeight; y++) {
                if (y < nCeiling) {
                    g.setColor(Color.BLACK);
                } else if (y > nCeiling && y <= nFloor) {
                    g.setColor(Color.GRAY);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.drawLine(x, y, x, y);
            }
        }

        if (Math.abs(fPlayerX - fEndX) < 0.5 && Math.abs(fPlayerY - fEndY) < 0.5) {
            gameState = GameState.CONGRATS;
        }
    }

    private void drawMap(Graphics g) {
        int mapScale = 10; // Scale for the minimap

        for (int y = 0; y < nMapHeight; y++) {
            for (int x = 0; x < nMapWidth; x++) {
                if (map.charAt(y * nMapWidth + x) == '#') {
                    g.setColor(Color.DARK_GRAY); // Wall color
                } else {
                    g.setColor(Color.LIGHT_GRAY); // Open space color
                }
                g.fillRect(x * mapScale, y * mapScale, mapScale, mapScale);
            }
        }

        int playerX = (int) (fPlayerX * mapScale);
        int playerY = (int) (fPlayerY * mapScale);
        g.setColor(Color.RED); // Player color
        g.fillRect(playerX - 2, playerY - 2, 4, 4); // Small square to represent the player
    }

    public void updateGame(float fElapsedTime) {
        if (keys[0]) {
            float newPlayerX = fPlayerX + (float) Math.sin(fPlayerA) * fSpeed * fElapsedTime;
            float newPlayerY = fPlayerY + (float) Math.cos(fPlayerA) * fSpeed * fElapsedTime;
            if (map.charAt((int) (newPlayerY) * nMapWidth + (int) (newPlayerX)) != '#') {
                fPlayerX = newPlayerX;
                fPlayerY = newPlayerY;
            } else {
                health -= 10; // Reduce health on wall collision
                if (health <= 0) {
                    gameState = GameState.CONGRATS; // End game if health is zero
                }
            }
        }
        if (keys[1]) fPlayerA -= (fSpeed * 0.75f) * fElapsedTime;
        if (keys[2]) {
            float newPlayerX = fPlayerX - (float) Math.sin(fPlayerA) * fSpeed * fElapsedTime;
            float newPlayerY = fPlayerY - (float) Math.cos(fPlayerA) * fSpeed * fElapsedTime;
            if (map.charAt((int) (newPlayerY) * nMapWidth + (int) (newPlayerX)) != '#') {
                fPlayerX = newPlayerX;
                fPlayerY = newPlayerY;
            } else {
                health -= 10;
                if (health <= 0) {
                    gameState = GameState.CONGRATS;
                }
            }
        }
        if (keys[3]) fPlayerA += (fSpeed * 0.75f) * fElapsedTime;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (true) {
            long now = System.nanoTime();
            float elapsedTime = (now - lastTime) / 1e9f;
            lastTime = now;
            updateGame(elapsedTime);
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameState == GameState.STARTUP && e.getKeyCode() == KeyEvent.VK_ENTER) {
            gameState = GameState.IN_GAME;
        }
        if (e.getKeyCode() == KeyEvent.VK_W) keys[0] = true;
        if (e.getKeyCode() == KeyEvent.VK_A) keys[1] = true;
        if (e.getKeyCode() == KeyEvent.VK_S) keys[2] = true;
        if (e.getKeyCode() == KeyEvent.VK_D) keys[3] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) keys[0] = false;
        if (e.getKeyCode() == KeyEvent.VK_A) keys[1] = false;
        if (e.getKeyCode() == KeyEvent.VK_S) keys[2] = false;
        if (e.getKeyCode() == KeyEvent.VK_D) keys[3] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("First-Person Shooter Game with Map");
        FPSJFrame game = new FPSJFrame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        new Thread(game).start();
    }
}
