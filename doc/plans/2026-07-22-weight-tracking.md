# Weight Tracking and Trend Sprint

Date: 2026-07-22
Status: Planned

## Requested outcome

Add a maintainable body-weight module alongside daily food tracking. Users can record, edit, and delete one weight entry per date, see the selected date's recorded or carried-forward weight on the homepage, and inspect 7-day, 30-day, or 365-day trends.

## Product rules

- Weight belongs to the date currently selected on the homepage.
- Historical dates can be backfilled; future dates cannot be recorded.
- A future-date record action remains tappable but explains why recording is unavailable.
- Each date has at most one real weight record.
- Weight is stored in kilograms, must be greater than zero, and accepts at most one decimal place. No additional range restriction is imposed.
- A date without a real record displays the most recent earlier weight. Before the first record, no weight is displayed.
- Editing a carried-forward value edits its actual source record and clearly shows that record's date. Delete is shown only when the selected date has a real record.
- Deleting a record causes later unrecorded dates to carry forward from the next earlier real record until another record is reached.
- Trend presets are rolling 7, 30, and 365 calendar days. The end date defaults to the selected homepage date but is capped at today.
- The trend page end date can be changed with a calendar. Dates before the first weight record and after today are disabled.
- Trend points carry the last real value forward across missing dates; dates before the first real record remain empty.

## Backend design

- Add a dedicated `body_weight_records` table with a unique record date, one-decimal numeric weight, timestamps, and a positive-value constraint.
- Add a non-destructive SQL migration for existing local and production databases; do not rely on the destructive food catalog initialization script.
- Keep the module separated into weight DTOs, mapper, service, and controller.
- Provide APIs to resolve the effective weight for a selected date, create or replace a date's record, update and delete a real record, fetch metadata, and return raw real records for a requested trend interval.
- Reject future dates, non-positive weights, and values with more than one decimal place.
- Keep forward-fill trend construction testable as service or frontend pure logic without coupling it to rendering.

## Frontend design

- Replace the single add-food button with two equal circular icon-and-label actions for adding food and recording weight.
- Apply a shared pale-green and white visual theme while preserving readable contrast and existing calendar states.
- Add a reusable homepage `WeightSummaryCard` below the eaten-food list. It shows real, carried-forward, or empty state and routes to the trend page.
- Add an independent weight editor page for creating and editing records, including source-date disclosure when editing a carried-forward record.
- Add an independent trend page with 7/30/365-day presets, selectable end-date calendar, empty state, and a reusable line-chart component.
- Keep API calls, trend date construction, presentation components, and page orchestration in separate files.
- Guard page refreshes and submissions against stale responses and duplicate saves.

## Verification

- Backend unit tests: date validation, precision validation, upsert semantics, effective/source record lookup, deletion, metadata, interval queries, and missing-day behavior.
- Frontend unit tests: rolling date ranges, end-date capping, forward filling, first-record blanks, and API response handling.
- Run `mvnw.cmd test`, `npm.cmd test`, and `npm.cmd run verify:weapp`.
- Confirm the production bundle has no unsupported optional chaining/nullish coalescing and no localhost API URL.
- Verify Markdown links, `git diff --check`, and synchronization of shared documentation.

## Delivery workflow

1. Create a GitHub Sprint/Project and cross-repository Issues.
2. Create Issue-linked backend and frontend branches from current `main`.
3. Implement and verify backend, frontend, database migration, and documentation.
4. Open and merge Pull Requests, close all Sprint items, and delete feature branches.
5. Deploy the merged backend and non-destructive migration to Tencent Cloud with rollback protection.
6. Build the frontend package; Mini Program upload remains a manual user-owned step.

## GitHub records

- Sprint: [CalorieCalculator - Weight Tracking Sprint (Project #4)](https://github.com/users/Hunter3304/projects/4)
- Backend Issue #10: body weight persistence, APIs, migration, and tests.
- Frontend Issue #14: homepage card, editor, actions, and visual theme.
- Frontend Issue #15: trend chart, presets, calendar, and trend tests.
- Backend Issue #11: production migration, deployment, and verification.
