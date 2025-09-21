package fr.openmc.core.disabled.corporation.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

import java.util.UUID;

@DatabaseTable(tableName = "company_permissions")
public class CompanyPermission {
    @DatabaseField(canBeNull = false)
    private UUID company;
    @DatabaseField(canBeNull = false)
    private UUID player;
    @Getter
    @DatabaseField(canBeNull = false)
    private String permission;

    CompanyPermission() {
        // required for ORMLite
    }

    public CompanyPermission(UUID company, UUID player, String permission) {
        this.company = company;
        this.player = player;
        this.permission = permission;
    }
}
