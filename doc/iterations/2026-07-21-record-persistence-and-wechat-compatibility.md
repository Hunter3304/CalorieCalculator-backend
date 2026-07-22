# Daily-Record Persistence and WeChat Compatibility

Date: 2026-07-21

## Feedback and requested outcome

Two user-visible problems were recorded:

1. Confirming a food and weight did not immediately add the item to the daily record.
2. The WeChat uploader rejected optional-chaining and nullish-coalescing tokens in the generated bundle.

The requested outcome was a direct, reliable save flow and an uploadable WeChat production package.

## Delivered

- Replaced the obsolete two-step cart behavior with immediate daily-record persistence.
- Validated HTTP response status codes for save operations.
- Prevented duplicate submissions while a save request is in progress.
- Returned to the homepage after a successful save.
- Refreshed the daily summary through `useDidShow` when the homepage becomes visible.
- Removed the obsolete `CartFooter` component.
- Added API response tests.
- Added a production-bundle compatibility test for `?.` and `??` tokens.
- Added `verify:weapp` to build and validate the WeChat package in one command.

## Verification

- Selected foods persisted to daily records.
- Duplicate confirmation taps did not create duplicate requests.
- Homepage totals refreshed after returning from add-food.
- The WeChat uploader accepted the generated package after the compatibility fix.
- The production bundle used the Tencent Cloud API rather than localhost.

## GitHub records

Frontend repository:

- Issue #1: selected food not added to daily records — closed.
- Pull Request #2: daily-record persistence fix — merged.
- Issue #3: WeChat uploader rejected optional chaining — closed.
- Pull Request #4: JavaScript compatibility fix — merged.

## Outcome and follow-ups

The direct-save experience and build compatibility were fixed. Calendar-based historical viewing and date-aware entry remained a later iteration.

Canonical current state: [`../HANDOFF.md`](../HANDOFF.md).
