# Matterminder

Matterminder is a service that can handle reminders for Mattermost.  
It is a scala play application that is backed by a postgresql database.

## Matterminder Configuration

There are only a few configurations needed to be adjusted.
All of them can be found in their corresponding files in the conf-Folder or - if they stay unchanged - via environment variables. 

### Mattermost Server

- *MATTERMINDER_MATTERMOST_SERVER*: The url to the Mattermost server

- *MATTERMINDER_MATTERMOST_PORT*: The port to access Mattermost

- *MATTERMINDER_MATTERMOST_PROTOCOL*: The protocol to access Mattermost (e.g. http)

- *MATTERMINDER_SLASH_COMMAND_TOKEN_TO_WEBHOOK_KEY_MAPPINGS*: To support multiple teams in Mattermost you have to create a slash command and an incoming webhook in each team. Each of the slash commands get an own token and each of the incoming webhooks an own key. To be able to send notifications to Mattermost, we must map every token to the corresponding key in each team in this format:  
  **token-of-team1:key-of-team1#token-of-team2:key-of-team2**

### Database

- *MATTERMINDER_POSTGRES_SERVER*: The host on which the db is running

- *MATTERMINDER_POSTGRES_DATABASE*: The database name

- *MATTERMINDER_POSTGRES_USER*: The database user

- *MATTERMINDER_POSTGRES_PASSWORD*: The password of the database user

### Application Secret

For Play, an application secret is needed to be set when running in production mode.

If the application is staged with sbt (```sbt stage```), it can be run like this:  
- **target/universal/stage/bin/matterminder -Dplay.crypto.secret="matterminderSecret"**

## Docker

An example dockerfile and docker-compose file is provided in the docker folder. 

## Mattermost Configuration

Incoming webhooks and slash commands must be allowed (**System Console** => **Integrations** => **Custom Integrations**).

Starting with Mattermost v4.2.2, if your Matterminder instance has an internal IP address, you have to allow this connection in the configuration (**System Console** => **Advanced** => **Developer** => **Allow untrusted internal connections to**).
> https://docs.mattermost.com/administration/config-settings.html#allow-untrusted-internal-connections-to

After this, for each team that should be able to use Matterminder, a slash command and an incoming webhook needs to be created.

### Slash Command

- **Integrations** => **Slash Commands** => **Add Slash Command**

In general the settings are self explaining and have a nice explanation below. 
Most of the settings will only have cosmetic effect.

Important settings:

- **Request URL:** The URL that is called at the Matterminder service.  
The route is always ```remind``` and the port is ```9000``` by default. Example: **http://my-matterminder-server:9000/remind**

- **Request Method:** This must be set to ```POST``` (which is the default anyway)
 

### Incoming Webhook

- **Integrations** => **Incoming Webhooks** => **Add Incoming Webhook**

- **Display Name** and **Description** are self explaining and have an additional explanation below.  
- **Channel** is the default channel for incoming messages and is ignored anyway, so every channel is fine here.


## Known Issues

- **Private reminders for creator of the incoming webhook**  
Since webhooks cannot direct message the user who created the webhook, Matterminder is not able to send notifications for private reminders to the user that created the incoming webhook.  
There is a feature request to add this ability: https://mattermost.uservoice.com/forums/306457-general/suggestions/15697014-add-option-to-direct-message-yourself 
