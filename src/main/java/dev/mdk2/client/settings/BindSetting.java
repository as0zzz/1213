package dev.mdk2.client.settings;

import org.lwjgl.glfw.GLFW;

public class BindSetting extends Setting<Integer> {
    public static final int NONE = -1;
    public static final int MOUSE_OFFSET = 1000;

    public BindSetting(final String name, final int defaultKey) {
        super(name, Integer.valueOf(defaultKey));
    }

    public int getKey() {
        return getValue().intValue();
    }

    public void setKey(final int key) {
        setValueInternal(Integer.valueOf(key));
    }

    public String getDisplayName() {
        return getKeyName(this.getKey());
    }

    public static String getKeyName(final int key) {
        if (key == NONE) {
            return "None";
        }
        if (isMouseBind(key)) {
            final int mouseButton = toMouseButton(key);
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                return "Mouse1";
            }
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                return "Mouse2";
            }
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                return "Mouse3";
            }
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_4) {
                return "Mouse4";
            }
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_5) {
                return "Mouse5";
            }
            return "Mouse" + (mouseButton + 1);
        }
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            return "RShift";
        }
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT) {
            return "LShift";
        }
        if (key == GLFW.GLFW_KEY_LEFT_CONTROL) {
            return "LCtrl";
        }
        if (key == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            return "RCtrl";
        }
        if (key == GLFW.GLFW_KEY_LEFT_ALT) {
            return "LAlt";
        }
        if (key == GLFW.GLFW_KEY_RIGHT_ALT) {
            return "RAlt";
        }
        if (key == GLFW.GLFW_KEY_SPACE) {
            return "Space";
        }
        if (key == GLFW.GLFW_KEY_ENTER) {
            return "Enter";
        }
        if (key == GLFW.GLFW_KEY_TAB) {
            return "Tab";
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            return "Esc";
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            return "Backspace";
        }
        if (key == GLFW.GLFW_KEY_DELETE) {
            return "Delete";
        }

        final String glfwName = GLFW.glfwGetKeyName(key, 0);
        if (glfwName != null && !glfwName.isEmpty()) {
            return glfwName.toUpperCase();
        }

        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25) {
            return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        }

        return "Key " + key;
    }

    public static int fromMouseButton(final int button) {
        return MOUSE_OFFSET + button;
    }

    public static boolean isMouseBind(final int key) {
        return key >= MOUSE_OFFSET;
    }

    public static int toMouseButton(final int key) {
        return key - MOUSE_OFFSET;
    }
}
