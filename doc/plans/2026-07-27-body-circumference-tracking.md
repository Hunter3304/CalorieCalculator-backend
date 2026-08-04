# Body Circumference Tracking Sprint

Date: 2026-07-27
Status: Completed — feature merged, production verified, and release documentation finalized

## Requested outcome

Add a body-circumference module alongside food and body-weight tracking. Users can record six circumference measurements for the date selected on the homepage, see the latest effective values, and inspect the trend of one selected measurement.

The six supported measurements are:

- Chest circumference
- Waist circumference
- Hip circumference
- Arm circumference
- Thigh circumference
- Calf circumference

All values use centimeters.

## Scope and affected repositories

### Backend

- Add non-destructive PostgreSQL persistence for dated circumference records.
- Add circumference DTOs, entity, mapper, service, controller, validation, snapshot lookup, update/delete behavior, and trend APIs.
- Add focused backend unit tests and a production-safe migration.

### Frontend

- Add a third homepage quick action using the existing green circular visual style.
- Add an independent circumference editor with six inputs.
- Add a homepage circumference summary/trend card.
- Add an independent circumference trend page with a measurement dropdown and the existing 7-, 30-, and 365-day range patterns.
- Add API functions, pure trend/input helpers, reusable components, styling, and source-level tests.

### Shared project records

- Keep the plan, iteration feedback, documentation index, and engineering handoff synchronized in both repositories.

## Product rules

- Circumference belongs to the date selected on the homepage.
- Future dates cannot receive circumference records.
- Each date has at most one sparse circumference record.
- The six fields are independent and optional. A save may contain any subset of them.
- Every supplied value is measured in centimeters, must be greater than zero, and supports at most one decimal place.
- A blank field means “do not provide a new value for this measurement”; it inherits the latest earlier non-null value of the same measurement.
- The six measurements are resolved independently. Their effective values and source dates may therefore come from different historical records.
- Submitting all six fields blank does not create an empty database row. It is treated as a successful no-op and leaves the currently inherited values unchanged.
- Before a measurement has any real value in history, its effective value is absent and the UI displays `--`.
- The homepage summary displays all six effective values or `--` for measurements that have never been recorded.
- Saving values for a date creates or updates that date's sparse override without modifying older source records. This avoids unexpectedly changing several historical dates when inherited fields have different sources.
- Editing an existing selected-date record updates its explicitly stored fields. Clearing an explicit field removes that date's override for the field and makes it fall back to the next earlier value.
- Deleting a selected-date circumference record removes that date's complete sparse override. Later dates then resolve each measurement from the next earlier non-null value.
- Trend presets cover rolling 7, 30, and 365 calendar days and cannot end after today.
- The trend page contains a dropdown for the six measurements and shows only the selected measurement. Waist circumference is the default.
- Trend points preserve blank dates before the first real value for the selected measurement and carry its latest real value through later missing dates.
- Real trend markers are field-specific: a date is marked real only when that date explicitly stores the selected measurement.

## Deliberate differences from body-weight behavior

Body weight has one value and therefore one unambiguous carried source record. Circumference has six independently optional values, so one effective snapshot may have several source dates. For that reason:

- Circumference snapshots expose a source date per measurement rather than one source date for the whole class.
- Editing from a carried snapshot creates selected-date overrides instead of mutating multiple older source records.
- Trend carry-forward and real-record markers are calculated independently for the selected measurement.

## Planned backend design

### Database

- Add a `body_circumference_records` table through a dedicated idempotent migration.
- Use one unique `record_date` per row.
- Add nullable one-decimal numeric columns:
  - `chest_cm`
  - `waist_cm`
  - `hip_cm`
  - `arm_cm`
  - `thigh_cm`
  - `calf_cm`
- Add positive-value checks for every non-null measurement.
- Require at least one non-null measurement per persisted row.
- Include `created_at` and `updated_at` timestamps plus a date index.
- Add the same table definition to `schema.sql` for fresh installations without using that destructive script on existing databases.

### API model

