package io.github.landgrafhomyak.chatwars.ny2023_map;

public enum TileType {
    VALLEY('V', "valley"),
    FOREST('F', "forest"),
    FIELDS('f', "fields"),
    TAVERN('t', "tavern"),
    ZERO('z', "zero"),
    WELL('w', "well"),
    SHOP('s', "shop");
    public final char serial;
    public final String cssClass;

    TileType(char serial, String cssClass) {
        this.serial = serial;
        this.cssClass = cssClass;
    }

    public static TileType fromSerial(char serial) {
        if (serial < 's') {
            if (serial == 'F') return TileType.FOREST;
            if (serial == 'V') return TileType.VALLEY;
            if (serial == 'f') return TileType.FIELDS;
        } else if (serial > 's') {
            if (serial == 't') return TileType.TAVERN;
            if (serial == 'w') return TileType.WELL;
            if (serial == 'z') return TileType.ZERO;
        } else {
            return TileType.SHOP;
        }
        return null;
    }

    public static TileType fromEmoji(String source) {
        if (source.length() < 1)
            return null;

        char c;
        if ((c = source.charAt(0)) == '\uD83C') {
            if (source.length() < 2)
                return null;
            if ((c = source.charAt(1)) == '\uDFD4')
                return TileType.VALLEY;
            else if (c == '\uDF3B')
                return TileType.FIELDS;
            else if (c == '\uDF32')
                return TileType.FOREST;
        } else if (c == '\uD83D') {
            if (source.length() < 2)
                return null;
            if ((c = source.charAt(1)) == '\uDED6')
                return TileType.TAVERN;
            else if (c == '\uDD73')
                return TileType.WELL;
        } else if (c == '\uD83E') {
            if (source.length() < 2)
                return null;
            if (source.charAt(1) == '\uDDFF')
                return TileType.ZERO;
        } else if (c == '\u26FA') {
            return TileType.SHOP;
        }
        return null;
    }

    public static char serial(TileType self) {
        if (self == null) return '\0';
        return self.serial;
    }
}
