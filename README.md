# ING card summary analyzer

## DESCRIPTION
It parses ING credit card transactions summary PDF into a CSV, with aggregated total sum per contractor

## HOW TO USE
Build a fat jar with dependencies: `mvn clean compile assembly:single`

Run with the included configuration file (ing.xml) and your credit card summary PDF: `java -jar ing-card-analyzer-0.1.0-jar-with-dependencies.jar ing.xml summary.pdf`

It will probably work with other banks as well, tweak the config file to enable it.

## RESULTS
Results are printed in CSV format in the console. Copy it to any spreadsheet.