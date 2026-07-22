# Body-weight Tracking and Production Release

Date: 2026-07-22
Status: Completed

## Requested outcome

Add a maintainable body-weight module to the existing daily food workflow. The homepage needs two equal circular actions, a pale-green and white visual refresh, a weight summary below the eaten-food list, a separate editor, and a separate trend page.

## Agreed product rules

- Weight is recorded for the homepage-selected date. Historical dates allow backfill; future dates remain tappable but explain that recording is unavailable.
- One real record is stored per date in kilograms, greater than zero and with at most one decimal place.
- A missing date shows the most recent earlier real record. Dates before the first record remain empty.
- Carried values edit their actual source record; delete is available only for a real record on the selected date.
- Trends use rolling 7-, 30-, or 365-day ranges. The chosen end date is capped at today, and the calendar disables dates before the first record and after today.

## Delivered engineering changes

### Backend

- Added the non-destructive `body_weight_records` migration with a unique date, positive one-decimal weight, timestamps, and an index.
- Kept controller, service, mapper, entity, and DTO responsibilities separate.
- Added selected-date snapshot, date upsert, real-record update/delete, and trend APIs.
- Implemented authoritative future-date and precision validation plus effective-value and trend carry-forward logic.
- Updated backend and deployment documentation with migration, backup, and rollback commands.

### Frontend

- Replaced the homepage action with two equal circular icon-and-short-label actions and applied the shared pale-green/white theme.
- Added reusable `WeightSummaryCard`, independent weight editor and trend pages, calendar selection, preset controls, and a canvas line chart.
- Kept API access, date/trend pure logic, components, and page orchestration in separate modules.
- Added stale-response and duplicate-submit guards and preserved the existing calendar and food-entry behavior.
- Changed the default frontend unit-test command to target source-level suites explicitly; the generated-bundle check remains in `verify:weapp` so an H5 build cannot silently overwrite `dist` before the WeChat bundle test.

## Verification evidence

- Backend Maven tests: 14 passed, 0 failed.
- Frontend Node tests: 11 passed, 0 failed.
- ESLint and Stylelint passed for the changed frontend source.
- `npm run verify:weapp` completed the production WeChat build and bundle compatibility check; unsupported optional chaining/nullish-coalescing syntax and localhost API leakage were absent.
- The H5 production build succeeded with the existing entrypoint-size warning.
- Local PostgreSQL migration and API integration covered create, carried snapshots, trend generation, update, delete, and cleanup.
- Visual H5 inspection was attempted through the required in-app browser workflow, but browser setup could not start because the Windows sandbox repeatedly returned `helper_unknown_error: setup refresh had errors`. No visual-browser pass is claimed.

## Production release

- Created `backups/calorie_calculator-before-weight-20260722.dump` with mode `600` before migration.
- Retained the old image as `calorie-calculator-backend:pre-weight-20260722`.
- Applied the non-destructive migration and deployed the merged backend.
- Verified `/healthz`, an empty snapshot, an empty trend, create, carry-forward, pre-first-record null points, and deletion through the public API.
- Deleted the temporary acceptance record and confirmed the weight table returned to an empty state.
- PostgreSQL container ID and creation timestamp remained unchanged; only the backend container was recreated. Nginx remained running.
- `/api/import/json` continued to return HTTP 404 through Nginx.
- The updated Mini Program package is built locally but still requires the user to upload it manually with WeChat DevTools.

## GitHub records

- Sprint: [CalorieCalculator - Weight Tracking Sprint (Project #4)](https://github.com/users/Hunter3304/projects/4)
- Backend Issues [#10](https://github.com/Hunter3304/CalorieCalculator-backend/issues/10) and [#11](https://github.com/Hunter3304/CalorieCalculator-backend/issues/11)
- Frontend Issues [#14](https://github.com/Hunter3304/CalorieCalculator-frontend/issues/14), [#15](https://github.com/Hunter3304/CalorieCalculator-frontend/issues/15), and [#17](https://github.com/Hunter3304/CalorieCalculator-frontend/issues/17)
- Backend Pull Request [#12](https://github.com/Hunter3304/CalorieCalculator-backend/pull/12)
- Frontend Pull Request [#16](https://github.com/Hunter3304/CalorieCalculator-frontend/pull/16)

## Problems and lessons for the next iteration

- GitHub and SSH calls can be slow while the VPN is connected. Use longer timeouts and verify remote state before retrying; slowness alone is not a failed operation.
- The Windows Codex sandbox intermittently failed even for read-only commands and browser setup. Retry only the required command with narrow approval and keep infrastructure failure separate from product failure.
- Remote JSON passed through PowerShell, SSH, and a POSIX shell can lose quotes. Prefer a direct HTTP client with a structured request body for production acceptance tests.
- A migration can succeed even when a chained verification command fails. Read command output and run an independent idempotent verification before deciding whether to rerun the migration.
- H5 and WeChat builds share `dist`; source tests must not depend on whichever bundle was built most recently. Keep source tests explicit and bundle tests attached to the corresponding build command.
- Preserve blank dates before the first real weight in the trend point array instead of dropping them; the renderer can skip null points while retaining correct horizontal time spacing.

## Remaining action

Upload the generated `dist` package through WeChat DevTools and validate the experience version on a real device. Formal release still requires the documented domain, ICP, HTTPS, and WeChat legal-domain work.
