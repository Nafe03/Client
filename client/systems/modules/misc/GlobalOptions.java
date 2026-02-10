package dev.anarchy.waifuhax.client.systems.modules.misc;

import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.systems.modules.annotations.AutoDisable;
import dev.anarchy.waifuhax.api.systems.modules.annotations.Hidden;
import dev.anarchy.waifuhax.api.systems.modules.annotations.ListHidden;
import dev.anarchy.waifuhax.api.systems.modules.annotations.ManualInstanciating;
import lombok.AccessLevel;
import lombok.Getter;

@ListHidden
@Hidden
@AutoDisable
@ManualInstanciating
public class GlobalOptions extends AbstractModule {

    public BooleanSetting allowDragOutsideOfHud = new BooleanSetting("Allow dragging outside of hud editor",
            "allows you to move hud elements accross the screen without opening the hud editor",
            true);

    public BooleanSetting hideTopBarOutsideClickgui = new BooleanSetting("Hide top menu bar outside of clickgui",
            "disable the top menu bar outside of WaifuHax related screens",
            false);

    public BooleanSetting printToggleMessage = new BooleanSetting("Print Toggle Message",
            "",
            true);

    @Getter(AccessLevel.PUBLIC)
    private static GlobalOptions instance;

    public GlobalOptions() {
        instance = this;
        isEnabled.setShouldDraw(false);
        keycode.setShouldDraw(false);
    }

    @Override
    public String getDescription() {
        return "";
    }
}
