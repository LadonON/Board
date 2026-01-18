package github.ladonON.board;

import com.maximde.hologramlib.HologramLib;
import github.ladonON.board.commands.BoardMain;
import github.ladonON.board.leaderboards.Updater;
import org.bukkit.plugin.java.JavaPlugin;

public final class Board extends JavaPlugin {

    private static Board instance;
    private Updater updater;

    @Override
    public void onLoad() {
        HologramLib.onLoad(this);
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getLogger().info("Board plugin enabled!");

        HologramLib.init();

        if (getCommand("board") != null) {
            getCommand("board").setExecutor(new BoardMain(this));
        }

        updater = new Updater(this);

        int intervalTicks = getConfig().getInt("update-interval", 60) * 20;
        updater.start(intervalTicks);
    }


    @Override
    public void onDisable() {
        if (updater != null) {
            updater.stop();
            updater.getDatabase().disconnect();
        }
        HologramLib.onDisable();
    }

    public Updater getUpdater() {
        return updater;
    }
}
