package de.intranda.goobi.plugins;

import java.util.*;

/**
 * This file is part of a plugin for Goobi - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

import org.apache.commons.configuration.SubnodeConfiguration;
import org.goobi.beans.Batch;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.PropertyParser;


@PluginImplementation
@Log4j2
public class WaitForOthersStepPlugin implements IStepPluginVersion2 {
    
    @Getter
    private String title = "intranda_step_wait_for_others";
    @Getter
    private Step step;
    private String returnPath;
    private String property;
    private boolean createBatch = false;
    
    @Override
    public void initialize(Step step, String returnPath) {
        this.returnPath = returnPath;
        this.step = step;
                
        // read parameters from correct block in configuration file
        SubnodeConfiguration myconfig = ConfigPlugins.getProjectAndStepConfig(title, step);
        property = myconfig.getString("property", "default property"); 
        createBatch = myconfig.getBoolean("createBatch", false);
        log.info("WaitForOthers step plugin initialized");
    }

    @Override
    public PluginGuiType getPluginGuiType() {
    	return PluginGuiType.NONE;
    }

    @Override
    public String getPagePath() {
        return "/uii/plugin_step_wait_for_others.xhtml";
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String cancel() {
        return "/uii" + returnPath;
    }

    @Override
    public String finish() {
        return "/uii" + returnPath;
    }
    
    @Override
    public int getInterfaceVersion() {
        return 0;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }
    
    @Override
    public boolean execute() {
        PluginReturnValue ret = run();
        return ret != PluginReturnValue.ERROR;
    }

    @Override
    public PluginReturnValue run() {
        boolean successful = true;

        Optional<String> propertyValue = PropertyParser.getInstance()
                .getPropertiesForProcess(step.getProzess())
                .stream()
                .filter(p -> p.getName().equals(property))
                .map(DisplayProperty::getReadValue)
                .findFirst();

        if (propertyValue.isEmpty()) {
            log.error("Property " + property + " has no value to check");
        	JournalEntry entry = new JournalEntry(step.getProzess().getId(), new Date(), "Plugin " + title, LogType.ERROR,
                    "Property " + property + " has no value to check.", EntryType.PROCESS);
    		JournalManager.saveJournalEntry(entry);
        	return PluginReturnValue.ERROR;
        }
        
        List<Step> stepsToClose = new ArrayList<>();
        String sql = FilterHelper.criteriaBuilder("\"project:" + step.getProzess().getProjekt().getTitel() + "\" \"processproperty:" + property + ":" + propertyValue.get() + "\"", false, null, null, null, true, false);
        List<org.goobi.beans.Process> processes = ProcessManager.getProcesses("prozesse.titel", sql, null);
        for (org.goobi.beans.Process p : processes) {
            if (p.getId().equals(step.getProcessId())) {
                // skip current process
                continue;
            }
            
            // check if all other processes reached this step
            for (Step otherstep : p.getSchritte()) {
                if (step.getTitel().equals(otherstep.getTitel())) {
                    // found step to check
                    switch (otherstep.getBearbeitungsstatusEnum()) {
                        case LOCKED:
                            // if no - wait
                            return PluginReturnValue.WAIT;
                        case ERROR:
                        case INFLIGHT:
                        case INWORK:
                        case OPEN:
                            // must be closed at the end
                            stepsToClose.add(otherstep);
                            break;
                        case DEACTIVATED:
                        case DONE:
                        default:
                            // already got further, do nothing
                            break;

                    }
                }
            }
        }
        
        // we reached this, so we don't have any locked steps
        Batch newBatch = new Batch();
        if (createBatch) {
	        newBatch.setBatchName(property + " " + propertyValue.get());
	        step.getProzess().setBatch(newBatch);
	        ProcessManager.saveProcessInformation(step.getProzess());
        }
        
        // close the step in all other processes
        if (!stepsToClose.isEmpty()) {
            HelperSchritte hs = new HelperSchritte();
            for (Step other : stepsToClose) {
            	if (createBatch) {
	            	other.getProzess().setBatch(newBatch);
	            	ProcessManager.saveProcessInformation(other.getProzess());
            	}
            	hs.CloseStepObjectAutomatic(other);
            }
        }
        
        log.info("WaitForOthers step plugin executed");
        if (!successful) {
            return PluginReturnValue.ERROR;
        }
        return PluginReturnValue.FINISH;
    }
}
