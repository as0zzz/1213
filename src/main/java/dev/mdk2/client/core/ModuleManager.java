package dev.mdk2.client.core;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<Module>();

    public void register(final Module module) {
        this.modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(this.modules);
    }

    public List<Module> getByCategory(final Category category) {
        final List<Module> result = new ArrayList<Module>();
        for (final Module module : this.modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public <T extends Module> T get(final Class<T> moduleType) {
        for (final Module module : this.modules) {
            if (moduleType.isInstance(module)) {
                return moduleType.cast(module);
            }
        }
        return null;
    }
}
