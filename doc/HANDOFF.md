# CalorieCalculator Engineering Handoff

Last updated: 2026-08-04 (Asia/Shanghai)

## 1. Purpose

Read this file before starting any new work on CalorieCalculator. It records the current local, GitHub, database, cloud deployment, and WeChat Mini Program state. Treat the repository status and user-owned uncommitted files documented below as important constraints.

## 2. Project layout

This is a frontend/backend-separated project with two independent Git repositories:

- Backend: `D:\AAA\develop\CalorieCalculator\CalorieCalculator-backend`
- Frontend: `D:\AAA\develop\CalorieCalculator\CalorieCalculator-frontend`
- Backend documentation: `D:\AAA\develop\CalorieCalculator\CalorieCalculator-backend\doc`
- Frontend documentation: `D:\AAA\develop\CalorieCalculator\CalorieCalculator-frontend\doc`
- Iteration feedback: `doc\iterations` in each repository
- Iteration plans: `doc\plans` in each repository
- The two versioned documentation copies are shared project records and must be kept synchronized whenever common documentation changes.

Git executable:

- `D:\AAA\app\Git\cmd\git.exe`

Node.js/npm:

- Node installation: `D:\AAA\app\NodeJS`
- Use `D:\AAA\app\NodeJS\npm.cmd` from PowerShell because execution policy blocks `npm.ps1`.

GitHub CLI:

- `D:\AAA\app\GitHubCLI\gh.exe`
- Authenticated GitHub account: `Hunter3304`

## 3. GitHub repositories

- Backend: `git@github.com:Hunter3304/CalorieCalculator-backend.git`
- Frontend: `git@github.com:Hunter3304/CalorieCalculator-frontend.git`

Required engineering workflow for future changes:

1. Diagnose and reproduce the problem.
2. Create a GitHub Issue before creating a branch.
3. Update local `main` from `origin/main`.
4. Create an Issue-linked feature/fix branch.
5. Implement maintainable, extensible, and testable changes.
6. Run proportionate unit tests, static checks, builds, and integration checks.
7. Push the branch and create a Pull Request.
8. Merge the Pull Request into `main`.
9. Confirm the Issue is closed.
10. Delete both the remote and local working branch.
11. Preserve unrelated user changes throughout.

## 4. Current Git state

### Frontend

At final weight-feature handoff preparation, `main` was clean and synchronized with `origin/main` at `232c8f0`. The final handoff documentation merge is expected to be later than this feature baseline.

The previous `project.config.json` and `project.private.config.json` modified status was caused only by Windows line-ending/index metadata. It was safely refreshed without changing file content. The frontend working tree was clean after cleanup.

### Backend

At final weight-feature handoff preparation, `main` was clean and synchronized with `origin/main` at `12a508c`. The final handoff documentation merge is expected to be later than this feature baseline.

IntelliJ `.idea` metadata is no longer tracked. Local IDE files remain on disk and are ignored by Git. The backend working tree was clean after cleanup.

Both repositories should contain only the local and remote `main` branch after this handoff workflow is merged and cleaned. Weight feature, release, README, and earlier documentation branches were deleted locally and remotely before final handoff preparation.

## 5. Backend

Technology:

- Spring Boot
- Java 21
- Maven Wrapper (`mvnw.cmd`)
- MyBatis
- PostgreSQL
- Default local backend port: `8080`

Local datasource password configuration intentionally retains a development fallback:

```properties
spring.datasource.password=${PGPASSWORD:123456}
```

Do not commit real passwords. The strong local PostgreSQL password is stored in the current Windows user's `PGPASSWORD` environment variable. Production secrets are stored only on the server.

Useful local commands:

