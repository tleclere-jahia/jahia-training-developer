# Cypress.io end-to-end tests

[Cypress.io](https://www.cypress.io) is an open source, MIT licensed end-to-end test runner

## Folder structure

These folders hold end-to-end tests and supporting files for the Cypress Test Runner.

- [e2e](e2e) holds the actual test files, [read more](https://on.cypress.io/writing-and-organizing-tests)
- [fixtures](fixtures) holds files sent by the tests, such as the zip imported by the `importNode` provisioning operation
- [support](support) runs before all tests. [training.ts](support/training.ts) holds the helpers shared by the specs:
  site setup, GraphQL queries and mutations, node creation.

## Running the tests

The tests need a running Jahia with the `jahia-training-developer` module deployed, its dependencies, and the
`dx-base-demo-templates` template set.

```bash
yarn install
yarn e2e:ci
```

The configuration is in [../cypress.config.ts](../cypress.config.ts). These environment variables override the defaults:

| Variable              | Default                  |
|-----------------------|--------------------------|
| `JAHIA_URL`           | `http://localhost:8080`  |
| `SUPER_USER_PASSWORD` | `root`                   |
| `CYPRESS_TEMPLATE_SET`| `dx-base-demo-templates` |

Each spec creates its own site and deletes it at the end. `platform.test.cy.ts` uses the `digitall` site, because the
module configuration of `GoneResourceErrorHandler` and of the `/modules/training/test` endpoint names it.

## More information

- [https://github.com/cypress.io/cypress](https://github.com/cypress.io/cypress)
- [https://docs.cypress.io/](https://docs.cypress.io/)
- [Writing your first Cypress test](http://on.cypress.io/intro)
