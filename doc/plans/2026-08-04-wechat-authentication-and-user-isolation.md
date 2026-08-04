# WeChat Authentication and User Isolation Plan

Date: 2026-08-04
Status: In progress
Repositories: backend and frontend
GitHub Issues: pending creation after external-push authorization

## Release gate

Experience version `1.1.2` must not be submitted for public review. Formal review can begin only after every API request has an authenticated server-derived user, all personal reads and writes are owner-scoped, existing production data belongs to the original owner, account/data deletion works, and two different WeChat users pass isolation acceptance.

## Security rules

- The Mini Program obtains a short-lived code with `Taro.login()` and sends only that code to the backend.
- The backend alone stores the AppID/AppSecret and exchanges the code through WeChat `code2Session`; codes, AppSecret, OpenID, session tokens, and token hashes are never logged or committed.
- The backend returns a cryptographically random opaque token. Only its SHA-256 hash is stored, with an expiry, and the raw token is sent once.
- Protected APIs derive the current user from `Authorization: Bearer <token>`. No owner/user ID from request bodies, paths, or query strings is trusted.
- Missing, invalid, revoked, or expired sessions return 401. Authenticated access to another user's object behaves as 404 to avoid revealing its existence.
- Public food catalog rows remain shared and read-only. Custom food rows are private to their owner.

## Backend implementation

1. Add `app_users` and `app_sessions`; make OpenID unique, make token hashes unique, and index session expiry/user ownership.
2. Add `owner_user_id` to `daily_records`, `body_weight_records`, `body_circumference_records`, and custom `food_items`. Replace date-only unique constraints with `(owner_user_id, record_date)` for weight and circumference.
3. Preserve existing rows by assigning them to a migration-only legacy owner. Public foods keep no owner; custom foods receive the legacy owner. Add foreign keys, owner/date indexes, and a check that custom foods have an owner while public foods do not.
4. Add `POST /api/auth/wechat`, session lookup/revocation, `DELETE /api/auth/session`, `GET /api/account`, and transactional `DELETE /api/account`.
5. Add a request authentication filter and current-user accessor. Exempt only the login endpoint; Nginx health remains outside `/api`.
6. Thread the authenticated user ID through controllers, services, and every mapper. Scope reads, inserts, updates, deletes, calendar boundaries, carry-forward snapshots, and trends by owner.
7. Validate that a daily record may reference a public food or the current user's custom food. Scope custom-food list/create/update/delete and last-used ordering without allowing one user to mutate another user's catalog entry.
8. Add a guarded production-only legacy-claim SQL script. It must succeed only when exactly one real user exists, move every legacy-owned row to that user in one transaction, verify row counts, and remove the migration-only owner.
9. Read `WECHAT_APP_ID`, `WECHAT_APP_SECRET`, and session lifetime from production environment variables. Refuse startup when production login credentials are missing; provide a fake exchanger only in tests.

## Frontend implementation

1. Add a single-flight auth service that calls `Taro.login()`, exchanges the code, stores the opaque token, and never persists a WeChat code or OpenID.
2. Route all requests through one wrapper that adds the Bearer token, performs one controlled re-login/retry on 401, and surfaces a stable error after the retry limit.
3. Make startup wait for authentication before personal data loads. Keep page loading/error states usable during login and network recovery.
4. Add a Settings/Privacy page describing collected diet, weight, circumference, custom-food, and account-identifier data; link it from the home page.
5. Add logout and account/personal-data deletion. Deletion requires an explicit destructive confirmation, clears the local token only after the server response, and returns to a clean signed-out state.
6. Update the WeChat privacy-protection guide in the Mini Program console before formal review; console submission remains a user-owned authenticated action.

## Tests and acceptance

- Backend unit/integration tests cover code exchange errors, token hashing/expiry/revocation, 401 behavior, account deletion, legacy claim guards, owner-scoped CRUD, calendar, carry-forward, trends, and custom-food access.
- Every update/delete-by-ID test includes an attempted cross-user access. Every date query is exercised with colliding dates for two users.
- Frontend tests cover single-flight login, token attachment, one-time 401 retry, failed login, logout, deletion confirmation, and startup gating.
- Existing backend regression tests, `clean package`, frontend source tests, lint/style checks, H5 build, and `verify:weapp` must pass.
- Database migration is tested against a copy containing legacy daily, custom-food, weight, and circumference rows; pre/post row counts must match.
- Physical-device acceptance uses two different WeChat accounts with the same dates and different values. Neither account may list, read, update, delete, carry forward, trend, or summarize the other's data.

## Production rollout and rollback

1. Obtain the Mini Program AppSecret privately and place it only in the server environment. Do not send it in chat or commit it.
2. Back up PostgreSQL, verify the backup listing, retain the current backend image/configuration, and record non-sensitive rollback references outside public docs.
3. Apply the additive migration and deploy the authenticated backend during a short maintenance window; version `1.1.2` will receive 401 until the new client is installed.
4. Build and upload a new experience version, then have the original owner log in before any second tester.
5. Run the guarded legacy-claim transaction, compare table counts, and verify the original records in the new client.
6. Run two-user isolation acceptance, public HTTPS/import-block/private-port regression, and container health checks.
7. If acceptance fails, stop review, restore the retained backend/configuration and database backup as appropriate, and keep the current public release closed. Never roll back only code across an incompatible migrated schema without checking compatibility.
8. After success, update both READMEs, synchronized HANDOFF/iteration records, close Issues/PRs, upload the final experience build, and only then submit it for formal review.

## Known external/manual gates

- GitHub pushes, Issues, and PRs require explicit confirmation that the repositories are approved destinations for the deployment documentation.
- The AppSecret must be entered privately by the user on the server.
- The original owner must perform the first authenticated Mini Program login before legacy data is claimed.
- Privacy-guide configuration, experience upload, formal review submission, and final publish require the user's authenticated WeChat console/Developer Tools session.
