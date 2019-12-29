package gdx.clue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import static gdx.clue.Card.*;
import static gdx.clue.ClueMain.TILE_DIM;
import static gdx.clue.ClueMain.VIEWPORT_DIM_HEIGHT;
import static gdx.clue.Player.*;
import gdx.clue.astar.AStar;
import gdx.clue.astar.Location;
import gdx.clue.astar.PathFinder;
import java.util.List;

public class GameScreen implements Screen, InputProcessor {

    private final Stage stage;
    private final Batch batch;
    private final Viewport viewport = new ScreenViewport();
    private InputMultiplexer input;
    private Clue game;
    private ClueMap map;
    private PathFinder<Location> pathfinder;
    private List<Player> players;
    private Player currentTurnPlayer;
    private Player yourPlayer;
    private Turn turn;

    private final Vector3 screenPos = new Vector3();
    private final Vector3 gridPos = new Vector3();

    private final RoomIconPlacement playerIconPlacement;
    private TextureRegion rolledDiceImageLeft = null;
    private TextureRegion rolledDiceImageRight = null;
    private final NotebookPanel notebookPanel = new NotebookPanel();
    private final MainPanel mainPanel;
    private final MessagePanel messagePanel = new MessagePanel();
    private final ShowCardsRoutine showCards;

    public GameScreen() {
        game = new Clue();
        map = new ClueMap();
        pathfinder = new AStar<>();
        stage = new Stage(viewport);
        batch = new SpriteBatch();
        input = new InputMultiplexer(this, stage);
        playerIconPlacement = new RoomIconPlacement();
        mainPanel = new MainPanel(stage, this);
        turn = new Turn(this);
        showCards = new ShowCardsRoutine(this);
    }

    public Clue getGame() {
        return this.game;
    }

    public ClueMap getMap() {
        return this.map;
    }

    public Stage getStage() {
        return this.stage;
    }

    public PathFinder<Location> getPathfinder() {
        return this.pathfinder;
    }

    public ShowCardsRoutine getShowCards() {
        return showCards;
    }

    public Player getYourPlayer() {
        return yourPlayer;
    }

    public RoomIconPlacement getPlayerIconPlacement() {
        return playerIconPlacement;
    }

    private void setGridCoordinates() {
        gridPos.set(
                Math.round(((screenPos.x - TILE_DIM * 8) / TILE_DIM) - 0.5f),
                Math.round(((screenPos.y - TILE_DIM) / TILE_DIM) + 0.5f),
                0);
    }

