![Maven Build and Test](https://github.com/a-runebou/DD2480-CI-V/actions/workflows/maven.yml/badge.svg)

# DD2480-CI-V

## Summary
This project implements a minimal Continuous Integration (CI) server in Java, designed to demonstrate the core principles of continuous integration. The server listens for GitHub webhook events, checks out the affected branch, compiles the project, executes automated tests, and reports build results back to GitHub using commit status notifications.

**Core Idea:**
1. **GitHub webhook** notifies our server when someone pushes.
2. The server **checks out** the pushed branch/commit into a temporary workspace.
3. It **builds (compiles)** and **runs tests** in that workspace.
4. It **reports the result** (e.g., as a **GitHub commit status**).



---

## Table of Contents

- [DD2480-CI-V](#dd2480-ci-v)
  - [Summary](#summary)
  - [Table of Contents](#table-of-contents)
  - [Project Structure](#project-structure)
  - [Requirements and Dependencies](#requirements-and-dependencies)
    - [Build \& Test](#build--test)
  - [Run the Server](#run-the-server)
  - [Local Setup with ngrok](#local-setup-with-ngrok)
  - [Build list URL](#build-list-url)
  - [Grader's Guide](#graders-guide)
  - [Statement of Contributions](#statement-of-contributions)
    - [Individual Contribution:](#individual-contribution)
      - [Dev. Fabian W (GitHub: @crancker96):](#dev-fabian-w-github-crancker96)
      - [Dev. Apeel Subedi (GitHub: @rippyboii):](#dev-apeel-subedi-github-rippyboii)
      - [Dev. Carl Isaksson (GitHub: @carlisaksson):](#dev-carl-isaksson-github-carlisaksson)
      - [Dev. Josef Kahoun (GitHub: @kahoujo1):](#dev-josef-kahoun-github-kahoujo1)
      - [Dev. Alexander Runebou (GitHub: @a-runebou):](#dev-alexander-runebou-github-a-runebou)
    - [Team Contribution:](#team-contribution)
  - [.](#)
  - [SEMAT](#semat)
  - [License](#license)

---

## Project Structure
```
.
├── .github/workflows/       # GitHub Actions 
├── ci-server/              # Main project file
│   ├── src/main/java/com/ci/
│   │   ├── Server.java     # HTTP server + webhook endpoint
│   │   ├── checkout/       # Git checkout logic
│   │   ├── pipeline/       # CIPipeline to structure the integration
│   │   └── statuses/       # GitHub commit status poster/reporting
│   └── src/test/java/...   # Unit tests
└── LICENSE
```

---


## Requirements and Dependencies
- Java 19 (JDK)
- **Git** installed and available in PATH
- Internet access for webhooks

Verify your Java installation by running:
```bash
java -version
```


### Build & Test 
```bash
cd ci-server
./mvnw clean test
```
Note: This project uses Maven Wrapper, no local Maven installation is required, but Maven version 3.9.12 was used locally.

---

## Run the Server

Start the server:

```bash
./mvnw exec:java
```

When running, the server exposes:
- `POST /webhook`: GitHub sends push payloads here.

---

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
Note: The `token.config` file must be added in following paths for the program to build:
   - `ci-server/src/main/resources/token.config`
   - `ci-server/src/test/resources/token.config`



To post commit statuses, the server needs a GitHub token.
We recommend using fine-grained personal access tokens, [see GitHub Authentication](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens).

The notifications are sent using the `StatusPoster` class, which allows
- Setting the commit status to one of the following values: `'error'`, `'pending'`, `'failure'`, or `'success'`. 
- Adding a description for the status.
- Adding a URL associated with the CI job.

For more information, please refer to the `StatusPoster` implementation.


---

## Build list URL
The CI server saves results of the individual builds inside a database. Specifically, the commit SHA, build/test result, short build/test description, and build date are saved for each commit received.

To view the saved entries, you can use the public REST API by sending GET HTTP requests, specifically:
- `\builds` returns information about all saved entries.
- `\builds\{SHA}` returns information about the entry with the given SHA, granted such entry exists 


---

## Grader's Guide

 `assessment` branch is made for grading reference.
All grading actions are to be performed on the branch **`assessment`**.  


## Statement of Contributions
 

### Individual Contribution:
#### Dev. Fabian W (GitHub: @crancker96): 
> Dummy Class, Documentation, Bug fixes


#### Dev. Apeel Subedi (GitHub: @rippyboii):
> CI Pipeline Implementation, Documentation, GitCheckout Service, Bug fixes


#### Dev. Carl Isaksson (GitHub: @carlisaksson):
> ...........

#### Dev. Josef Kahoun (GitHub: @kahoujo1):
> Commit Status posting, Database handling, REST API for the database, Documentation

#### Dev. Alexander Runebou (GitHub: @a-runebou):
>  Project structure, HTTP server, Webhook handling, Documentation, Bug fixes, Code coverage



### Team Contribution:
.
.
.
.
---

## SEMAT 

**Team state (Essence "Team" alpha)**: Collaborating.

Our team is currently in the Collaborating state. The mission, roles, responsibilities, and communication methods are defined, and team members work together toward shared goals. 

For a detailed description, see our Wiki page at: [Way of Working documentation](https://github.com/a-runebou/DD2480-CI-V/wiki/Progress-of-the-Team).

---

## License

This project is licensed under the terms in [LICENSE](LICENSE).