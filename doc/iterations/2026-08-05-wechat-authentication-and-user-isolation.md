# WeChat Authentication, User Isolation, and Privacy Controls

Date: 2026-08-05
Status: Production rollout, original-owner claim, and single-account regression complete; two-account acceptance and formal review pending
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
- Backend PR #25 and frontend PR #31 were merged. Production backup, additive migration, private credential configuration, authenticated deployment, real WeChat login, and guarded legacy claim are complete.
- Backend login hotfix Issue #26 / PR #27 resolved a production Java 21 `UnknownContentTypeException` caused by WeChat's non-standard JSON content type. The fix passed 35 tests and isolated production-network candidate validation before deployment.
- A fresh 2026-08-11 pre-claim backup passed `pg_restore --list`; the claim transaction moved every legacy-owned row to the sole real user and removed the legacy owner. Aggregate counts matched after the transaction.
- Production audit found all personal owner columns populated, public foods unowned, owner-aware unique indexes and foreign keys present, and every stored session token represented only by a 64-character SHA-256 hash.
- The original owner confirmed calendar and circumference save behavior. A transient WeChat DevTools `dist/app.json` launch failure occurred while Taro was rebuilding `dist`; waiting for the build, verifying `miniprogramRoot` as `dist/`, and recompiling/reopening the project resolved it without a code change.
- Current verification: 35 backend tests, 19 frontend tests, targeted ESLint/Stylelint, `verify:weapp`, HTTPS health/landing checks, unauthenticated 401, import blocking, and closed public 5432/8080 all passed.
- Physical two-real-account isolation, privacy-guide completion, formal review, and publication are not yet claimed.

## GitHub tracking

- Backend Issue [#24](https://github.com/Hunter3304/CalorieCalculator-backend/issues/24).
- Frontend Issue [#30](https://github.com/Hunter3304/CalorieCalculator-frontend/issues/30).
- Backend Pull Request [#25](https://github.com/Hunter3304/CalorieCalculator-backend/pull/25).
- Frontend Pull Request [#31](https://github.com/Hunter3304/CalorieCalculator-frontend/pull/31).

## Deployment and release status

The pre-authentication experience version `1.1.2` remains unsuitable for review. The authenticated build, production backend, migration, original-owner login, and legacy claim are complete. Public review remains gated only by two-real-account physical isolation/account-deletion acceptance, WeChat privacy-guide completion, final device regression, and submission of the accepted authenticated build.

## Remaining actions

1. Use a second real WeChat account to confirm empty/private initial state, colliding dates with different values, read/update/delete/carry-forward/trend isolation, logout, and permanent deletion of the second test account.
2. Complete the WeChat privacy-protection guide and final physical-device regression without Developer Debugging.
3. Submit only the authenticated accepted build for formal review, then publish after approval.
4. Merge the release-documentation PRs for backend Issue #28 and frontend Issue #32, then delete their working branches.
