# DD2480-CI-V
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