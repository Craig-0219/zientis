package com.zientis.core;

import com.zientis.core.injection.DependencyContainer;
import com.zientis.core.service.ServiceManager;

/**
 * @deprecated This class is deprecated and will be removed. Use Dependency Injection or access services via the ServiceRegistry instead.
 */
@Deprecated
public class ZientisCore {

    private static ZientisCore instance;

    private ZientisCore() {}

    /**
     * @deprecated Please use dependency injection instead of this static accessor.
     */
    @Deprecated
    public static ZientisCore getInstance() {
        if (instance == null) {
            instance = new ZientisCore();
        }
        return instance;
    }

    /**
     * @deprecated Services should be accessed via DependencyContainer.
     */
    @Deprecated
    public ServiceManager getServiceManager() {
        return DependencyContainer.getInstance().getInstance(ServiceManager.class);
    }

    /**
     * @deprecated The container should be accessed via Bukkit ServicesManager or injection.
     */
    @Deprecated
    public DependencyContainer getDependencyContainer() {
        return DependencyContainer.getInstance();
    }

    /**
     * @deprecated Use the DependencyContainer directly.
     */
    @Deprecated
    public void inject(Object target) {
        getDependencyContainer().inject(target);
    }
}
