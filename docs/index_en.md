---
title: Waiting for Other Processes
identifier: intranda_step_wait_for_others
description: Step plugin for waiting on a task until other processes with the same property reach the same status
published: true
keywords:
  - Goobi workflow
  - Plugin
  - Step Plugin
---

## Introduction
This documentation explains the plugin that allows a task to wait until other processes with the same property reach the same status, so that they can then continue through the workflow together.

## Installation
To use the plugin, the following files must be installed:

```bash
/opt/digiverso/goobi/plugins/step/plugin-step-wait-for-others-base.jar
/opt/digiverso/goobi/config/plugin_intranda_step_wait_for_others.xml
```

Once the plugin is installed, it can be selected within the workflow for the corresponding tasks and thus executed automatically. An example workflow might look as follows:

![Example structure of a workflow](screen1_en.png)

To use the plugin, it must be selected for a workflow step:

![Configuration of the task step for using the plugin](screen2_en.png)


## Overview and Functionality

As soon as the workflow step with this plugin is triggered, all processes of the same project that share the same property with the same value are identified. If the other processes have already reached the same status, the last process arriving at this step causes all processes to complete the step and continue in the workflow. If configured, a new batch is also created that merges all identified processes.


## Configuration
The plugin is configured in the file plugin_intranda_step_wait_for_others.xml as shown here:

{{CONFIG_CONTENT}}

{{CONFIG_DESCRIPTION_PROJECT_STEP}}

Parameter               | Description
------------------------|------------------------------------
`property`              | Name of the property that must exist in all processes and have the same value.
`createBatch`           | Specifies whether the identified processes should also be grouped into a new batch.