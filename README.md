# Pac-Man Game

A Java implementation of the classic Pac-Man arcade game with modern features and improvements.

## Features

- Classic Pac-Man gameplay with ghosts, power pellets, and cherries
- Multiple levels with increasing difficulty
- Ghost AI with improved movement patterns
- Power-up system with temporary ghost vulnerability
- Score tracking and lives system
- Smooth character movement and collision detection
- Wrap-around board edges
- Cherry bonus items

## Recent Updates

- Improved ghost movement behavior for more dynamic gameplay
- Adjusted ghost speeds per level (0.5x for level 1, 0.6x for level 2, 1.0x for level 3)
- Enhanced collision detection system
- Added stationary timer to prevent ghosts from staying in one area
- Implemented weighted direction preferences for better ghost exploration
- Optimized Pac-Man's movement responsiveness

## Controls

- Arrow keys to move Pac-Man
- ESC to exit the game
- ENTER to restart after game over

## Setup

1. Ensure you have Java installed on your system
2. Compile the game: `javac PacMan.java`
3. Run the game: `java PacMan`

## Game Elements

- Regular dots: 10 points
- Power pellets: 50 points + ghost vulnerability
- Cherries: 100 points
- Eating vulnerable ghosts: 200 points
- Three lives to start
- Advancing levels increases ghost speed and maze complexity