```powershell
cd D:\AAA\develop\CalorieCalculator\CalorieCalculator-backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Local API example:

```text
http://localhost:8080/api/foods/page?page=1&size=10
```

## 6. Local PostgreSQL

- PostgreSQL version: 17.10
- Installation: `D:\AAA\app\PostgreSQL\17`
- Windows service: `postgresql-x64-17`
- Service startup: Automatic
- Database: `calorie_calculator`
- Database user: `postgres`
- Password: stored in the Windows user-level `PGPASSWORD`; never copy it into this file or Git.
- Food data count after import: 1799 rows
- Source food JSON files imported: 75 files / 1795 imported records, plus 4 seed records

pgAdmin was discussed as a desired database management tool; verify its actual installation state before assuming it is available.

## 7. Tencent Cloud production deployment

Server:

- Provider: Tencent Cloud Lighthouse
- Instance name: `calorie-calculator-prod`
- Region: Shanghai
- OS: Ubuntu 24.04.4 LTS
- Public IPv4: `124.221.90.240`
- SSH user: `ubuntu`
- SSH private key: `D:\AAA\keys\calorie_calculator_prod\calorie_calculator_prod.pem`

Never print or commit the SSH private key. Its Windows ACL has already been restricted.

Server application directory:

- `/opt/calorie-calculator`

Runtime architecture:

- Nginx container publishes port 80.
- Spring Boot backend is internal-only on port 8080.
- PostgreSQL 17 container is internal-only on port 5432.
- PostgreSQL data uses a persistent Docker named volume.
- Docker Engine and Docker Compose are enabled on boot.
- Server has approximately 2 CPU, 3.6 GiB RAM, and 2 GiB swap.

Production secret handling:

- Production database password is random and stored only in `/opt/calorie-calculator/.env.production` with mode 600.
- Never print, download, or commit the production password.

Deployment files in the backend repository:

- `Dockerfile`
- `.dockerignore`
- `.env.production.example`
- `docker-compose.production.yml`
- `maven-settings.xml`
- `deploy/nginx.conf`
- `deploy/import-foods.sh`
- `deploy/README.md`

Production checks last verified on 2026-07-22 after the calendar backend deployment:

- `http://124.221.90.240/healthz` returns HTTP 200.
- `http://124.221.90.240/api/foods/page?page=1&size=10` returns data.
- `http://124.221.90.240/api/records/calendar?month=2026-07` returns calendar metadata.
- Verified calendar bounds: `2026-07-21` through `2026-07-29`, with one recorded date at verification time.
- Backend and Nginx were recreated from the merged calendar source and returned healthy/running status.
- PostgreSQL was not recreated; the existing container and named data volume remained in place.
- Previous backend image retained as `calorie-calculator-backend:pre-calendar` for rollback.
- Production food count: 1799.
- Public ports 5432 and 8080 are not exposed.
- `/api/import/*` is blocked by Nginx.
- Container restart persistence was previously verified.

## 8. Frontend and WeChat Mini Program

Technology:

- Taro 4.2.0
- React 18
- WeChat Mini Program output: `dist/`
- AppID: `wx7ed730f3cde2c594`

Install/build/test commands:

```powershell
cd D:\AAA\develop\CalorieCalculator\CalorieCalculator-frontend
D:\AAA\app\NodeJS\npm.cmd install
D:\AAA\app\NodeJS\npm.cmd test
D:\AAA\app\NodeJS\npm.cmd run verify:weapp
```

`verify:weapp` builds the production package and verifies that the generated `dist/common.js` does not contain optional chaining (`?.`) or nullish coalescing (`??`), because the WeChat upload validator rejected those tokens.

Environment configuration:

- `.env.development` currently points to `http://124.221.90.240/api`.
- `.env.production` currently points to `http://124.221.90.240/api`.
- `src/services/api.js` retains `http://localhost:8080/api` only as a fallback when no environment value is injected.
- The generated production bundle was verified to use the Tencent Cloud API, not localhost.

Recent frontend fixes:

- Homepage no longer downloads the entire 1799-item food list unnecessarily.
- Confirming a food weight now immediately persists the daily record instead of silently adding it to a second-step cart.
- Save responses validate HTTP status codes.
- Duplicate save submissions are prevented while a request is in progress.
- Successful saves return to the homepage, whose `useDidShow` refreshes the daily summary.
- The obsolete `CartFooter` component was removed.
- Automated API response tests were added.
- WeChat bundle syntax compatibility tests were added.


Calendar system Sprint completed in code on 2026-07-21:

