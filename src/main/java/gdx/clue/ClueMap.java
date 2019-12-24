package gdx.clue;

import com.badlogic.gdx.graphics.Color;
import gdx.clue.astar.Location;
import gdx.clue.astar.PathFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class ClueMap {

    private final Location[][] nodes;
    private int hbound = 0;
    private int vbound = 0;

    private static final String TEMPLATE = "xxxxxxxxxoxxxoxxxxxxxxxx\n"
            + "xxxxxxxoooxxxoooxxxxxxxx\n"
            + "xxxxxxooxxxxxxxooxxxxxxx\n"
            + "xxxxxxooxxxxxxxooxxxxxxx\n"
            + "xxxxxxooxxxxxxxooo5xxxxx\n"
            + "xxxxxxoo4xxxxx4ooooooooo\n"
            + "xxxx3xooxxxxxxxoooooooox\n"
            + "oooooooox4xxx4xoooxxxxxx\n"
            + "xooooooooooooooooo6xxxxx\n"
            + "xxxxxoooooooooooooxxxxxx\n"
            + "xxxxxxxxooxxxxxoooxxxxxx\n"
            + "xxxxxxxxooxxxxxoooxxxx6x\n"
            + "xxxxxxx2ooxxxxxoooooooox\n"
            + "xxxxxxxxooxxxxxoooxx8xxx\n"
            + "xxxxxxxxooxxxxxooxxxxxxx\n"
            + "xxxxxx2xooxxxxxoo8xxxxxx\n"
            + "xoooooooooxxxxxooxxxxxxx\n"
            + "ooooooooooooooooooxxxxxx\n"
            + "xooooooooxx00xxooooooooo\n"
            + "xxxxxx1ooxxxxxxoooooooox\n"
            + "xxxxxxxooxxxxx0oo7xxxxxx\n"
            + "xxxxxxxooxxxxxxooxxxxxxx\n"
            + "xxxxxxxooxxxxxxooxxxxxxx\n"
            + "xxxxxxxooxxxxxxooxxxxxxx\n"
            + "xxxxxxxoxxxxxxxxoxxxxxxx";

    public ClueMap() {

        StringTokenizer tokens = new StringTokenizer(TEMPLATE, "\n");
        vbound = tokens.countTokens();

        String line = tokens.nextToken();
        if (hbound == 0) {
            hbound = line.length();
        }

        nodes = new Location[hbound][vbound];
        for (int i = 0; i < hbound; i++) {
            for (int j = 0; j < vbound; j++) {
                nodes[i][j] = new Location(i, j);
                nodes[i][j].setHeight(100);
            }
        }

        tokens = new StringTokenizer(TEMPLATE, "\n");
        int y = 0;

        while (tokens.hasMoreTokens()) {
            line = tokens.nextToken();
            char[] array = line.toCharArray();
            for (int x = 0; x < array.length; x++) {
                Location t = nodes[x][y];
                if (array[x] == 'x') {
                    t.setBlocked(true);
                }
                try {
                    int room_id = Integer.parseInt(String.valueOf(array[x]));
                    t.setIsRoom(true);
                    t.setRoomId(room_id);
                } catch (Exception e) {
                }
            }
            y++;
        }

        attachNeighbors();

        //fix 2 doors which need neighbor removals
        nodes[18][5].removeNeighbor(nodes[18][6]);
        nodes[18][6].removeNeighbor(nodes[18][5]);

        nodes[17][21].removeNeighbor(nodes[16][21]);
        nodes[16][21].removeNeighbor(nodes[17][21]);

    }

    public Location getLocation(int x, int y) {
        return nodes[x][y];
    }

    public Location getLocationAndSetColor(int x, int y, Color color) {
        nodes[x][y].setColor(color);
        return nodes[x][y];
    }

    public void setLocationColor(Location location, Color color) {
        nodes[location.getX()][location.getY()].setColor(color);
    }

    public int getXSize() {
        return nodes.length;
    }

    public int getYSize() {
        return nodes[0].length;
    }

    public Collection<Location> getLocations() {
        Collection<Location> locations = new ArrayList<>();
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[0].length; j++) {
                locations.add(nodes[i][j]);
            }
        }
        return locations;
    }

    public Location getRoomLocation(int room_id) {
        Location room = null;
        Collection<Location> locations = new ArrayList<>();
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[0].length; j++) {
                if (nodes[i][j].getRoomId() == room_id) {
                    room = nodes[i][j];
                    break;
                }
            }
        }
        return room;
    }

    public ArrayList<Location> getAllRoomLocations(int exclusion) {
        ArrayList<Location> rooms = new ArrayList<>(9);
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[0].length; j++) {
                if (nodes[i][j].getRoomId() == exclusion) {
                    continue;
                }
                if (nodes[i][j].getRoomId() != -1) {
                    rooms.add(nodes[i][j]);
                }
            }
        }
        return rooms;
    }

    public ArrayList<Location> getAllDoorLocationsForRoom(int roomId) {
        if (roomId == -1) {
            return null;
        }
        ArrayList<Location> doors = new ArrayList<>();
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[0].length; j++) {
                if (nodes[i][j].getRoomId() == roomId) {;
                    doors.add(nodes[i][j]);
                }
            }
        }
        return doors;
    }

    public ArrayList<Location> highlightReachablePaths(Location starting_location, PathFinder<Location> pathfinder, int dice_roll) {

        Collection<Location> locs = getLocations();

        ArrayList<Location> choices = new ArrayList<>();

        ArrayList<Location> doors = getAllDoorLocationsForRoom(starting_location.getRoomId());

        if (doors == null) {
            for (Location loc : locs) {
                List<Location> path2 = pathfinder.findPath(locs, starting_location, Collections.singleton(loc));
                if (path2 != null && path2.size() == dice_roll + 1) {
                    Location l = path2.get(path2.size() - 1);
                    l.setHighlighted(true);
                    choices.add(l);
                }
                if (path2 != null && loc.isRoom() && path2.size() < dice_roll + 1) {
                    Location l = path2.get(path2.size() - 1);
                    l.setHighlighted(true);
                    choices.add(l);
                }
            }
        } else {
            for (Location door : doors) {
                for (Location loc : locs) {
                    List<Location> path2 = pathfinder.findPath(locs, door, Collections.singleton(loc));
                    if (path2 != null && path2.size() == dice_roll + 1) {
                        Location l = path2.get(path2.size() - 1);
                        l.setHighlighted(true);
                        choices.add(l);
                    }
                    if (path2 != null && loc.isRoom() && path2.size() < dice_roll + 1) {
                        Location l = path2.get(path2.size() - 1);
                        l.setHighlighted(true);
                        choices.add(l);
                    }
                }
            }
        }

        return choices;

    }

    public void resetHighlights() {
        for (Location loc : getLocations()) {
            loc.setHighlighted(false);
        }
    }

    private void attachNeighbors() {
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[0].length; j++) {
                Location t = nodes[i][j];

                if (i != 0) {
                    t.addNeighbor(nodes[i - 1][j]);
                }

                if (i != nodes.length - 1) {
                    t.addNeighbor(nodes[i + 1][j]);
                }

                if (j != 0) {
                    t.addNeighbor(nodes[i][j - 1]);
                }

                if (j != nodes[0].length - 1) {
                    t.addNeighbor(nodes[i][j + 1]);
                }
            }

        }
    }

}
