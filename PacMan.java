import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.io.File;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int xVelocity = 0;
        int yVelocity = 0;
        int stationaryTimer = 0; // Add timer to track how long ghost stays in same area
        Point lastPosition = new Point(0, 0); // Track last position

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
            this.lastPosition = new Point(x, y);
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity(direction);
            this.x += this.xVelocity;
            this.y += this.yVelocity;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.xVelocity;
                    this.y -= this.yVelocity;
                    this.direction = prevDirection;
                    updateVelocity(direction);
                }
            }
        }

        void updateVelocity(char direction) {
            int baseSpeed = tileSize/4; // Base speed for normal movement
            
            if (this.image == pacmanUpImage || this.image == pacmanDownImage ||
                this.image == pacmanLeftImage || this.image == pacmanRightImage) {
                // Pacman speed remains constant
                baseSpeed = tileSize/4; // Reduced from tileSize/3 to tileSize/4 for normal speed
            } else {
                // For ghosts, apply the speed multiplier
                baseSpeed = (int)(tileSize/4 * ghostSpeedMultiplier);
                if (powerPelletActive) {
                    baseSpeed = (int)(baseSpeed * 0.6); // Slower when vulnerable
                }
            }

            switch(direction) {
                case 'U':
                    xVelocity = 0;
                    yVelocity = -baseSpeed;
                    break;
                case 'D':
                    xVelocity = 0;
                    yVelocity = baseSpeed;
                    break;
                case 'L':
                    xVelocity = -baseSpeed;
                    yVelocity = 0;
                    break;
                case 'R':
                    xVelocity = baseSpeed;
                    yVelocity = 0;
                    break;
                default:
                    xVelocity = 0;
                    yVelocity = 0;
                    break;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    private int currentLevel = 1;
    private double ghostSpeedMultiplier = 1.0;
    private boolean powerPelletActive = false;
    private int powerPelletTimer = 0;
    private int powerPelletDuration = 100;
    private int cherryTimer = 0;
    private int cherrySpawnInterval = 500; 
    private Block cherry = null;
    private Image cherryImage;
    private Image powerPelletImage;
    private Image scaredGhostImage;
    
    // Different maps for different levels
    private String[][] levelMaps = {
        // Level 1 - Original map
        {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X   X   X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
        },
        // Level 2 - More complex map
        {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XXXXX  X  XXXXX X",
            "X    X       X    X",
            "XXX  X XXXXX X  XXX",
            "X    X   X   X    X",
            "X XX XXX X XXX XX X",
            "X X  X   r   X  X X",
            "X X XXX bpo XXX X X",
            "X X  X       X  X X",
            "X XX X XXXXX X XX X",
            "X                 X",
            "X XXXXXXXXXXXXX  XX",
            "X        X        X",
            "XXXX XXX X XXX XXXX",
            "X  X     P     X  X",
            "X  X X XXXXX X X  X",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
        },
        // Level 3 - Most complex map
        {
            "XXXXXXXXXXXXXXXXXXX",
            "X   X    X    X   X",
            "X X X XX X XX X X X",
            "X X             X X",
            "X X XXX XXX XXX X X",
            "X     X  X  X     X",
            "XXXXX X XXX X XXXXX",
            "X   X X  r  X X   X",
            "X X X X bpo X X X X",
            "X X     XXX     X X",
            "X XXXXX     XXXXX X",
            "X     X XXX X     X",
            "XXXXX X     X XXXXX",
            "X     X XXX X     X",
            "X XXX XX X XX XXX X",
            "X   X    P    X   X",
            "X X XXXXXXXXXXX X X",
            "X X      X      X X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
        }
    };

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        setOpaque(true);
        setBounds(0, 0, boardWidth, boardHeight);
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        loadImages();
        loadMap();
        addKeyListener(this);
        setFocusable(true);
        gameLoop = new Timer(16, this); // Changed from 50ms to 16ms (approximately 60 FPS)
        gameLoop.start();
    }

    private Image loadAndVerifyImage(String path, String imageName) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException(imageName + " image not found at: " + path);
        }
        Image img = new ImageIcon(path).getImage();
        if (img.getWidth(null) <= 0 || img.getHeight(null) <= 0) {
            throw new RuntimeException(imageName + " image failed to load properly");
        }
        System.out.println(imageName + " loaded successfully");
        return img;
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        cherry = null;

        String[] currentMap = levelMaps[Math.min(currentLevel - 1, levelMaps.length - 1)];
        
        // Adjust ghost speed based on level
        switch (currentLevel) {
            case 1: // Normal speed
                ghostSpeedMultiplier = 0.5;
                break;
            case 2: // Normal speed
                ghostSpeedMultiplier = 0.6;
                break;
            case 3: // Slightly faster
                ghostSpeedMultiplier = 1.0;
                break;
            default: // Cap at level 3 speed
                ghostSpeedMultiplier = 1.0;
        }

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = currentMap[r];
                char mapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (mapChar == 'X') { // wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (mapChar == 'b') { // blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (mapChar == 'o') { // orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (mapChar == 'p') { // pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (mapChar == 'r') { // red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (mapChar == 'P') { // pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (mapChar == ' ') { // regular food
                    if (random.nextInt(20) == 0) { // 5% chance for power pellet
                        Block powerPellet = new Block(powerPelletImage, x, y, tileSize, tileSize);
                        foods.add(powerPellet);
                    } else {
                        Block food = new Block(null, x + 14, y + 14, 4, 4);
                        foods.add(food);
                    }
                }
            }
        }
    }

    public void draw(Graphics g) {
        // Draw power pellet timer
        if (powerPelletActive) {
            g.setColor(Color.BLUE);
            g.fillRect(10, boardHeight - 20, (int)((float)powerPelletTimer/powerPelletDuration * 100), 10);
        }

        // Draw cherry if active
        if (cherry != null) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }

        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            if (powerPelletActive) {
                g.drawImage(scaredGhostImage, ghost.x, ghost.y, ghost.width, ghost.height, null);
            } else {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        for (Block food : foods) {
            if (food.image != null) { // power pellet
                g.drawImage(food.image, food.x, food.y, food.width, food.height, null);
            } else { // regular food
                g.setColor(Color.WHITE);
                g.fillRect(food.x, food.y, food.width, food.height);
            }
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.setColor(Color.RED);
            String gameOverText = "Game Over! Level: " + currentLevel + " Score: " + score;
            String restartText = "Press ENTER to restart";
            String exitText = "Press ESC to exit";
            
            // Center the text
            int textX = (boardWidth - g.getFontMetrics().stringWidth(gameOverText)) / 2;
            g.drawString(gameOverText, textX, boardHeight / 2 - 20);
            
            textX = (boardWidth - g.getFontMetrics().stringWidth(restartText)) / 2;
            g.drawString(restartText, textX, boardHeight / 2 + 10);
            
            textX = (boardWidth - g.getFontMetrics().stringWidth(exitText)) / 2;
            g.drawString(exitText, textX, boardHeight / 2 + 40);
        } else {
            g.setColor(Color.WHITE);
            g.drawString("Level: " + currentLevel + " Lives: " + lives + " Score: " + score, tileSize/2, tileSize/2);
            
            // Draw small text for ESC instruction
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Press ESC to exit", boardWidth - 100, 20);
        }
    }

    private void updateGhostBehavior(Block ghost) {
        // Check if ghost has been in the same area
        Point currentPos = new Point(ghost.x, ghost.y);
        if (Math.abs(currentPos.x - ghost.lastPosition.x) < tileSize/2 && 
            Math.abs(currentPos.y - ghost.lastPosition.y) < tileSize/2) {
            ghost.stationaryTimer++;
        } else {
            ghost.stationaryTimer = 0;
            ghost.lastPosition = currentPos;
        }

        // Force direction change if ghost has been stationary too long
        boolean forceChange = ghost.stationaryTimer > 20; // Reduce stationary time threshold

        // Get available directions that don't lead to walls
        ArrayList<Character> validDirections = new ArrayList<>();
        
        // Don't allow immediate reversal of direction unless forced or no other choice
        char oppositeDirection = 'X';
        switch(ghost.direction) {
            case 'U': oppositeDirection = 'D'; break;
            case 'D': oppositeDirection = 'U'; break;
            case 'L': oppositeDirection = 'R'; break;
            case 'R': oppositeDirection = 'L'; break;
        }
        
        // Check each direction with a small offset to allow smoother transitions
        for (char direction : directions) {
            if (!forceChange && direction == oppositeDirection) continue;
            
            int testX = ghost.x;
            int testY = ghost.y;
            
            switch(direction) {
                case 'U': testY -= tileSize; break;
                case 'D': testY += tileSize; break;
                case 'L': testX -= tileSize; break;
                case 'R': testX += tileSize; break;
            }
            
            boolean hasWall = false;
            // Check for walls with more lenient collision detection
            for (Block wall : walls) {
                if (Math.abs(wall.x - testX) < tileSize*0.7 && Math.abs(wall.y - testY) < tileSize*0.7) {
                    hasWall = true;
                    break;
                }
            }
            
            if (!hasWall) {
                validDirections.add(direction);
            }
        }
        
        if (validDirections.isEmpty() && !forceChange) {
            validDirections.add(oppositeDirection);
        }
        
        if (!validDirections.isEmpty()) {
            // Encourage exploration by reducing chance to continue in same direction
            if (!forceChange && validDirections.contains(ghost.direction) && random.nextInt(100) < 50) {
                // 50% chance to continue in current direction if not forced to change
                ghost.updateDirection(ghost.direction);
            } else {
                // Choose random valid direction, but avoid the current area if stationary
                ArrayList<Character> preferredDirections = new ArrayList<>();
                for (char dir : validDirections) {
                    int newX = ghost.x;
                    int newY = ghost.y;
                    switch(dir) {
                        case 'U': newY -= tileSize; break;
                        case 'D': newY += tileSize; break;
                        case 'L': newX -= tileSize; break;
                        case 'R': newX += tileSize; break;
                    }
                    // Prefer directions that lead away from current position if stationary
                    if (ghost.stationaryTimer > 10 && 
                        (Math.abs(newX - ghost.lastPosition.x) > tileSize || 
                         Math.abs(newY - ghost.lastPosition.y) > tileSize)) {
                        // Add direction multiple times to increase its probability
                        for (int i = 0; i < 3; i++) {
                            preferredDirections.add(dir);
                        }
                    } else {
                        preferredDirections.add(dir);
                    }
                }
                
                // Choose random direction with weighted preference
                char newDirection = preferredDirections.get(random.nextInt(preferredDirections.size()));
                ghost.updateDirection(newDirection);
                ghost.stationaryTimer = 0; // Reset timer after direction change
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver) {
            char newDirection = 'X';
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                newDirection = 'U';
                pacman.image = pacmanUpImage;
            }
            else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                newDirection = 'D';
                pacman.image = pacmanDownImage;
            }
            else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                newDirection = 'L';
                pacman.image = pacmanLeftImage;
            }
            else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                newDirection = 'R';
                pacman.image = pacmanRightImage;
            }
            
            if (newDirection != 'X') {
                // Check if the new direction is valid before updating
                int testX = pacman.x;
                int testY = pacman.y;
                switch(newDirection) {
                    case 'U': testY -= tileSize/2; break;
                    case 'D': testY += tileSize/2; break;
                    case 'L': testX -= tileSize/2; break;
                    case 'R': testX += tileSize/2; break;
                }
                
                boolean hasWall = false;
                for (Block wall : walls) {
                    if (Math.abs(wall.x - testX) < tileSize*0.7 && 
                        Math.abs(wall.y - testY) < tileSize*0.7) {
                        hasWall = true;
                        break;
                    }
                }
                
                if (!hasWall) {
                    pacman.updateDirection(newDirection);
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0); // Close game when ESC is pressed
        }
        
        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
    }

    private void loadImages() {
        try {
            //load images using absolute paths with Windows separators
            String basePath = System.getProperty("user.dir");
            String separator = System.getProperty("file.separator");
            
            // Load and verify each image
            wallImage = loadAndVerifyImage(basePath + separator + "wall.png", "Wall");
            blueGhostImage = loadAndVerifyImage(basePath + separator + "blueGhost.png", "Blue Ghost");
            orangeGhostImage = loadAndVerifyImage(basePath + separator + "orangeGhost.png", "Orange Ghost");
            pinkGhostImage = loadAndVerifyImage(basePath + separator + "pinkGhost.png", "Pink Ghost");
            redGhostImage = loadAndVerifyImage(basePath + separator + "redGhost.png", "Red Ghost");
            scaredGhostImage = loadAndVerifyImage(basePath + separator + "scaredGhost.png", "Scared Ghost");
            powerPelletImage = loadAndVerifyImage(basePath + separator + "powerFood.png", "Power Pellet");
            cherryImage = loadAndVerifyImage(basePath + separator + "cherry.png", "Cherry");

            pacmanUpImage = loadAndVerifyImage(basePath + separator + "pacmanUp.png", "Pacman Up");
            pacmanDownImage = loadAndVerifyImage(basePath + separator + "pacmanDown.png", "Pacman Down");
            pacmanLeftImage = loadAndVerifyImage(basePath + separator + "pacmanLeft.png", "Pacman Left");
            pacmanRightImage = loadAndVerifyImage(basePath + separator + "pacmanRight.png", "Pacman Right");

            System.out.println("All images loaded successfully from: " + basePath);
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Exit if images can't be loaded
        }

        // Initialize ghost directions
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.fillRect(0, 0, boardWidth, boardHeight);

        // Draw walls
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // Draw food and power pellets
        g.setColor(Color.yellow);
        for (Block food : foods) {
            if (food.image != null) { // power pellet
                g.drawImage(food.image, food.x, food.y, food.width, food.height, null);
            } else { // regular food
                g.fillOval(food.x, food.y, food.width, food.height);
            }
        }

        // Draw cherry if active
        if (cherry != null) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }

        // Draw ghosts
        for (Block ghost : ghosts) {
            if (powerPelletActive) {
                g.drawImage(scaredGhostImage, ghost.x, ghost.y, ghost.width, ghost.height, null);
            } else {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }
        }

        // Draw pacman
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        // Draw score
        g.setColor(Color.yellow);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);

        // Draw lives
        g.drawString("Lives: " + lives, boardWidth - 100, 30);

        // Draw level
        g.drawString("Level: " + currentLevel, boardWidth/2 - 40, 30);

        // Draw game over
        if (gameOver) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over", boardWidth/2 - 100, boardHeight/2);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Press Enter to Restart", boardWidth/2 - 100, boardHeight/2 + 40);
        }

        System.out.println("Game board painted");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac Man");
        PacMan pacMan = new PacMan();
        frame.add(pacMan);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void move() {
        // Update power pellet timer
        if (powerPelletActive) {
            powerPelletTimer--;
            if (powerPelletTimer <= 0) {
                powerPelletActive = false;
            }
        }

        // Update cherry timer and spawn cherry
        cherryTimer++;
        if (cherryTimer >= cherrySpawnInterval && cherry == null) {
            ArrayList<Point> emptySpots = new ArrayList<>();
            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < columnCount; c++) {
                    int x = c * tileSize;
                    int y = r * tileSize;
                    Point spot = new Point(x, y);
                    boolean isEmpty = true;
                    for (Block wall : walls) {
                        if (wall.x == x && wall.y == y) {
                            isEmpty = false;
                            break;
                        }
                    }
                    if (isEmpty) {
                        emptySpots.add(spot);
                    }
                }
            }
            if (!emptySpots.isEmpty()) {
                Point spot = emptySpots.get(random.nextInt(emptySpots.size()));
                cherry = new Block(cherryImage, spot.x, spot.y, tileSize, tileSize);
            }
            cherryTimer = 0;
        }

        pacman.x += pacman.xVelocity;
        pacman.y += pacman.yVelocity;

        // Handle wrap-around for left and right sides
        if (pacman.x + pacman.width <= 0) {
            pacman.x = boardWidth - pacman.width;
        } else if (pacman.x >= boardWidth) {
            pacman.x = 0;
        }

        //check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.xVelocity;
                pacman.y -= pacman.yVelocity;
                break;
            }
        }

        //check ghost collisions and movement
        for (Block ghost : ghosts) {
            char prevDirection = ghost.direction; // Store previous direction
            
            if (collision(ghost, pacman)) {
                if (powerPelletActive) {
                    ghost.reset(); // Ghost is eaten
                    score += 200;
                } else {
                    lives -= 1;
                    if (lives == 0) {
                        gameOver = true;
                        return;
                    }
                    resetPositions();
                }
            }

            // Update ghost behavior
            updateGhostBehavior(ghost);

            // Apply ghost speed multiplier
            ghost.xVelocity *= ghostSpeedMultiplier;
            ghost.yVelocity *= ghostSpeedMultiplier;

            ghost.x += ghost.xVelocity;
            ghost.y += ghost.yVelocity;

            // Handle ghost wrap-around
            if (ghost.x + ghost.width <= 0) {
                ghost.x = boardWidth - ghost.width;
            } else if (ghost.x >= boardWidth) {
                ghost.x = 0;
            }

            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    // Check for collision with walls
                    ghost.x -= ghost.xVelocity;
                    ghost.y -= ghost.yVelocity;
                    ghost.direction = prevDirection;
                    ghost.updateVelocity(ghost.direction);
                    
                    // Try moving in a different direction
                    boolean moved = false;
                    for (char newDirection : directions) {
                        if (newDirection != prevDirection) {
                            ghost.direction = newDirection;
                            ghost.updateVelocity(ghost.direction);
                            ghost.x += ghost.xVelocity;
                            ghost.y += ghost.yVelocity;
                            boolean collision = false;
                            // Create a new variable name for the inner loop
                            for (Block wallCheck : walls) {
                                if (collision(ghost, wallCheck)) {
                                    collision = true;
                                    ghost.x -= ghost.xVelocity;
                                    ghost.y -= ghost.yVelocity;
                                    break;
                                }
                            }
                            if (!collision) {
                                moved = true;
                                break;
                            }
                        }
                    }
                    
                    // If ghost can't move in any direction, stop it
                    if (!moved) {
                        ghost.xVelocity /= ghostSpeedMultiplier;
                        ghost.yVelocity /= ghostSpeedMultiplier;
                    }
                }
            }

            // Reset ghost velocity after movement
            ghost.xVelocity /= ghostSpeedMultiplier;
            ghost.yVelocity /= ghostSpeedMultiplier;
        }

        // Check food and power pellet collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                if (food.image != null) { // power pellet
                    powerPelletActive = true;
                    powerPelletTimer = powerPelletDuration;
                    score += 50;
                } else { // regular food
                    score += 10;
                }
            }
        }
        if (foodEaten != null) {
            foods.remove(foodEaten);
        }

        // Check cherry collision
        if (cherry != null && collision(pacman, cherry)) {
            score += 100;
            cherry = null;
            cherryTimer = 0;
        }

        // Level complete
        if (foods.isEmpty()) {
            currentLevel++;
            loadMap();
            resetPositions();
            powerPelletActive = false;
            powerPelletTimer = 0;
        }
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.xVelocity = 0;
        pacman.yVelocity = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
