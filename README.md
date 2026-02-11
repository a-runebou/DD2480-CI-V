# DD2480-CI-V
xx
## Summary
This project implements a minimal Continuous Integration (CI) server in Java, designed to demonstrate the core principles of continuous integration. The server listens for GitHub webhook events, checks out the affected branch, compiles the project, executes automated tests, and reports build results back to GitHub using commit status notifications.

## Requirements
- Java 19 (JDK)

Verify your Java installation by running:
```bash
java -version
```

Maven version 3.9.12 was used locally, but this project uses Maven Wrapper meaning no local Maven installation is required.

## Build and Run

```bash
./mvnw clean test
./mvnw exec:java
```

## Local Setup with ngrok
To test the CI server locally with GitHub webhooks, you can use [ngrok](https://ngrok.com/) to expose your local server to the internet.
1. Download and install ngrok from the [official website](https://ngrok.com/download).
2. Start your CI server locally by running:
   ```bash
   ./mvnw exec:java
   ```
3. In a separate terminal, run ngrok to expose your local server (assuming your CI server runs on port `2485` as per default):
   ```bash
    ngrok http 2485
    ```
4. ngrok will provide you with a public URL (e.g., `https://<random-id>.ngrok-free.app`). Use this URL to configure your GitHub webhook.
5. In your GitHub repository, go to **Settings** > **Webhooks** > **Add webhook**.
   - Set the **Payload URL** to your ngrok URL (e.g., `https://<random-id>.ngrok-free.app/webhook`).
   - Set the **Content type** to `application/json`.
   - Disable **SSL verification**.
   - Choose **Just the push event** as the events you want to trigger the webhook.
   - Make sure the webhook is **Active**.
   - Click **Add webhook** to save.
6. Now, when you push changes to the GitHub repository, GitHub will send a webhook to your local CI server via ngrok, which triggers the pipeline.

## Notification of CI results
The notification of results was implemented using **commit statuses** via the GitHub REST API.
Settings for the notification must be stored inside file `ci-server/src/main/resources/token.config` (note that the file is missing on GitHub, and needs to be set up manually).

The configuration file must include the following entries:
```
owner: <OWNER_NAME>
repo: <REPO_NAME>
token: <TOKEN>
```
We recommend using fine-grained personal access tokens, [see GitHub Authentication](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens).

The notifications are sent using the `StatusPoster` class, which allows
- Setting the commit status to one of the following values: `'error'`, `'pending'`, `'failure'`, or `'success'`. 
- Adding a description for the status.
- Adding a URL associated with the CI job.

For more information, please refer to the `StatusPoster` implementation.