package dev.matthiesen.cobble_poke_bank.common.database.dialect;

public final class SQLiteDialect implements IDatabaseDialect {

    @Override
    public String getInsertIgnore() {
        return "INSERT OR IGNORE";
    }

    @Override
    public String getOnConflictUpdate(String key, String update) {
        return "ON CONFLICT(" + key + ") DO UPDATE SET " + update;
    }

    @Override
    public String getOnConflictDoNothing(String key) {
        return "ON CONFLICT(" + key + ") DO NOTHING";
    }

    @Override
    public String getDataType(String type) {
        return switch (type) {
            case "integer", "bigint" -> "integer";
            case "text", "varchar" -> "text";
            default -> type;
        };
    }
}
