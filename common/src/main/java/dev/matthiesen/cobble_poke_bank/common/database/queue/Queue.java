package dev.matthiesen.cobble_poke_bank.common.database.queue;

import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import dev.matthiesen.cobble_poke_bank.common.database.Database;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Queue implements IQueue {

    private final Database database;
    private final boolean isBatch;
    private final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<>();

    public Queue(Database database, boolean isBatch) {
        this.database = database;
        this.isBatch = isBatch;
    }

    @Override
    public void add(SqlTask task) {
        this.queue.add(task);
    }

    @Override
    public void execute() {
        if (this.queue.isEmpty()) {
            return;
        }
        List<Object> items = new ArrayList<>();
        Object item;
        while ((item = this.queue.poll()) != null) {
            items.add(item);
        }
        this.database.executeQueue(items, isBatch);
    }

    @Override
    public void hello() {
        this.add(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
                statement.execute();
            } catch (Exception e) {
                CobblePokeBankCommon.INSTANCE.createErrorLog("Failed to send hello packet", e);
            }
        });
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }
}
