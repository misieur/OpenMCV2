package fr.openmc.core.disabled.corporation.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

import java.util.UUID;

@Getter
@DatabaseTable(tableName = "shops")
public class DBShop {
    @DatabaseField(id = true)
    private UUID id;

    @DatabaseField
    private UUID owner;
    @DatabaseField
    private UUID city;
    @DatabaseField
    private UUID company;

    @DatabaseField(canBeNull = false)
    private double x;
    @DatabaseField(canBeNull = false)
    private double y;
    @DatabaseField(canBeNull = false)
    private double z;

    DBShop() {
        // required for ORMLite
    }

    public DBShop(UUID id, UUID owner, UUID city, UUID company, double x, double y, double z) {
        this.id = id;
        this.owner = owner;
        this.city = city;
        this.company = company;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
