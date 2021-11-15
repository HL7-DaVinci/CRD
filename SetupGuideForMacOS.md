# The Ultimate Guide to Running DRLS

## Purpose of this guide

This document details the installation process for the **Documentation Requirements Lookup Service (DRLS)** system. Be aware that each component of DRLS has their own README, and this document is **not designed to replace those individual READMEs**, where you will find more detailed documentation of how each DRLS component works. That said, this document ***is designed to take you through the entire set up process for DRLS***. In other words, it is a standalone guide that does not depend on any supplementary DRLS documentation.

This guide will take you through the development environment setup for each of the following DRLS components:
1. [Coverage Requirements Discovery (CRD)](https://github.com/HL7-DaVinci/CRD)
2. [(Test) EHR FHIR Service](https://github.com/HL7-DaVinci/test-ehr)
3. [Documents, Templates, and Rules (DTR) SMART on FHIR app](https://github.com/HL7-DaVinci/dtr)
4. [Clinical Decision Support (CDS) Library](https://github.com/HL7-DaVinci/CDS-Library)
5. [CRD Request Generator](https://github.com/HL7-DaVinci/crd-request-generator)
6. Keycloak

## Table of Contents
- [Prerequisites](#prerequisites)
- [Install core tools](#install-core-tools)
    * [Installing core tools on MacOS](#installing-core-tools-on-macos)
        + [Install AdoptOpenJDK8 or equivalent JDK](#install-adoptopenjdk8)
        + [Install Gradle 6.3](#install-gradle-6.3)
        + [Install Node.js and npm](#install-node.js-v12.14+-and-npm)
        + [Install Docker Desktop for Mac](#install-docker-desktop-for-mac)
- [Clone DRLS](#clone-drls)
- [Configure DRLS](#configure-drls)
    * [CRD configs](#crd-configs)
    * [test-ehr configs](#test-ehr-configs)
    * [crd-request-generator configs](#crd-request-generator-configs)
    * [dtr configs](#dtr-configs)
    * [Add VSAC credentials to your development environment](#add-vsac-credentials-to-your-development-environment)
- [Run DRLS](#run-drls)
    * [Start CRD](#start-crd)
    * [Start test-ehr](#start-test-ehr)
    * [Start keycloak](#start-keycloak)
    * [Start dtr](#start-dtr)
    * [Start crd-request-generator](#start-crd-request-generator)
- [Verify DRLS is working](#verify-drls-is-working)

## Prerequisites

Your computer must have these minimum requirements:
- Running MacOS
    
    > A DRLS setup guide for Windows 10 is currently in progress and will be added to this page when complete. That said, we **highly recommend** that you run DRLS on MacOS, as this was the primary platform on which DRLS was developed.
- x86_64 (64-bit) or equivalent processor
    * Follow these instructions to verify your machine's compliance: https://www.macobserver.com/tips/how-to/mac-32-bit-64-bit/ 
- At least 8 GB of RAM
- At least 256 GB of storage
- Internet access
- [Chrome browser](https://www.google.com/chrome/)
- [Git installed](https://www.atlassian.com/git/tutorials/install-git)

Additionally, you must have credentials (api key) access to the **[Value Set Authority Center (VSAC)](https://vsac.nlm.nih.gov/)**. Later on you will add these credentials to your development environment, as they are required for allowing DRLS to pull down updates to value sets that are housed in VSAC. If you don't already have VSAC credentials, you should [create them using UMLS](https://www.nlm.nih.gov/research/umls/index.html).

## Install core tools

### Installing core tools on MacOS

#### Install AdoptOpenJDK11

> Important note: **Do not skip** these steps, **even if you already have AdoptOpenJDK11 installed on your Mac**, as they will make your life easier when you need to update and/or switch your Mac's default Java version down the line.
> In this guide, the steps to install AdoptOpenJDK11 are given using sdkman. You can feel free to use other package management tools.

1. Install [sdkman](https://sdkman.io/), a MacOS-native tool for package management.
2. Using sdkman, install **AdoptOpenJDK (version 11)** for MacOS. We **do not recommend** using the Oracle JDK as Oracle's license for its JDK is more restrictive.
    > You will need to install the JDK for **Java 11**

    Run the following sdkman commands to complete the install:
    ```bash
    sdk list java # find sdkman's identifier for the LTS version of AdoptOpenJDK 11
    sdk install java <identifier_for_adoptopenjdk11>
    
    # After the installation is complete, verify the correct version of java is running with:
    java -version
    ```

#### Install Gradle 6.3
1. You will use sdkman to do this as well.
    ```bash
    sdk install gradle 6.3
    
    # After the installation is complete, verify the installation worked with:
    gradle -v
    ```

#### Install Node.js v12.14+ and npm
1. Check if you already have Node.js and npm installed:
    ```bash
    node -v # See if Node.js is installed
    npm -v # See if npm is installed
    ```
2. Depending on the output of the above commands, either:
    * **[Install Node.js](https://nodejs.org/en/)**. Be sure to pick the **LTS version**.
        
        > This download will come with npm, so no need to separately install it.
    * or **update your existing Node.js and npm installations** via:
        ```bash
        sudo npm install npm@latest -g # update npm
        sudo npm install n -g # install n, the node version manager
        sudo n stable # update current Node.js installation to latest supported version
        ```
#### Install Docker Desktop for Mac

1. Download the **stable** version of **[Docker for Mac](https://www.docker.com/products/docker-desktop)** and follow the steps in the installer.
2. Once the installation is complete, you should see a Docker icon on your Mac's menu bar (top of the screen). Click the icon and verify that **Docker Desktop is running.**

## Clone DRLS

1. Create a root directory for the DRLS development work (we will call this `<drlsroot>` for the remainder of this setup guide). While this step is not required, having a common root for the DRLS components will make things a lot easier down the line.
    ```bash
    mkdir <drlsroot>
    ```

    `<drlsroot>` will be the base directory into which all the other components will be installed. For example, CRD will be cloned to `<drlsroot>/crd`.
2. Now clone the DRLS component repositories from Github:
    ```bash
    cd <drlsroot>
    git clone https://github.com/HL7-DaVinci/CRD.git CRD
    git clone https://github.com/HL7-DaVinci/CDS-Library CDS-Library
    git clone https://github.com/HL7-DaVinci/test-ehr test-ehr
    git clone https://github.com/HL7-DaVinci/crd-request-generator crd-request-generator
    git clone https://github.com/HL7-DaVinci/dtr dtr
    ```

## Configure DRLS

### CRD configs

1. `cd <drlsroot>/crd/server/src/main/resources`
2. Edit `application.yml` to include:
    ```yaml
    spring:
        profiles:
        active: localDb
    
    localDb:
        path: <drlsroot>/CDS-Library/CRD-DTR/ # add the absolute path to where you cloned CDS-Library. If this path includes your home directory, be sure to specify the full name of your home directory rather than ~/ in the path you provide.
    ```

### test-ehr configs

1. `cd <drlsroot>/test-ehr/src/main/resources`
2. Edit `fhirServer.properties` to include:
    ```bash
    client_id = app-token
    client_secret= #replaceMeWithYourClientSecret
    realm= ClientFhirServer
    use_oauth = false
    ```
    
>Note: **#replaceMeWithYourClientSecret** is not required/needed.

### crd-request-generator configs

1. `cd <drlsroot>/crd-request-generator/src`
2. Edit `properties.json` to look like this:
    ```json
    {
        "realm": "ClientFhirServer",
        "client": "app-login",
        "auth": "http://localhost:8180/auth",
        "server": "http://localhost:8090",
        "ehr_server": "http://localhost:8080/test-ehr/r4",
        "ehr_base": "http://localhost:8080/test-ehr/r4",
        "cds_service": "http://localhost:8090/r4/cds-services",
        "order_sign": "order-sign-crd",
        "order_select": "order-select-crd",
        "user": "alice",
        "password": "alice",
        "public_keys": "http://localhost:3001/public_keys"
    }
    ```

### dtr configs

***None***

### Add VSAC credentials to your development environment

> At this point, you should have credentials to access VSAC. If not, please refer to [Prerequisites](#prerequisites) for how to create these credentials and return here after you have confirmed you can access VSAC.
> 
> **To download the full ValueSets, your VSAC account will need to be added to the CMS-DRLS author group on https://vsac.nlm.nih.gov/. You will need to request membership access from an admin. The folks at MITRE can point you to an admin.** 
> 
> If this is not configured, you will get `org.hl7.davinci.endpoint.vsac.errors.VSACValueSetNotFoundException: ValueSet 2.16.840.1.113762.1.4.1219.62 Not Found` errors.

> While this step is optional, we **highly recommend** that you do it so that DRLS will have the ability to dynamically load value sets from VSAC. 

You can see a list of your pre-existing environment variables on your Mac by running `env` in your Terminal. To add to `env`:
1. `cd ~/`
2. Open `.bash_profile` and add the following lines at the very bottom:
    ```bash
    export VSAC_API_KEY=vsac_api_key
    ```
3. Save `.bash_profile` and complete the update to `env`: 
    ```bash
    source .bash_profile
    ```

> Be aware that if you have chosen to skip this step, you will be required to manually provide your VSAC credentials at http://localhost:8090/data and hit **Reload Data** every time you want DRLS to use new or updated value sets.

## Run DRLS

### Start CRD

```bash
# In a new Terminal tab/window
cd to <drlsroot>/crd
gradle bootRun
```

Wait until you see the server is running at 91% and there is no more console output before proceeding to [Start test-ehr]().

> Optional. Verify CRD is running with the following links.
> 
> - http://localhost:8090 should show you a valid web page.
> - http://localhost:8090/data should reveal a few rule sets with names such as "Hospital Beds" and "Non Emergency Ambulance Transportation."

### Start test-ehr

```bash
# In a new Terminal tab/window
cd to <drlsroot>/test-ehr
rm -rf target # not required if you are running test-ehr for the very first time
rm -rf build # not required if you are running test-ehr for the very first time
gradle bootRun
gradle loadData
```
Wait until you see the server is running at 91% and there is no more console output before proceeding to [Start keycloak]().

> Optional. Verify test-ehr is running with the following links. http://localhost:8080/test-ehr/r4 is the base URL of the server and will require a resource type or operation name appended to the end.
>
> - http://localhost:8080/test-ehr/r4/Patient should display a 200 response with a patient resource.

### Start keycloak

```bash
# In a new Terminal tab/window
docker run --name keycloak -p 8180:8080 --rm -e DB_VENDOR=h2 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin hkong2/keycloak
```
The above command will pull down our latest keycloak container image from Docker Hub and instantiate it.

> Optional. Verify keycloak is running:
>
> - http://localhost:8180 should show you a keycloak log in page.
> - Log in using admin/admin for username and password.

### Start dtr

```bash
# In a new Terminal tab/window
cd <drlsroot>/dtr
npm install # Only required if running crd-request-generator for the first time
npm start
```

> Verify dtr is running:
> - http://localhost:3005/register should show you a simple web page with a form to register a Client ID and Fhir Server.
> - Instructions to register on this page are detailed below in the section **Register the test-ehr**.

### Start crd-request-generator

```bash
# In a new Terminal tab/window
cd <drlsroot>/crd-request-generator
npm install # Only required if running crd-request-generator for the first time
PORT=3000 npm start
```

Once crd-request-generator has started successfully, a webpage running on http://localhost:3000/ehr-server/reqgen should open automatically.

### Optional: (Re)load EHR data

> In order to complete this step, you must already have the test-ehr running.

Do this step if: 
1. You are running DRLS for the first time.
2. New FHIR resources (Patients, Practitioners, Observations, etc.) have been added to the test-ehr.

```bash
# In a new Terminal tab/window
cd <drlsroot>/test-ehr
gradle loadData
```
## Verify DRLS is working

### Register the test-ehr

1. Go to http://localhost:3005/register.
    - Client Id: **app-login**
    - Fhir Server (iss): **http://localhost:8080/test-ehr/r4**
2. Click **Submit**

### The fun part: Generate a test request

1. Go to http://localhost:3000/ehr-server/reqgen.
2. Click **Patient Select** button in upper left.
3. Find **William Oster** in the list of patients and click the dropdown menu next to his name.
4. Select **E0470** in the dropdown menu.
5. Click anywhere in the row for William Oster.
6. Click **Submit** at the bottom of the page.
7. After several seconds you should receive a response in the form of two **CDS cards**:
    - **Respiratory Assist Device**
    - **Positive Airway Pressure Device**
8. Select **Order Form** on one of those CDS cards.
9. If you are asked for login credentials, use **alice** for username and **alice** for password.
10. A webpage should open in a new tab, and after a few seconds, a questionnaire should appear.

Congratulations! DRLS is fully installed and ready for you to use!
