# AzureMCBootstrapper

AzureMCBootstrapper is a core component of the AzureMC project. It performs the following functions:

- Downloads the latest version of PaperMC
- Fetches the most recent server data from Azure Blob Storage
- Initiates the Minecraft server
- Collects and uploads server data after the Minecraft server has stopped
- Requests the removal of the VM

## Envoirement variables
- `BLOB_STRING`: Connection String vor Azure Blob Storage
- `FUNCTIONS_DOMAIN`: Funtions domain name for AzureMCFuntions

***or***

Provide the variables via ``java -jar bootloader.jar domain blob`` as command arguments.