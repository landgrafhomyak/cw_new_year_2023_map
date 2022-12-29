package io.github.landgrafhomyak.chatwars.ny2023_map;

@SuppressWarnings("unused")
@Deprecated
public /* sealed */ class Building {
    public final Castle ownerCastle;
    public final String owner;

    public Building(Castle castle, String owner) {
        this.ownerCastle = castle;
        this.owner = owner;
    }

    static final class Snowman extends Building {
        public Snowman(Castle castle, String owner) {
            super(castle, owner);
        }
    }
    static final class Wall extends Building {
        public Wall(Castle castle, String owner) {
            super(castle, owner);
        }
    }
    static final class Ram extends Building {
        public Ram(Castle castle, String owner) {
            super(castle, owner);
        }
    }
    static final class Road extends Building {
        public Road(Castle castle, String owner) {
            super(castle, owner);
        }
    }
    static final class ChristmasTree extends Building {
        public ChristmasTree(Castle castle, String owner) {
            super(castle, owner);
        }
    }
}
