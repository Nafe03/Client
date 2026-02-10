package dev.anarchy.waifuhax.client.systems.modules.misc;

import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AntiSpam extends AbstractModule {

    private final List<String> rules = new ArrayList<>();

    @Override
    public String getDescription() {
        return "Advanced spam filter";
    }

    public List<String> getRules() {
        return new ArrayList<>(rules);
    }

    public void addRule(String rule) {
        rules.add(rule);
        saveRules();
    }

    public void loadRuleFile() {
        final String path = PathUtils.join("./WaifuHax", "filter.txt");
        if (new File(path).exists()) {
            rules.addAll(PathUtils.getAllLines(path));
        }
    }

    public void saveRules() {
        StringBuilder ruleStr = new StringBuilder();
        for (String rule : rules)
            ruleStr.append(rule).append("\n");
        PathUtils.writeStringToFile(PathUtils.join("./WaifuHax", "filter.txt"), ruleStr.toString());
    }

    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);
        if (!new File(PathUtils.join("./WaifuHax", "filter.txt")).exists()) {
            WHLogger.printToChat("it seems you have not used this module yet. do \"§b!filter add [regex expression]§r\"");
        }
    }

    public void rmRule(int index) {
        if (index < rules.size() && rules.get(index) != null) {
            rules.remove(index);
        }
        saveRules();
    }
}
