package dev.anarchy.waifuhax.client.screens;

import dev.anarchy.waifuhax.api.gui.WHWindow;
import dev.anarchy.waifuhax.api.gui.components.impl.UIText;
import dev.anarchy.waifuhax.client.events.hud.DrawnWaifuHudEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class HalfLifeStyleMainMenu extends Screen {

    private SplashTextRenderer splashText;

    private final Identifier MAIN_MENU_BACKGROUND = Identifier.of("waifuhax", "textures/background.png");

    private final WHWindow window = new WHWindow("Test Checkbox (1)", new Vector2f(455, 20));
    private final WHWindow window2 = new WHWindow("Test Checkbox (2)", new Vector2f(455 + 196, 20));

    private WHWindow selected = null;

    private final UIText scale = (UIText) new UIText()
            .setText("Hello World !")
            .setScale(1f)
            .setColor(0xFFFFFF)
            .setPos(new Vector3f(5, 5, 0));

    public HalfLifeStyleMainMenu() {
        super(Text.of("ttttt"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float scaleFactor = (float) (1 / MinecraftClient.getInstance().getWindow().getScaleFactor());
        //context.drawTexture(RenderLayer::getGuiTextured, MAIN_MENU_BACKGROUND, 0, 0, 0, 0, width, height, width, height);
        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.of("width: " + width + " | height: " + height + " | scale: " + scaleFactor + " window info : " + (window == null ? "null" :
                "window pos: " + window.getRoot().getAbsolutePos().x + " " + window.getRoot().getAbsolutePos().y +
                        " window's child pos: " + window.getRoot().getChildAtIndex(0).getAbsolutePos().x + " " + window.getRoot().getChildAtIndex(0).getAbsolutePos().y)),
                5, 5, 0xFFFFFFFF, true);
        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.of("Mouse: " + MinecraftClient.getInstance().mouse.getX() + " | " + MinecraftClient.getInstance().mouse.getY() + " | is hovered " + window.isHeaderHovered(mouseX, mouseY)),
                5, 15, 0xFFFFFFFF, true
        );
        window.onHudBeingDrawn(DrawnWaifuHudEvent.get(context));
        window2.onHudBeingDrawn(DrawnWaifuHudEvent.get(context));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    /*
    private UIContainer comp = new UIContainer();
    private UIContainer back = new UIContainer();

    public HalfLifeStyleMainMenu() {
        super(ElementaVersion.V10);

        final Window window = MinecraftClient.getInstance().getWindow();

        System.out.println("Window screen factor: " + window.getScaleFactor());

        final float buttonsX = 32f;
        final float firstButtonY = 0.4f;
        final float normalOffset = 5f;
        final float exitOffset = 5f;

        back.setY(new PixelConstraint(0))
                .setX(new PixelConstraint(0))
                .setWidth(new PixelConstraint(window.getWidth()))
                .setHeight(new PixelConstraint(window.getHeight()))
                .setChildOf(getWindow())
                .addChild(UIImage.ofResource("/assets/waifuhax/textures/background.png")
                        .setWidth(new RelativeWindowConstraint(1f))
                        .setHeight(new RelativeWindowConstraint(1f)));

        comp.setX(new CenterConstraint())
                .setY(new PixelConstraint(0))
                .setX(new PixelConstraint(0))
                .setWidth(new PixelConstraint(window.getWidth()))
                .setHeight(new PixelConstraint(window.getHeight()))
                .setChildOf(back.getChildren().get(0))
                .addChild(UIImage.ofResource("/assets/waifuhax/textures/hlmenu/title.png"))
                        .setHeight(new RelativeWindowConstraint(1f))
                        .setWidth(new RelativeWindowConstraint(1f))
                .addChild(UIImage.ofResource("/assets/waifuhax/textures/hlmenu/subtitle.png"))
                        .setY(new SiblingConstraint())
                .addChild(new ClickableTextBuilder()
                        .setName("Singleplayer")
                        .setPos(new Vec2f(buttonsX, firstButtonY))
                        .onMouseClick((listener, event) -> {
                            MinecraftClient.getInstance().setScreen(new SelectWorldScreen(this));
                            return null;
                        })
                        .build()
                ).addChild(new ClickableTextBuilder()
                        .setName("Multiplayer")
                        .setPos(new Vec2f(buttonsX, 0))
                        .onMouseClick((listener, event) -> {
                            MinecraftClient.getInstance().setScreen(new MultiplayerScreen(this));
                            return null;
                        })
                        .build().setY(new SiblingConstraint(normalOffset))
                ).addChild(new ClickableTextBuilder()
                        .setName("Options")
                        .setPos(new Vec2f(buttonsX, 0))
                        .onMouseClick((listener, event) -> {
                            MinecraftClient.getInstance().setScreen(new OptionsScreen(this, MinecraftClient.getInstance().options));
                            return null;
                        })
                        .build().setY(new SiblingConstraint(normalOffset))
                );
        if (ModUtils.isModPresent("modmenu")) {
            // I can explain...
            // I just need to instantiate the Mod Menu's screen,
            // no need to include the entire fucking api in the build.gradle
            comp.addChild(new ClickableTextBuilder()
                            .setName("Mods (" + ModUtils.getLoadedMods().size() + ")")
                            .setPos(new Vec2f(buttonsX, 0))
                            .onMouseClick((listener, event) -> {
                                try {
                                    MinecraftClient.getInstance()
                                            .setScreen((Screen) Class.forName("com.terraformersmc.modmenu.gui.ModsScreen")
                                            .getDeclaredConstructor(Screen.class).newInstance(this));
                                } catch (InstantiationException |
                                         IllegalAccessException |
                                         InvocationTargetException |
                                         NoSuchMethodException |
                                         ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                                return null;
                            }).build().setY(new SiblingConstraint(normalOffset)));
        }
        comp.addChild(new ClickableTextBuilder()
                .setName("Exit")
                .setPos(new Vec2f(buttonsX, 0.75f))
                .onMouseClick((listener, event) -> {
                    MinecraftClient.getInstance().scheduleStop();
                    return null;
                })
                .build().setY(new SiblingConstraint(exitOffset)));

    }


    private void debugInfo(DrawContext context) {
        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.of("Mouse: " + MinecraftClient.getInstance().mouse.getX() + " | " + MinecraftClient.getInstance().mouse.getY()),
                5, 5, 0xFFFFFF, true
        );

        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.of("Screen: " + context.getScaledWindowWidth() + " | " + context.getScaledWindowHeight()),
                5, 15, 0xFFFFFF, true
        );

        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.of("Font height: " + MinecraftClient.getInstance().textRenderer.fontHeight),
                5, 25, 0xFFFFFF, true
        );

        final int scale = MinecraftClient.getInstance().options.getGuiScale().getValue();

        context.drawText(MinecraftClient.getInstance().textRenderer,
                Text.of(
                        "X1: " + (32 * scale) +
                                " | X2: " + (32 + (MinecraftClient.getInstance().textRenderer.getWidth("Single Player")) * scale) +
                                " | Y1: " + (context.getScaledWindowHeight() * scale) +
                                " | Y2: " + (context.getScaledWindowHeight() * scale + (MinecraftClient.getInstance().textRenderer.fontHeight * scale))
                ), 5, 35, 0xFFFFFF, true
        );
    }

    @Override
    public void onDrawBackground(@NotNull UMatrixStack matrixStack, int tint) {

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public void blur() {
    }*/
}
