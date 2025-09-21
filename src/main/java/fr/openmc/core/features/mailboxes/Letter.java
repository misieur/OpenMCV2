package fr.openmc.core.features.mailboxes;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import fr.openmc.core.features.mailboxes.letter.LetterHead;
import fr.openmc.core.features.mailboxes.letter.SenderLetter;
import fr.openmc.core.utils.CacheOfflinePlayer;
import fr.openmc.core.utils.serializer.BukkitSerializer;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Getter
@DatabaseTable(tableName = "mail")
public class Letter {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false)
    private UUID sender;
    @DatabaseField(canBeNull = false)
    private UUID receiver;
    @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
    private byte[] items;
    @DatabaseField(columnName = "num_items", canBeNull = false)
    private int numItems;
    @DatabaseField(canBeNull = false)
    private Timestamp sent;
    @DatabaseField
    private boolean refused;

    Letter() {
        // required by ORMLite
    }

    Letter(UUID sender, UUID receiver, byte[] items, int numItems, Timestamp sent, boolean refused) {
        this.sender = sender;
        this.receiver = receiver;
        this.items = items;
        this.numItems = numItems;
        this.refused = refused;
        this.sent = sent;
    }

    public boolean refuse() {
        refused = true;
        return MailboxManager.saveLetter(this);
    }

    public LetterHead toLetterHead() {
        OfflinePlayer player = CacheOfflinePlayer.getOfflinePlayer(sender);
        ItemStack[] items = BukkitSerializer.deserializeItemStacks(this.items);
        return new LetterHead(player, id, numItems,
                LocalDateTime.ofInstant(sent.toInstant(), ZoneId.systemDefault()), items);
    }

    public SenderLetter toSenderLetter() {
        OfflinePlayer player = CacheOfflinePlayer.getOfflinePlayer(sender);

        return new SenderLetter(player, id, numItems, LocalDateTime.ofInstant(sent.toInstant(), ZoneId.systemDefault()),
                refused);
    }
}
