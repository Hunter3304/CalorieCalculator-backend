# Production HTTPS plan

Date: 2026-08-04

Issue: #21

## Goal

Serve the public landing page and Mini Program API through filed domains with trusted TLS while preserving the existing private backend/database topology and HTTP-IP rollback path.

## Work

1. Add root, www, and API virtual hosts with TLS 1.2/1.3.
2. Publish container port 443 and mount certificates from /etc/calorie-calculator/tls without committing private keys.
3. Serve a minimal filed-domain landing page and retain the ICP link.
4. Preserve /healthz, API proxying, and /api/import/* blocking.
5. Validate configuration before recreating only the Nginx container.
6. Verify certificate chains, redirects, API behavior, and private ports.

## Safety

- Never commit, print, or copy private-key contents into logs.
- Keep PostgreSQL and backend containers unchanged.
- Validate the new Nginx configuration before replacing the running container.
- Preserve raw-IP HTTP temporarily as a rollback and experience-version path.
