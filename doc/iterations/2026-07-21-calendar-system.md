# Calendar Navigation and Date-Aware Records

Date: 2026-07-21

## Feedback and requested outcome

The homepage needed date navigation instead of being limited to today. The requested interaction was:

- Previous-day and next-day buttons beside the date.
- Disabled navigation at the earliest and latest allowed dates.
- A tappable date that opens a complete natural-month calendar.
- Month selection/navigation inside the calendar.
- A distinct selected-date state.
- A dark marker for dates that contain food records.
- Summary, consumed-food list, and add-food behavior synchronized to the selected date.
- Historical backfill and future meal planning.

## Agreed product rules

- The lower bound is the later of the first recorded date and one year before today.
- The lower bound never moves later than today, even when the first saved item is a future plan.
- The upper bound is seven days after today.
- Empty dates inside the range are selectable and can receive records.
- Adding food saves to the selected date rather than implicitly saving to today.
- The calendar renders complete natural months of 28, 29, 30, or 31 days.

## Delivered

Backend:

- Added `GET /api/records/calendar?month=yyyy-MM`.
- Added `minDate`, `maxDate`, and monthly `recordedDates` metadata.
- Added record-date validation to `POST /api/records`.
- Added boundary, empty-database, and future-first-record tests.

Frontend:

- Added previous/next day controls and disabled boundary states.
- Added a bottom-sheet natural-month picker and month selector.
- Added selected-date highlighting and recorded-date markers.
- Made daily records, nutrition totals, and empty states date-aware.
- Passed the selected date explicitly to add-food.
- Preserved the selected date when returning home.
- Added stale-response guards for rapid navigation.
- Added date arithmetic, leap-year, month-layout, and range tests.

## Verification

- Backend: 6 tests passed.
- Frontend: 8 tests passed.
- `npm run verify:weapp` passed.
- Production bundle contained no localhost API URL.

## GitHub records

- Project: `CalorieCalculator - Calendar System Sprint` (#3), all items Done.
- Backend Issue #3 / Pull Request #4 — closed and merged.
- Frontend Issues #5, #6, and #7 / Pull Request #8 — closed and merged.

## Outcome and follow-ups

The code was merged to both `main` branches. At the end of this iteration, backend deployment and experience-version upload were intentionally left for the next release iteration.

Canonical current state: [`../HANDOFF.md`](../HANDOFF.md).