- Use a stable measurement key enum or allowlist shared by validation and trend selection.
- Return an effective snapshot containing:
  - selected date
  - selected-date record ID when present
  - effective value for every measurement
  - source date for every measurement
  - whether every measurement is explicitly recorded on the selected date
  - first real date per measurement where needed
- Accept nullable/omitted measurement fields in create/update requests.
- Distinguish omitted fields from explicit clearing when updating an existing sparse record.

### Proposed endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/circumferences/{yyyy-MM-dd}` | Resolve all six effective values for the selected date |
| `PUT` | `/api/circumferences/{yyyy-MM-dd}` | Create or update the selected date's sparse overrides |
| `DELETE` | `/api/circumferences/records/{id}` | Delete the complete selected-date sparse record |
| `GET` | `/api/circumferences/trend?type=waist&startDate=...&endDate=...` | Return one measurement's daily effective trend |

The final request shape must make update intent explicit so an omitted field means unchanged while an explicitly cleared field removes the selected-date override.

### Service logic

- Validate dates, supported measurement keys, positive values, and one-decimal precision.
- Resolve each effective measurement with its own latest-on-or-before lookup.
- Upsert only the explicitly submitted selected-date fields.
- Remove the row when an update clears its last remaining explicit value.
- Generate daily trend points for only the requested measurement, including null points before its first value and carried points afterward.
- Cap trend intervals at 365 days and reject future end dates.
- Return 404 for missing real records where record-ID operations require one.

## Planned frontend design

### Homepage

- Keep the existing selected-date orchestration and stale-response protection.
- Add a third green circular action labeled `记录围度`.
- Fetch circumference snapshots together with daily food and weight data.
- Add a reusable circumference card below the weight card and before the nutrition summary.
- Display a compact six-item grid with `cm` values or `--`.
- Provide selected-date edit/delete actions and an entry to the trend page.
- Preserve the existing food, calendar, and weight behavior.

### Circumference editor

- Add a separate page registered in `app.config.js`.
- Show the selected record date and six clearly labeled decimal inputs.
- Show inherited values as contextual hints without treating them as newly entered values.
- Allow any subset of fields and allow all fields to remain blank.
- Prevent duplicate submissions and validate nonblank values before sending.
- When editing a real selected-date record, allow an explicit stored value to be cleared so that measurement resumes inheriting.
- Return to the homepage after a successful save/no-op and refresh through `useDidShow`.

### Trend page

- Add a separate page registered in `app.config.js`.
- Default the dropdown to waist circumference.
- Offer all six measurement types using user-facing Chinese labels and stable API keys.
- Reuse the rolling 7-, 30-, and 365-day date logic and end-date calendar behavior.
- Draw one line at a time and distinguish real measurement points from carried values.
- Display `--`/empty state when the selected measurement has never been recorded.
- Guard rapid dropdown, preset, and date changes against stale responses.

### Frontend organization

- Keep request functions in `src/services/api.js`.
- Keep measurement definitions, validation, range construction, and normalization in a pure utility module.
- Keep summary and chart rendering in reusable components.
- Keep page components focused on orchestration, navigation, and user feedback.
- Avoid optional chaining and nullish coalescing in generated WeChat code.

## Test and verification strategy

### Backend tests

- Accept one, several, or all six valid values.
- Reject non-positive values and values with more than one decimal place.
- Reject future record dates.
- Treat an all-blank create as a no-op and do not persist an empty row.
- Resolve different measurements from different source dates.
- Override only submitted fields on the selected date.
- Clear one selected-date override and fall back to an earlier value.
- Delete a sparse date record and recalculate effective values.
- Remove a row after its last explicit value is cleared.
- Generate field-specific 7-, 30-, and 365-day trends.
- Preserve null points before the selected measurement's first real value.
- Carry forward missing dates and mark only field-specific real points.
- Reject unsupported measurement types, reversed ranges, future end dates, and ranges over 365 days.

### Frontend tests

- Validate positive one-decimal centimeter inputs while accepting blanks.
- Normalize all six snapshot values safely.
- Map dropdown labels to stable API keys.
- Build rolling trend ranges and cap future end dates.
- Preserve null trend points and normalize numeric response values.
- Verify sparse save payload construction, including explicit clearing during edit.
- Verify successful and failed API response handling.

