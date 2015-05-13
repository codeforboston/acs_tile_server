# ACS Tile Server

This project has been created to deal with census/acs data and will help generating maps for the ungentry project.
It connects to the census ftp server to collect data, then process them to build maps.

## Compilation

At root folder type:
`mvn package`

This should create a folder target with a jetty-censusacsserver.jar file.
When starting, it will open the default browser to display a server UI.

## Running

In target directory, just type :
`java -jar jetty-censusacsserver.jar -d ../data`

## Workflow:

Select build map in the menu, then select ACS data year, it will be downloaded from Census ftp server
![Step 1](/doc/Workflow_Step1.png)

Then select a state, to work with
![Step 2](/doc/Workflow_Step2.png)

It should be highlighted, then click next
![Step 3](/doc/Workflow_Step3.png)

When needed it will upload state files from the census
![Step 5](/doc/Workflow_Step5.png)

It will computes the set of data available for the selected state (this can take few minutes). 
Just check some of those fields, then click next
![Step 6](/doc/Workflow_Step6.png)

After some time, it will start to build tiles for the selected map
![Step 7](/doc/Workflow_Step7.png)

You can use the little leaflet example to display tiled layer that currently works with images
![Step 8](/doc/Workflow_Step8.png)





