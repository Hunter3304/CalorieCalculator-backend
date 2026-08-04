# CalorieCalculator Documentation

This directory is the versioned project-documentation hub shared by the independent frontend and backend repositories.

An equivalent `doc` directory is stored in each application repository. Shared handoff, iteration, and planning documents must be updated in both repositories in the same iteration so the two copies do not drift.

## Current state

- [`HANDOFF.md`](HANDOFF.md): canonical current engineering, deployment, database, GitHub, and WeChat handoff.
- [`iterations/`](iterations/): one feedback document per recorded iteration.
- [`plans/`](plans/): one implementation plan per iteration, created before implementation begins.

Read `HANDOFF.md` before starting work. Read the latest iteration document when continuing a recently completed feature or deployment, and create the new iteration plan under `plans/` before changing code.

## Iteration history

| Date | Iteration | Feedback document |
| --- | --- | --- |
| 2026-07-20 to 2026-07-21 | Local data, Tencent Cloud deployment, and first experience version | [`2026-07-20-infrastructure-and-first-experience.md`](iterations/2026-07-20-infrastructure-and-first-experience.md) |
| 2026-07-21 | Daily-record persistence and WeChat bundle compatibility | [`2026-07-21-record-persistence-and-wechat-compatibility.md`](iterations/2026-07-21-record-persistence-and-wechat-compatibility.md) |
| 2026-07-21 | Calendar navigation and date-aware records | [`2026-07-21-calendar-system.md`](iterations/2026-07-21-calendar-system.md) |
| 2026-07-22 | Calendar backend production deployment and documentation refresh | [`2026-07-22-calendar-production-deployment.md`](iterations/2026-07-22-calendar-production-deployment.md) |
| 2026-07-22 | Versioned documentation mirrors and iteration plans | [`2026-07-22-versioned-documentation-and-plans.md`](iterations/2026-07-22-versioned-documentation-and-plans.md) |
| 2026-07-22 | Body-weight tracking, trend chart, and production release | [2026-07-22-weight-tracking-and-production-release.md](iterations/2026-07-22-weight-tracking-and-production-release.md) |
| 2026-07-27 | Body-circumference tracking and production release | [2026-07-27-body-circumference-tracking-and-production-release.md](iterations/2026-07-27-body-circumference-tracking-and-production-release.md) |

The older entries were reconstructed from the engineering handoff and linked GitHub Issues/PRs. They summarize recorded feedback without presenting paraphrases as direct quotations.

## Recording future iterations

Before implementation, create a plan under `plans/` using the same date and short slug that will identify the completed iteration. Keep it updated when scope or verification changes.

Create a new Markdown file under `iterations/` for every feature, fix, deployment, or release iteration. Use the date and a short slug in the filename:

```text
yyyy-MM-dd-short-iteration-name.md
```

Each document should include:

1. User feedback or requested outcome.
2. Agreed product rules and scope.
3. Engineering changes and affected repositories.
4. Verification evidence.
5. GitHub Issues, PRs, and Project links.
6. Deployment or release status.
7. Remaining risks and next actions.

After completing an iteration, update every affected repository's root `README.md`, this index, and `HANDOFF.md`.
