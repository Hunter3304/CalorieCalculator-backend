# WeChat Authentication, User Isolation, and Privacy Controls

Date: 2026-08-05
Status: Implementation verified locally; Pull Requests and production rollout pending
Repositories: backend and frontend

## Requested outcome

Remove the final code-level blockers before public Mini Program review: authenticate through WeChat without exposing the AppSecret, isolate every user's personal data, preserve the existing production owner's records, disclose stored data, and provide logout plus permanent account/data deletion.

## Product and security rules

- The client sends only a short-lived `Taro.login()` code to the backend and stores only the returned opaque application token.
- AppSecret, OpenID, raw session tokens, and token hashes must never be committed or logged. OpenID remains server-side only.
- Every personal request derives the owner from the validated Bearer session. No client-provided user ID is trusted.
- Public foods remain shared. Custom foods, food usage ordering, daily records, weight, and circumference remain private to their owner.
- Cross-user object access behaves like a missing object. Account deletion is explicit, confirmed, and transactional.

## Backend changes

- Added server-side WeChat `code2Session`, `app_users`, SHA-256-hashed expiring sessions, a global `/api/**` authentication filter, logout, account metadata, and permanent account deletion.
- Added `owner_user_id` to every personal table and rewrote personal CRUD, calendar, carry-forward, trend, custom-food, and last-used-food queries to include the authenticated owner.
- Added an additive production migration that assigns existing rows to one migration-only legacy owner, plus a guarded one-real-user claim transaction.
- Added server environment placeholders for AppID/AppSecret/session lifetime and changed the container health check to unauthenticated `/healthz`.

## Frontend changes

- Added single-flight automatic login, opaque-token storage, Bearer attachment, and exactly one re-login/retry after HTTP 401.
- Routed all backend calls through the authenticated request wrapper.
- Added a Privacy & Account page linked from the home page, with stored-data disclosure, logout, destructive confirmation, and account/data deletion.
- Kept production API traffic on `https://api.caloriecalculator.top/api`.

## Verification evidence

- Backend: 33 tests passed with zero failures; `mvnw.cmd clean package` produced the Spring Boot JAR.
- Frontend: 19 tests passed; targeted ESLint and Stylelint passed; `npm run verify:weapp` passed; `npm run build:h5` passed with the existing 337 KiB entrypoint-size warning.
- The WeChat bundle contains no unsupported optional-chaining/nullish-coalescing tokens.
- SQL inspection found authenticated owner predicates on every personal read/write/update/delete path.
- Disposable PostgreSQL acceptance loaded the `origin/main` legacy schema, preserved daily/custom-food/weight/circumference rows through migration and one-user claim, allowed two owners to use colliding dates, and confirmed the claim rejects both zero and two real users. Both temporary databases were removed.
- No production migration, production deployment, real WeChat login, visual-device approval, or physical two-user acceptance is claimed in this iteration record yet.

## GitHub tracking

- Backend Issue [#24](https://github.com/Hunter3304/CalorieCalculator-backend/issues/24).
- Frontend Issue [#30](https://github.com/Hunter3304/CalorieCalculator-frontend/issues/30).
- Pull Request links will be added after the verified branches are pushed.

## Deployment and release status

Experience version `1.1.2` remains tester-only and must not be submitted. The new implementation still requires reviewed merges, a verified PostgreSQL backup, additive migration, private AppSecret entry, authenticated backend deployment, a new experience upload, original-owner first login and guarded legacy claim, and two-account physical isolation acceptance.

## Remaining actions

1. Push both branches, create and review the Pull Requests, and merge them only after approval.
2. During a controlled maintenance window, verify the production backup and rollback artifacts, then apply the already rehearsed additive migration.
3. Have the original owner log in first, claim legacy rows, verify counts and original data, then invite a second account.
4. Test colliding dates, different values, cross-user updates/deletes, carry-forward, trends, logout, and deletion on physical devices.
5. Complete the WeChat privacy-protection guide, upload the accepted build, submit formal review, and publish only after approval.
