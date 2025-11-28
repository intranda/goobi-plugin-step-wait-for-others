---
title: Warten auf andere Vorgänge
identifier: intranda_step_wait_for_others
description: Step Plugin zum Warten einer Aufgabe auf den gleichen Status anderer Vorgänge mit gleicher Eigenschaft
published: true
keywords:
  - Goobi workflow
  - Plugin
  - Step Plugin
---

## Einführung
Diese Dokumentation erläutert das Plugin zum Warten einer Aufgabe auf den gleichen Status anderer Vorgänge mit gleicher Eigenschaft, um diese anschließend gemeinsam den Workflow durchlaufen zu lassen.

## Installation
Um das Plugin nutzen zu können, müssen folgende Dateien installiert werden:

```bash
/opt/digiverso/goobi/plugins/step/plugin-step-wait-for-others-base.jar
/opt/digiverso/goobi/config/plugin_intranda_step_wait_for_others.xml
```

Nach der Installation des Plugins kann dieses innerhalb des Workflows für die jeweiligen Arbeitsschritte ausgewählt und somit automatisch ausgeführt werden. Ein Workflow könnte dabei beispielhaft wie folgt aussehen:

![Beispielhafter Aufbau eines Workflows](screen1_de.png)

Für die Verwendung des Plugins muss dieses in einem Arbeitsschritt ausgewählt sein:

![Konfiguration des Arbeitsschritts für die Nutzung des Plugins](screen2_de.png)


## Überblick und Funktionsweise
Sobald der Arbeitsschritt mit diesem Plugin ausgelöst wird, werden alle Vorgänge des gleichen Projektes ermittelt, die eine gleiche Eigenschaft mit gleichem Wert aufweisen werden. Haben die anderen Vorgänge bereits den gleichen Status erreicht, löst der letzte Vorgang, der in diesem Arbeitsschritt ankommt aus, dass alle Vorgänge den Arbeitsschritt nun abschließen und den Workflow weiter durchlaufen. Sofern konfiguriert, wird dabei auch ein neuer Batch erzeugt, zu dem alle ermittelten Vorgänge zusammengeführt werden.


## Konfiguration
Die Konfiguration des Plugins erfolgt in der Datei `plugin_intranda_step_wait_for_others.xml` wie hier aufgezeigt:

{{CONFIG_CONTENT}}

{{CONFIG_DESCRIPTION_PROJECT_STEP}}

Parameter               | Erläuterung
------------------------|------------------------------------
`property`              | Name der Eigenschaft, die bei allen Vorgängen vorhanden sein soll und den gleichen Wert aufweisen muss.
`createBatch`           | Vorgabe, ob die ermittelten Vorgänge außerdem in einem neuen Batch gruppiert werden sollen.
