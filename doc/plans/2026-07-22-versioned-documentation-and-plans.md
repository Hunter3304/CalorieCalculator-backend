# Versioned Documentation and Iteration Plans

Date: 2026-07-22

## Requested outcome

Store the shared `doc` directory inside both the frontend and backend repositories and add a `doc/plans` directory for future per-iteration plans.

## Scope and decisions

- Copy the existing handoff, documentation index, and iteration history into both repositories.
- Keep the two repository copies synchronized whenever shared documentation changes.
- Keep the original project-level `doc` directory temporarily to avoid deleting user-owned source material during this migration.
- Add links from each repository README to its local documentation index.

## Implementation steps

1. Create one GitHub Issue and Issue-linked branch in each repository.
2. Copy the existing documentation tree into each repository.
3. Add the plans index and this first iteration plan.
4. Replace guidance that says documentation is local-only.
5. Add the completed iteration record and verify both documentation trees match.
6. Commit, open Pull Requests, merge, and clean the branches.

## Verification

- Confirm expected Markdown files exist in both repositories.
- Compare the two `doc` trees recursively.
- Check Markdown links and stale local-only wording.
- Confirm both Git worktrees are clean after merge.

## GitHub records

- Backend Issue #7.
- Frontend Issue #11.