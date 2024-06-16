package mcjty.lostcities.gui.elements;

import mcjty.lostcities.config.Configuration;
import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.varia.ComponentFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;

public class DoubleElement extends GuiElement {

    private final GuiLCConfig gui;
    private String label = null;
    private String prefix = null;
    private final EditBox field;
    private final String attribute;

    public DoubleElement(GuiLCConfig gui, String page, int x, int y, int w, String attribute) {
        super(page, x, y);
        this.gui = gui;
        this.attribute = attribute;
        Double c = gui.getLocalSetup().get().map(h -> (Double) h.toConfiguration().get(attribute)).orElse(0.0);
        field = new EditBox(gui.getFont(), x, y, w, 16, ComponentFactory.literal(Double.toString(c))) {
            // @todo 1.19.3
//            @Override
//            public void renderToolTip(PoseStack stack, int x, int y) {
//                    gui.getLocalSetup().get().ifPresent(h -> {
//                        gui.renderTooltip(stack, h.toConfiguration().getValue(attribute).getComment(), x, y);
//                    });
//            }
        };
        field.setResponder(s -> {
            gui.getLocalSetup().get().ifPresent(profile -> {
                Configuration configuration = profile.toConfiguration();

                double value = 0;
                try {
                    value = Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    return;
                }
                Configuration.Value val = configuration.getValue(attribute);
                val.set(value);
                if (val.constrain()) {
                    // It was constraint to min/max. Restore the field
                    setValue(val.get());
                }
                profile.copyFromConfiguration(configuration);
                gui.refreshPreview();
            });
        });
        gui.addWidget(field);
    }

    public DoubleElement prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public DoubleElement label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public void tick() {
//        field.tick(); // @todo 1.21
    }

    @Override
    public void render(GuiGraphics graphics) {
        if (field.visible) {
            if (label != null) {
                graphics.drawString(gui.getFont(), label, 10, y + 5, 0xffffffff);
            }
            if (prefix != null) {
                graphics.drawString(gui.getFont(), prefix, x - 8, y + 5, 0xffffffff);
            }
        }
    }

    @Override
    public void update() {
        gui.getLocalSetup().get().ifPresent(profile -> {
            Object result = profile.toConfiguration().get(attribute);
            setValue(result);
        });
    }

    private void setValue(Object result) {
        if (result instanceof Float) {
            field.setValue(Float.toString((Float) result));
        } else if (result instanceof Double) {
            field.setValue(Double.toString((Double) result));
        } else if (result instanceof Integer) {
            field.setValue(Integer.toString((Integer)result));
        }
    }

    @Override
    public void setEnabled(boolean b) {
        field.setEditable(b);
    }

    @Override
    public void setBasedOnMode(String mode) {
        field.setVisible(page.equalsIgnoreCase(mode));
    }

}
