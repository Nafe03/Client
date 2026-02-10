package dev.anarchy.waifuhax.client.systems.modules.hud;


import dev.anarchy.waifuhax.api.systems.modules.annotations.AutoEnable;
import dev.anarchy.waifuhax.api.systems.modules.annotations.Hidden;
import dev.anarchy.waifuhax.api.systems.modules.annotations.ListHidden;

// This one is a special hud module.
@AutoEnable
@ListHidden
@Hidden
public class TopMenuBar {

/*
    private final Set<ImguiMenu> tabs = new LinkedHashSet<>();

    public TopMenuBar() {
        super(CATEGORY.HUD);

        tabs.addAll(List.of(
                new ImguiMenu().setName("Modules").addChild(
                        new ImguiMenu().setName("Reload all").setMenuItem(true).setCallback(args -> ModuleManager.loadAll()),
                        new ImguiMenu().setName("Save all").setMenuItem(true).setCallback(args -> ModuleManager.saveAll())
                ),
                new ImguiMenu().setName("Systems").addChild(
                        new ImguiMenu().setName("Lua macros").setMenuItem(true).setCallback(args -> WHLogger.printToChat("Work In Progress")),
                        new ImguiMenu().setName("Wasp System").setMenuItem(true).setCallback(args -> WHLogger.printToChat("Work In Progress"))
                ),
                new ImguiMenu().setName("Friend").addChild(
                        new ImguiMenu().setName("Friend Management UI").setMenuItem(true).setCallback(args -> WHLogger.printToChat("Work In Progress")),
                        new ImguiMenu().setName("Sync accross all client").setMenuItem(true).setCallback(args -> WHLogger.printToChat("Work In Progress")),
                        new ImguiMenu().setName("Load from loaded clients").setMenuItem(true).setCallback(args -> WHLogger.printToChat("Work In Progress"))
                ),
                new ImguiMenu().setName("HUD").addChild(
                        new ImguiMenu().setName("Open Click GUI").setMenuItem(true).setCallback(args -> MinecraftClient.getInstance().setScreen(new ClickGuiScreen())),
                        new ImguiMenu().setName("Open Theme Editor").setMenuItem(true).setCallback(args -> ModuleManager.getModule(ThemeEditor.class).toggle()),
                        new ImguiMenu().setName("Open Hud Editor").setMenuItem(true).setCallback(args -> MinecraftClient.getInstance().setScreen(new HudEditor())),
                        new ImguiMenu().setName("settings").setMenuItem(true).setCallback(args -> ModuleManager.getModule(Settings.class).toggle())
                )
        ));
    }

    @Override
    public String getDescription() {
        return "";
    }

    @EventHandler
    public void onImguiRender(RenderImGuiEvent event) {
        WHLogger.print("Rendering myself");
        if (GlobalOptions.getInstance().hideTopBarOutsideClickgui.getValue()) {
            if (!(mc.currentScreen instanceof ClickGuiScreen || mc.currentScreen instanceof HudEditor)) {
                return;
            }
        }
        if (ImGui.beginMainMenuBar()) {
            tabs.forEach(ImguiMenu::draw);
            ImGui.endMainMenuBar();
        }
    }*/
}
