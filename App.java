import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                int rowCount = 21;
                int columnCount = 19;
                int tileSize = 32;
                int boardWidth = columnCount * tileSize;
                int boardHeight = rowCount * tileSize;

                JFrame frame = new JFrame("Pac Man");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setUndecorated(true); // Remove window decorations
                
                PacMan pacmanGame = new PacMan();
                frame.add(pacmanGame);
                
                frame.setResizable(false);
                frame.pack(); // Pack after adding component
                frame.setLocationRelativeTo(null); // Center on screen
                
                // Verify window size
                Dimension actualSize = frame.getSize();
                System.out.println("Window size: " + actualSize.width + "x" + actualSize.height);
                System.out.println("Expected size: " + boardWidth + "x" + boardHeight);
                
                pacmanGame.requestFocus();
                frame.setVisible(true);

                System.out.println("Game window created successfully");
            } catch (Exception e) {
                System.err.println("Error creating game window: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