    public int rollDice() {

        int roll1 = ClueMain.DICE.roll();
        int roll2 = ClueMain.DICE.roll();
        Sounds.play(Sound.DICE);

        rolledDiceImageLeft = ClueMain.DICE_TEXTURES[0][roll1 - 1];
        rolledDiceImageRight = ClueMain.DICE_TEXTURES[0][roll2 - 1];

        return roll1 + roll2;

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        batch.draw(ClueMain.ROOMS, TILE_DIM * 8, 0);

        for (int i = 0; i < map.getXSize(); i++) {
            for (int j = 0; j < map.getYSize(); j++) {
                Location t = map.getLocation(i, j);
                if (t.getBlocked()) {
                    //nothing
                } else if (t.getHighlighted()) {
                    batch.draw(ClueMain.TILE_DARK_GREEN, TILE_DIM * 8 + i * TILE_DIM, VIEWPORT_DIM_HEIGHT - j * TILE_DIM - TILE_DIM);
                } else if (t.isRoom()) {
                    batch.draw(ClueMain.TILE_BROWN, TILE_DIM * 8 + i * TILE_DIM, VIEWPORT_DIM_HEIGHT - j * TILE_DIM - TILE_DIM);
                } else {
                    batch.draw(ClueMain.TILE_LIGHT_GRAY, TILE_DIM * 8 + i * TILE_DIM, VIEWPORT_DIM_HEIGHT - j * TILE_DIM - TILE_DIM);
                }
            }
        }

        if (rolledDiceImageLeft != null) {
            batch.draw(rolledDiceImageLeft, TILE_DIM * 8 + 350, VIEWPORT_DIM_HEIGHT - 468);
        }
        if (rolledDiceImageRight != null) {
            batch.draw(rolledDiceImageRight, TILE_DIM * 8 + 403, VIEWPORT_DIM_HEIGHT - 468);
        }

        drawPlayerLocation(batch, SUSPECT_SCARLET);
        drawPlayerLocation(batch, SUSPECT_MUSTARD);
        drawPlayerLocation(batch, SUSPECT_GREEN);
        drawPlayerLocation(batch, SUSPECT_PLUM);
        drawPlayerLocation(batch, SUSPECT_WHITE);
        drawPlayerLocation(batch, SUSPECT_PEACOCK);

        playerIconPlacement.drawIcons(batch);

        drawRoomLabel(batch, "Kitchen", 50, 75);
        drawRoomLabel(batch, "Ballroom", 323, 128);
        drawRoomLabel(batch, "Conservatory", 610, 75);
        drawRoomLabel(batch, "Dining Room", 50, 400);
        drawRoomLabel(batch, "Billiard Room", 621, 335);
        drawRoomLabel(batch, "Library", 610, 529);
        drawRoomLabel(batch, "Study", 610, 712);
        drawRoomLabel(batch, "Hall", 370, 712);
        drawRoomLabel(batch, "Lounge", 100, 712);

        ClueMain.FONT_48.draw(batch, "Clue", TILE_DIM * 8 + 360, VIEWPORT_DIM_HEIGHT - 423);
        //ClueMain.FONT_14.draw(batch, String.format("%s, %s\n", gridPos.x, gridPos.y), 20, 20);

        this.messagePanel.render(batch);

        batch.end();

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenPos.x = screenX;
        screenPos.y = screenY;
        setGridCoordinates();
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    private void drawRoomLabel(Batch batch, String id, int x, int y) {
        ClueMain.FONT_24.draw(batch, id, TILE_DIM * 8 + x, VIEWPORT_DIM_HEIGHT - y);
    }

    private Color getRoomTextColor(int room_id) {

        Color textColor = Color.YELLOW;

        for (Location location : map.getLocations()) {
            if (!location.isRoom()) {
                continue;
            }
            if (location.getHighlighted() && location.getRoomId() == room_id) {
                return Color.FOREST;
            }
        }

        return textColor;
    }

    private Location getPlayerLocation(int id) {
        Location location = null;
        if (players == null) {
            return null;
        }
        Player player = game.getPlayer(id);
        if (player != null) {
            location = player.getLocation();
        }
        return location;
    }

    private void drawPlayerLocation(Batch batch, int id) {
        Location location = getPlayerLocation(id);
        if (location == null) {
            return;
        }
        if (currentTurnPlayer.getSuspectNumber() == id) {
            batch.draw(ClueMain.CIRCLES.get(Color.PINK), location.getX(), location.getY());
        }
        batch.draw(ClueMain.CIRCLES.get(location.getColor()), location.getX(), location.getY());
    }

    public void setCurrentPlayer(Player player) {
        currentTurnPlayer = player;
    }

    public void startGame() {

        try {
            game.createDeck();
            String msg = game.dealShuffledDeck();
            System.out.println(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        players = game.getPlayers();

        Location scarlet_location = map.getLocationAndSetColor(7, 24, COLOR_SCARLET);
        Location mustard_location = map.getLocationAndSetColor(0, 17, COLOR_MUSTARD);
        Location green_location = map.getLocationAndSetColor(13, 0, COLOR_GREEN);
        Location plum_location = map.getLocationAndSetColor(23, 18, COLOR_PLUM);
        Location white_location = map.getLocationAndSetColor(9, 0, COLOR_WHITE);
        Location peacock_location = map.getLocationAndSetColor(23, 5, COLOR_PEACOCK);

        // set locations for the current players
        for (Player player : players) {

            Notebook book = new Notebook(player);
            player.setNotebook(book);

            if (!player.isComputerPlayer()) {
                setCurrentPlayer(player);
                yourPlayer = player;
                notebookPanel.setNotebook(book, this.stage);
            }

            switch (player.getSuspectNumber()) {
                case SUSPECT_SCARLET:
                    player.setLocation(scarlet_location);
                    break;
                case SUSPECT_MUSTARD:
                    player.setLocation(mustard_location);
                    break;
                case SUSPECT_GREEN:
                    player.setLocation(green_location);
                    break;
                case SUSPECT_PLUM:
                    player.setLocation(plum_location);
                    break;
                case SUSPECT_WHITE:
                    player.setLocation(white_location);
                    break;
                case SUSPECT_PEACOCK:
                    player.setLocation(peacock_location);
                    break;
            }
        }

        turn.mainLoop(players);

    }

    public void setPlayerLocationFromMapClick(Location location) {
        setPlayerLocationFromMapClick(currentTurnPlayer, currentTurnPlayer.getPlayerColor(), currentTurnPlayer.getLocation(), location);
    }

    public void setPlayerLocationFromMapClick(Player player, Color color, Location from_location, Location to_location) {

        playerIconPlacement.removePlayerIcon(player.getSuspectNumber());
        playerIconPlacement.addPlayerIcon(to_location.getRoomId(), player.getSuspectNumber());

        //reset original color back to gray
        map.setLocationColor(from_location, Color.GRAY);
        //set the players location 
        player.setLocation(to_location);
        //set location color to the players color
        to_location.setColor(color);
    }

    public void addMessage(String text) {
        this.messagePanel.add(text, Color.WHITE);
    }

}
