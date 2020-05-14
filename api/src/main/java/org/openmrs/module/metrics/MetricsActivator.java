/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metrics;

import org.openmrs.Encounter;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.metrics.api.MetricsManager;
import org.openmrs.module.metrics.api.service.EventConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class MetricsActivator extends BaseModuleActivator implements DaemonTokenAware {
	
	private static final Logger log = LoggerFactory.getLogger(MetricsActivator.class);
	
	private static volatile DaemonToken daemonToken = null;
	
	private MetricsManager metricsManager;
	
	@Autowired
	private EventConfigurationService eventConfigurationService;
	
	/**
	 * @see #started()
	 */
	public void started() {
		metricsManager = new MetricsManager(daemonToken);
		
		if (eventConfigurationService.getClassesToMonitorFromConfiguration() != null) {
			metricsManager.addClassesToMonitor(eventConfigurationService.getClassesToMonitorFromConfiguration());
		} else {
			metricsManager.addClassToMonitor(Encounter.class);
		}
		
		metricsManager.start();
		
		log.info("Started Metrics");
	}
	
	/**
	 * @see #stopped()
	 */
	
	public void stopped() {
		metricsManager.stop();
		
		log.info("Shutdown Metrics");
	}
	
	@Override
	public void setDaemonToken(DaemonToken token) {
		if (MetricsActivator.daemonToken == null) {
			synchronized (MetricsActivator.class) {
				if (MetricsActivator.daemonToken == null) {
					MetricsActivator.daemonToken = token;
				}
			}
		}
	}
}
