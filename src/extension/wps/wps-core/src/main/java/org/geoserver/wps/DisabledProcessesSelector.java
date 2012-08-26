package org.geoserver.wps;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geoserver.config.ConfigurationListenerAdapter;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInitializer;
import org.geoserver.config.ServiceInfo;
import org.geoserver.wps.process.GeoServerProcessors;
import org.geoserver.wps.process.ProcessSelector;
import org.geotools.process.ProcessFactory;
import org.opengis.feature.type.Name;

/**
 * Filters processes and process factories based on the configuration in WPSInfo
 */
public class DisabledProcessesSelector extends ProcessSelector implements GeoServerInitializer {

    Set<Name> disabledProcesses = new HashSet<Name>();

    @Override
    protected boolean allowProcess(Name processName) {
        return !disabledProcesses.contains(processName);
    }

    @Override
    public void initialize(GeoServer geoServer) throws Exception {
        WPSInfo wps = geoServer.getService(WPSInfo.class);
        if(wps != null) {
            updateFilters(wps);
        }
        
        geoServer.addListener(new ConfigurationListenerAdapter() {
            @Override
            public void handlePostServiceChange(ServiceInfo service) {
                if(service instanceof WPSInfo) {
                    updateFilters((WPSInfo) service);
                }
            }
        });
    }

    private void updateFilters(WPSInfo wps) {
        List<ProcessGroupInfo> groups = wps.getProcessGroups();
        disabledProcesses.clear();
        if (groups != null) {
            for (ProcessGroupInfo group : groups) {
                if (!group.isEnabled()) {
                    ProcessFactory factory = GeoServerProcessors.getProcessFactory(group.getFactoryClass(), false);
                    if(factory != null) {
                        disabledProcesses.addAll(factory.getNames());
                    }
                } else if (group.getFilteredProcesses() != null) {
                    disabledProcesses.addAll(group.getFilteredProcesses());
                }
            }
        }
    }

}