- GitHub Project: `CalorieCalculator - Calendar System Sprint` (Project #3); all four items are Done.
- Backend Issue #3 / Pull Request #4: calendar metadata, recorded-date markers, and authoritative date bounds (merged).
- Frontend Issues #5, #6, and #7 / Pull Request #8: homepage date navigation, natural-month picker, date-aware food entry, and verification (merged).
- Backend `main` synchronized at merge commit `86af670` or later.
- Frontend `main` synchronized at merge commit `d53ccd7` or later.
- The selectable lower bound is the later of the first recorded date and one year before today; it never moves later than today.
- The selectable upper bound is seven days after today, and future dates in that window accept planned food records.
- Empty selectable dates show zero totals and accept backfilled/planned records.
- Calendar months show recorded dates with a dark marker and the selected date with a distinct highlight.
- The selected date is explicitly passed to the add-food page and preserved when returning home.
- Stale-response guards prevent rapid date changes from showing data for a previously selected date.
- Backend tests: 6 passed.
- Frontend tests: 8 passed.
- `npm run verify:weapp` passed, and the production bundle contains no localhost API URL.

Deployment status for this Sprint:

- The merged backend calendar API was deployed to Tencent Cloud and verified on 2026-07-22.
- The newly built frontend package has not yet been uploaded as a new WeChat experience version; the user owns this manual upload step.

Relevant GitHub records:

- Issue #1: selected food not added to daily records (closed)
- Pull Request #2: daily record persistence fix (merged)
- Issue #3: WeChat uploader rejected optional chaining (closed)
- Pull Request #4: WeChat JavaScript compatibility fix (merged)

Backend repository cleanup records:

- Backend Issue #1: stop tracking IntelliJ IDEA metadata (closed)
- Backend Pull Request #2: IntelliJ metadata cleanup (merged)

Documentation refresh records:

- Backend Issue #5: document calendar API and production deployment (closed).
- Backend Pull Request #6: backend/deployment README refresh (merged).
- Frontend Issue #9: update Mini Program calendar and release guide (closed).
- Frontend Pull Request #10: frontend README refresh (merged).

## 9. WeChat test/experience version status

- Local WeChat DevTools testing works.
- The latest code can be uploaded successfully after the JavaScript compatibility fix.
- An experience version QR code was generated and tested successfully.
- Experience-version food loading initially appeared blank because the phone blocked the HTTP IP request.
- Enabling `Developer Debugging` in the experience version allowed the HTTP API and produced fast responses.

Important distinction:

- `project.private.config.json` disables URL checking only for local development tooling.
- Experience/development builds may be tested on approved accounts with developer debugging enabled.
- A formal public release must not depend on developer debugging.

## 10. Domain, HTTPS, and formal release blocker

The production API still uses plain HTTP and a raw IP address:

```text
http://124.221.90.240/api
```

This is acceptable only for current development/experience testing with debugging. Before formal public release:

1. Complete domain purchase and real-name verification.
2. Complete ICP filing for the domain because the server is in mainland China.
3. Create an API subdomain such as `api.<domain>` pointing to `124.221.90.240`.
4. Configure a valid HTTPS certificate and port 443 on the server.
5. Add `https://api.<domain>` as the WeChat `request` legal domain (no `/api` path in the platform domain entry).
6. Change `.env.production` to `https://api.<domain>/api`.
7. Build with `npm run verify:weapp`.
8. Test on a real device without developer debugging.
9. Upload a new version, submit for WeChat review, and publish after approval.

The user selected an inexpensive `.top` domain, but the full domain name has not been recorded in this conversation. Do not guess it. Ask the user for the exact registered domain and confirm real-name/ICP status before configuring DNS or TLS.

## 11. Security and operational follow-ups

Recommended future production hardening:

- Configure HTTPS as described above.
- Add scheduled PostgreSQL backups stored off the server.
- Add Docker log rotation limits.
- Apply regular Ubuntu security updates.
- Restrict SSH ingress to trusted source IPs when practical.
- Configure Tencent Cloud snapshots.
- Add monitoring/alerting for `/healthz`, disk usage, database health, and container restarts.

## 12. Immediate next-task checklist

Before changing code:

1. Read this entire file.
2. Inspect both repository statuses.
3. Confirm both repositories are still clean before starting; preserve any new user-owned changes that appear later.
4. Confirm whether the task affects frontend, backend, infrastructure, or more than one repository.
5. Create and maintain the iteration plan under `doc/plans` in every affected repository.
6. For code changes, create the GitHub Issue before creating the branch.
7. Never expose passwords, production .env contents, or SSH key material.
8. At the end of every feature, update the root README.md in every affected repository, then update the iteration record and synchronized handoff before closing the documentation workflow.

## 13. Workspace and documentation lessons

The documentation migration on 2026-07-22 exposed the following local workflow details. Use them to avoid repeating diagnosis work:

- The Windows Codex sandbox intermittently returned `helper_unknown_error: setup refresh had errors`, including for read-only `rg` commands and built-in patch operations. When this exact infrastructure error recurs, retry only the required command with narrowly scoped approval; it does not indicate a repository or application failure.
- The temporary `apply_patch.bat` wrapper was discoverable but its packaged WindowsApps executable returned `Access is denied`. Retry the built-in patch path first. If it remains unavailable, use an explicitly approved, exact-match edit that fails when expected text is absent, then inspect the Git diff immediately.
- PowerShell does not automatically stop a multi-command script when a native executable returns a nonzero status. Check `$LASTEXITCODE` after Git checks, tests, and builds when later commands must not continue after failure.
- In PowerShell, backtick newline sequences inside single-quoted `gh pr create --body` text remain literal. Use a here-string or body file for multiline PR descriptions, and always verify that `Closes #N` actually closed the Issue after merge.
- Run `git diff --check` before committing. During this migration it detected an extra blank line at the end of `HANDOFF.md`; normalize files to one final newline before commit.
- Shared project documentation now has versioned copies in both repositories. Update both copies for cross-project facts and compare their relative file lists and SHA-256 hashes before merging. Repository-specific documentation can remain local to the affected repository.
- The original `D:\AAA\develop\CalorieCalculator\doc` directory was retained as an unversioned migration backup. The repository copies are authoritative; do not update only the old top-level copy or use it as the sole handoff source.
- GitHub CLI is installed at `D:\AAA\app\GitHubCLI\gh.exe`. Elevated PowerShell sessions may not inherit the PATH entry, so use this absolute path instead of assuming `gh` resolves.
- Distinguish an approval timeout from a command timeout. If Codex reports that automatic permission review did not finish, the command did not start; do not diagnose it as GitHub, SSH, VPN, or application failure.
- For VPN-dependent GitHub, SSH, build, upload, and deployment work, split compound commands into short observable steps. Use a bounded timeout for each step and verify remote state before retrying.
- A long-lived `ssh.exe` process may belong to the user or another session. Do not terminate it merely because a new SSH command is slow.
- WeChat Developer Tools can retain a stale bundle or hot-reload state in which every button appears unresponsive. Recompile, clear cache, and reopen the project before changing application code. On 2026-07-27 the user confirmed the buttons worked after this refresh.

## 14. Weight Tracking Sprint and production state

The Weight Tracking Sprint was completed on 2026-07-22:

Status: feature scope complete and closed. Start later functionality with a new plan, Sprint/Project, and Issues rather than extending this completed Sprint implicitly.

- GitHub Project: `CalorieCalculator - Weight Tracking Sprint` (Project #4).
- Backend Issue #10 / Pull Request #12 delivered persistence, APIs, migration, and 14 passing backend tests.
- Frontend Issues #14 and #15 / Pull Request #16 delivered the homepage actions and card, editor, trend page, calendar, theme, and 11 passing frontend tests.
- `body_weight_records` stores one positive, one-decimal kilogram value per real record date.
- Snapshot lookup returns the selected date's real record or the most recent earlier record and exposes the real source date.
- Trend lookup preserves null dates before the first record and carries the latest real value through later missing dates.
- Production was backed up to `backups/calorie_calculator-before-weight-20260722.dump` and the old image was retained as `calorie-calculator-backend:pre-weight-20260722`.
- The migration and merged backend were deployed successfully. Public create, carry-forward, trend, delete, and cleanup acceptance checks passed.
- The temporary production test record was deleted; the new table was empty after verification.
- A later read-only check observed a new real record created after acceptance cleanup. It was treated as user data and left untouched; do not copy personal weight values into public Issues, PRs, or documentation.
- PostgreSQL container ID `d03bd2712c2e4c2192776c43c0f7e929684f40e2e0cb8af4e2b1d6f56e5293ea` and creation time `2026-07-20T08:20:57.166504568Z` were unchanged across deployment.
- The WeChat production build and compatibility check passed again from current `main` on 2026-07-27; `dist` was regenerated at approximately 15:36 Asia/Shanghai. Uploading or selecting an experience version remains a manual user-owned step, and no upload is claimed in this handoff.
- Production `/healthz` returned HTTP 200 during the 2026-07-27 upload-readiness check.
- The H5 build passed, but required in-app browser visual inspection could not start because of the documented Windows sandbox refresh failure; do not treat it as visually approved.

Additional lessons from this iteration:

- VPN-connected GitHub and SSH responses may be slow without being failed. Use longer timeouts, then inspect the authoritative remote state before retrying to avoid duplicate Issues, migrations, or deployments.
- JSON sent through PowerShell, SSH, and a remote POSIX shell can lose quoting. Prefer a direct HTTP client with a structured body for acceptance requests.
- A chained migration command can report `CREATE TABLE` and then fail in its verification segment. Verify independently before rerunning an idempotent migration.
- H5 and WeChat builds share `dist`. Source-level tests must target their source suites explicitly; the generated-bundle compatibility test belongs after the matching WeChat build.
- Trend APIs and renderers must retain null points before the first record so the horizontal date scale remains accurate.

## 15. Body Circumference Tracking Sprint and production state

The Body Circumference Tracking Sprint was completed on 2026-07-27:

- GitHub Project: [CalorieCalculator - Body Circumference Tracking Sprint (Project #5)](https://github.com/users/Hunter3304/projects/5).
- Backend Issue #18 / Pull Request #20 delivered persistence, migration, APIs, and tests. Merge commit: `d37aa28b7f6001e264a518d6f9411d2486a40269`.
- Frontend Issues #23 and #24 / Pull Request #25 delivered the homepage action and summary, sparse editor, selector-based trend page, and tests. Merge commit: `a86e8d3849ca2458416d6837fff4c7a5fa66fa62`.
- Backend Issue #19 tracks production and release documentation. Frontend Issue #26 records the public deployment, rollback, container, and WeChat release summary.
- Backend verification passed with 23 Maven tests and a clean package. Frontend verification passed with 16 source tests, ESLint, Stylelint, H5 build, and `verify:weapp`.
- A custom-format production database backup was created before migration, verified with `pg_restore --list`, and stored with mode 600.
- The previous backend image was tagged and retained for immediate rollback.
- The deployment archive checksum matched locally and remotely before extraction.
- The non-destructive migration succeeded and only the backend container was recreated.
- The PostgreSQL container and persistent data volume remained unchanged.
- The Nginx container remained unchanged.
- Only the backend container was recreated from the merged release.
- All three containers were healthy/running. Public ports 5432 and 8080 remained closed, `/api/import/json` remained blocked, and `/healthz` passed.
- Production API acceptance covered empty state, partial input, independent carry-forward, all-blank no-op, trend, clear fallback, delete, and cleanup. Temporary acceptance records were deleted.
- Existing user food, daily-record, weight, and circumference data must never be printed in public Issues or documentation.
- The user confirmed homepage buttons work in WeChat Developer Tools after refreshing its cached build. Uploading/selecting a new experience version remains manual; no upload or formal publication is claimed.

## 16. Baseline for the next feature

- Food, calendar, body-weight, and body-circumference functionality is complete across code, automated verification, production migration/deployment, and versioned documentation.
- Use the latest merged `main` heads after the release-documentation PRs as the next starting commits.
- Backend tests last passed: 23. Frontend source tests last passed: 16. WeChat bundle compatibility and H5 production build last passed on 2026-07-27.
- The latest generated `dist` is suitable for manual WeChat Developer Tools testing/upload. A future frontend change must rerun `npm run verify:weapp`.
- Current experience testing still uses `http://124.221.90.240/api` and requires Developer Debugging. Formal release remains blocked by the domain, ICP, HTTPS, and WeChat legal-domain tasks in section 10.
- Preserve all existing production food, daily-record, body-weight, and body-circumference data. Never rerun destructive `schema.sql` against an existing database; use a dedicated non-destructive migration.
- Before implementing another feature, read this handoff, inspect both repository statuses, create the synchronized plan, create the Project/Issues, and follow the Issue-branch-PR-merge-cleanup workflow.
- At feature completion, update both affected root README files and the synchronized iteration/handoff records. Verify shared relative file lists and SHA-256 hashes before merging.
