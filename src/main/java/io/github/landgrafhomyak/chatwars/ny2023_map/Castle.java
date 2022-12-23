package io.github.landgrafhomyak.chatwars.ny2023_map;

public enum Castle {
    BLUE("\uD83C\uDDEA\uD83C\uDDFA"),
    BLACK("\uD83C\uDDEC\uD83C\uDDF5"),
    RED("\uD83C\uDDEE\uD83C\uDDF2"),
    WHITE("\uD83C\uDDE8\uD83C\uDDFE");

    public final String emoji;

    Castle(String emoji) {
        this.emoji = emoji;
    }
}
