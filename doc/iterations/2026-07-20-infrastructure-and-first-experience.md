# Infrastructure and First Experience Version

Date: 2026-07-20 to 2026-07-21

## Feedback and requested outcome

The project needed a persistent local database, complete food data, a remotely reachable backend, and a WeChat experience version that could be tested on a real phone.

During phone testing, the experience version initially showed no food data. The recorded feedback was that enabling Developer Debugging allowed the HTTP IP API to load quickly, confirming that the remaining limitation was WeChat's production network policy rather than backend performance.

## Agreed scope

- Install and configure PostgreSQL locally.
- Import the bundled Chinese food data.
- Deploy PostgreSQL, Spring Boot, and Nginx to Tencent Cloud Lighthouse.
- Keep PostgreSQL and Spring Boot private and expose only Nginx.
- Produce a WeChat experience build while documenting the HTTP-IP limitation.

## Delivered

- PostgreSQL 17.10 installed locally with an automatically started Windows service.
- Database `calorie_calculator` created with 1799 food rows.
- Tencent Cloud Lighthouse deployment created in Shanghai on Ubuntu 24.04.
- Docker Compose stack created with Nginx, Spring Boot, and PostgreSQL 17.
- PostgreSQL data placed in a persistent named volume.
- Public access limited to Nginx port 80; ports 5432 and 8080 remain private.
- `/api/import/*` blocked at Nginx.
- Production database secret stored only in `.env.production` with mode 600.
- Experience-version QR code generated and tested.

## Verification

- `http://124.221.90.240/healthz` returned HTTP 200.
- Food pagination returned data and the production catalog count was 1799.
- Container restart persistence was verified.
- Public ports 5432 and 8080 were not exposed.
- Experience-version requests succeeded on an approved account with Developer Debugging enabled.

## Outcome

The infrastructure and experience-testing path were functional. Formal release remained blocked because a raw HTTP IP cannot be used as the final WeChat request domain.

## Follow-ups

- Complete domain real-name verification and ICP filing.
- Configure an API subdomain and HTTPS.
- Register the HTTPS origin as the WeChat request domain.
- Test without Developer Debugging before formal review.

## References

- Backend deployment files: `Dockerfile`, `docker-compose.production.yml`, and `deploy/README.md`.
- Canonical current state: [`../HANDOFF.md`](../HANDOFF.md).
