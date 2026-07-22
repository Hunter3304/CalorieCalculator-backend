# Calendar Production Deployment and Documentation Refresh

Date: 2026-07-22

## Feedback and requested outcome

While domain real-name verification remained under review, the requested outcome was to:

- Deploy the merged calendar backend to Tencent Cloud.
- Leave WeChat experience-version upload to the user.
- Bring both repository README files up to date.
- Move the canonical handoff into a project-level `doc` directory.
- Begin keeping one feedback document for every iteration.

The deployment was performed over a VPN-connected network, so slow transfers were treated as expected. Transfer timeouts were followed by remote integrity checks rather than blind retries.

## Deployment changes

- Confirmed all three production containers were healthy before deployment.
- Confirmed sufficient disk, memory, and swap capacity.
- Confirmed `.env.production` retained mode 600 without reading or printing it.
- Uploaded the merged backend source as a compressed archive.
- Preserved the previous backend image as `calorie-calculator-backend:pre-calendar`.
- Built the new backend image on the server.
- Recreated only the backend and Nginx services.
- Left the PostgreSQL container and named data volume running throughout.

## Production verification

Verified on 2026-07-22:

- `/healthz` returned HTTP 200.
- Food pagination reported 1799 total foods and returned 10 items for a 10-item page.
- `/api/records/calendar?month=2026-07` returned:
  - `minDate`: `2026-07-21`
  - `maxDate`: `2026-07-29`
  - one recorded date
- Public `/api/import/json` remained unavailable through Nginx.
- Backend and PostgreSQL reported healthy after deployment.
- PostgreSQL container age remained unchanged, confirming it was not recreated.
- The previous backend image remained available by rollback tag.

## Documentation changes

- Backend README updated with calendar API, date rules, architecture, and deployment status.
- Deployment README updated with calendar verification and rollback guidance.
- Frontend README updated with calendar behavior, current Tencent Cloud environment, tests, and formal-release checklist.
- Canonical handoff moved to `doc/HANDOFF.md`.
- `doc/README.md` and iteration feedback history created.

## Release status

- Backend calendar API: deployed to Tencent Cloud.
- Frontend calendar code: merged and production build previously verified.
- WeChat experience-version upload: intentionally left to the user.
- Formal release: still blocked by domain real-name verification, ICP filing, DNS, HTTPS, and WeChat legal-domain configuration.

## Next actions

1. Wait for domain real-name verification to pass.
2. Submit ICP filing through Tencent Cloud.
3. Configure `api.<domain>`, TLS, and Nginx port 443.
4. Update `.env.production` to the HTTPS API URL.
5. Test without Developer Debugging, then submit the Mini Program for review.

Canonical current state: [`../HANDOFF.md`](../HANDOFF.md).
