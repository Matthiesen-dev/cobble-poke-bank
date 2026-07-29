package dev.matthiesen.cobble_poke_bank.common.database.dialect;

public interface IDatabaseDialect {
    String getInsertIgnore();
    String getOnConflictUpdate(String key, String update);
    String getOnConflictDoNothing(String key);
    String getDataType(String type);
}
