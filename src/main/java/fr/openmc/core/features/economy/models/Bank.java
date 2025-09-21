package fr.openmc.core.features.economy.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

import java.util.UUID;

@Getter
@DatabaseTable(tableName = "banks")
public class Bank {

    @DatabaseField(id = true)
    private UUID player;

    @DatabaseField(canBeNull = false, defaultValue = "0")
    private double balance;

    Bank() {
        // necessary for OrmLite
    }

    public Bank(UUID player) {
        this.player = player;
        this.balance = 0;
    }

    public void deposit(double amount) {
        balance += amount;
        assert balance >= 0;
    }

    public void withdraw(double amount) {
        balance -= amount;
        assert balance >= 0;
    }
}
