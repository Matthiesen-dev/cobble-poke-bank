package dev.matthiesen.cobble_poke_bank.common.database.queue;

public interface IQueue {
    void add(SqlTask task);
    void execute();
    void hello();
    boolean isEmpty();
}