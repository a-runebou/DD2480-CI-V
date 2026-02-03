# DD2480-CI-V
## Notification of CI results
The notification of results was implemented using commit statuses using GitHub REST API.
Settings for the notification are contained in a file `ci-server/src/main/resources/token.config` (note that the contents of the file are missing in GitHub, and need to be set up manually).
The file is a traditional configuration file and must include the following entries:
`owner: <OWNER_NAME>`,
`repo: <REPO_NAME>`,
and `token: <TOKEN>` (we suggest fine-grained personal access tokens, [see GitHub Authentication](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)).

The notification is executed using the `StatusPoster` class, and allows for setting the commit status to one of the following values: 'error', 'pending', 'failure', or 'success'. It also allows for adding a description and a URL associated with the CI job. For more information, please refer to the implementation.