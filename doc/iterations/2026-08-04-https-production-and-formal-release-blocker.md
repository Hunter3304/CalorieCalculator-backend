# HTTPS Production Rollout and Formal-Release Blocker

Date: 2026-08-04
Status: Completed

## Requested outcome

Move the Mini Program from the temporary HTTP IP endpoint to the ICP-filed domain, deploy trusted HTTPS, register the WeChat request domain, and determine whether the current experience version is ready for formal public review.

## Engineering delivery

- DNS `A` records for the apex, `www`, and `api` names resolve to the Tencent Cloud server.
- TCP 443 is allowed by the Lighthouse firewall.
- Trusted certificates cover `caloriecalculator.top`, `www.caloriecalculator.top`, and `api.caloriecalculator.top` and expire on 2026-11-02.
- Nginx serves the filed landing page on the root domain and proxies the API at `https://api.caloriecalculator.top/api`.
- HTTP domain traffic redirects to HTTPS, `/api/import/*` remains blocked, and ports 5432 and 8080 remain private.
- Backend Issue #21 and PR #22 delivered HTTPS deployment support. Frontend Issue #27 and PR #28 changed the production API origin and release documentation.
- The WeChat `request` legal domain is registered. Experience version `1.1.2` works on a physical device without Developer Debugging.

## Verification and rollback

Certificate SANs, validity, and private-key matches were checked before deployment. Certificate files are stored outside Git with restricted permissions. The Nginx configuration passed a one-shot validation before only the Nginx container was recreated; the backend and PostgreSQL container IDs did not change. Public checks returned 200 for the landing page, API health, and representative API requests, 301 for HTTP domain redirects, and 404 for the blocked import path. The previous Nginx configuration remains available for rollback, and raw-IP HTTP remains temporarily available for experience-version rollback.

## Problems encountered and solutions

- Windows sandbox setup failures prevented reliable browser automation and `apply_patch` execution. Authenticated Tencent/WeChat console actions were completed by the user; exact file edits were applied only after checking baselines and immediately verified with diffs and hashes.
- VPN latency made transfers and installs appear stalled. They were kept running and observed in bounded intervals instead of being cancelled prematurely.
- A GitHub merge appeared to run for a long time because automatic permission approval timed out before the command started. Checking the PR showed it was still open and mergeable; merging one PR at a time resolved it.
- PowerShell expanded remote shell syntax locally in one command. Later remote operations used explicit resolved paths or transferred scripts with simpler quoting.
- A nested remote cleanup command failed because of quoting. The temporary directory was resolved with `realpath`, compared with the expected safe parent, and removed only after the exact target was confirmed.
- PowerShell array matching produced a false-positive validation. Joining output lines before matching made the check evaluate the complete response.
- Certificate renewal in the Tencent console does not automatically redeploy a renewed certificate into self-managed Docker Nginx. Renewal and replacement must be scheduled before 2026-11-02.

## Formal-release decision

Do not submit version `1.1.2` for public review. Code inspection found no WeChat login, app session, authenticated user identity, or owner columns/filters. Personal records and custom foods are therefore globally shared. The next iteration must implement backend-only code exchange, authenticated sessions, server-derived owner scoping, non-destructive reassignment of existing production data to the original owner, privacy disclosure, account/data deletion, and two-user isolation acceptance before a new experience version can be reviewed.