### Commands

```powershell
cd D:\AAA\develop\CalorieCalculator\CalorieCalculator-backend
.\mvnw.cmd test
.\mvnw.cmd clean package

cd D:\AAA\develop\CalorieCalculator\CalorieCalculator-frontend
D:\AAA\app\NodeJS\npm.cmd test
D:\AAA\app\NodeJS\npm.cmd run verify:weapp
D:\AAA\app\NodeJS\npm.cmd run build:h5
```

Also run changed-source ESLint/Stylelint checks, `git diff --check`, and visual QA of the homepage, editor, dropdown, empty states, partial records, and trend chart.

## Data safety, deployment, and rollback

- Never run the destructive `schema.sql` against an existing local or production database.
- Apply a dedicated non-destructive migration locally before integration testing.
- Preserve all food, daily-record, body-weight, and existing user data.
- Before production migration, create and verify a PostgreSQL backup without printing secrets or personal measurements.
- Retain the previous backend image for rollback.
- Recreate only the backend container unless another component genuinely requires a change.
- Verify health, empty state, partial create, independent inheritance, field-specific trend, clearing, deletion, and cleanup through the deployed API.
- Do not copy personal circumference values into Issues, PRs, logs, or documentation.
- Regenerate the WeChat `dist` package with `verify:weapp`; Mini Program upload remains a manual user-owned action.
- Formal release remains subject to the existing domain, ICP, HTTPS, and WeChat legal-domain requirements.

## Delivery workflow

1. Review and approve this plan.
2. Create a new GitHub Project/Sprint for circumference tracking.
3. Create cross-repository Issues before creating implementation branches.
4. Update both local `main` branches from their remotes.
5. Create Issue-linked backend and frontend branches.
6. Implement the backend migration, model, APIs, service rules, and tests.
7. Implement the frontend action, editor, summary card, dropdown trend page, styling, and tests.
8. Run unit tests, static checks, production builds, bundle compatibility checks, integration checks, and visual QA.
9. Push branches, open PRs, review, merge into `main`, and confirm Issues close.
10. Delete local and remote feature branches.
11. Back up and migrate production, deploy the merged backend, and run acceptance checks.
12. Update both documentation copies, create the iteration feedback record, and refresh the handoff.

## GitHub records

- Project/Sprint: [CalorieCalculator - Body Circumference Tracking Sprint (Project #5)](https://github.com/users/Hunter3304/projects/5)
- Backend feature: [Issue #18](https://github.com/Hunter3304/CalorieCalculator-backend/issues/18)
- Frontend homepage/editor: [Issue #23](https://github.com/Hunter3304/CalorieCalculator-frontend/issues/23)
- Frontend trend: [Issue #24](https://github.com/Hunter3304/CalorieCalculator-frontend/issues/24)
- Backend production release: [Issue #19](https://github.com/Hunter3304/CalorieCalculator-backend/issues/19)
- Backend feature: [Pull Request #20](https://github.com/Hunter3304/CalorieCalculator-backend/pull/20) (merged)
- Frontend feature: [Pull Request #25](https://github.com/Hunter3304/CalorieCalculator-frontend/pull/25) (merged)
- Frontend release documentation: [Issue #26](https://github.com/Hunter3304/CalorieCalculator-frontend/issues/26)

## Plan changes

- Initial plan created from the user-confirmed six-measurement scope on 2026-07-27.
- Partial and all-blank input behavior is explicitly modeled as independent per-measurement inheritance.
- Local implementation now passes 23 backend tests, 16 frontend source tests, WeChat compatibility verification, H5 compilation, and real local API acceptance. In-app visual browser setup remains blocked by the documented Windows sandbox refresh failure.
- Production backup, migration, backend-only container recreation, API acceptance, regression checks, and cleanup completed on 2026-07-27.
- The user confirmed all homepage buttons work in WeChat Developer Tools after clearing/reopening the cached build; no application code change was required for that transient tooling state.
