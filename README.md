# Lab 2 Starter: Availability Calculator

A small reservation component. Given a room's bookings and the day's business hours,
`AvailabilityCalculator.freeSlots` computes when the room is free. It is the code you
work in for Lab 2.

It ships with a generated test suite that passes, and a property-based test harness
(jqwik) with one example property. Everything is green. Your job in Lab 2 is to decide
whether green actually means correct.

**Read `ARCHITECTURE.md` before the code.**

## Build and test

```
mvn test
```

`mvn test` runs both files, the ordinary example-based tests (`AvailabilityCalculatorTest`)
and the property-based tests (`AvailabilityProperties`). A code-coverage report is written
to `target/site/jacoco/index.html`.

## Continuous integration

This repository has CI configured in `.github/workflows/ci.yml`. GitHub disables workflows on a
fresh fork, so enable them once on your fork (the handout shows where). After that, every
push runs `mvn test`. You will watch the gate go red when your new property finds the bug, then
green once you fix it.

## Milestone 3: Generated Suite Audit

`AvailabilityCalculatorTest.java` stayed green even though `freeSlots` had a real bug
(it dropped the trailing free gap after the last booking, or the whole day when there
were no bookings). Three weaknesses in that generated suite explain why:

1. **No empty-bookings test — controllability gap.** No test ever calls `freeSlots`
   with `bookings = []`, so the exact input that triggers the bug (an empty booking
   list) is never supplied.
2. **The first five tests always book through to `DAY_END` — controllability gap.**
   `fullyBookedDayHasNoFreeSlots`, `bookingUntilEndOfDayLeavesTheMorningFree`,
   `gapsBetweenBookingsAreReturned`, `unsortedBookingsAreHandled`, and
   `overlappingBookingsAreMerged` all use a final booking that extends to `DAY_END`,
   so none of them ever leaves a trailing gap for the buggy code path to drop.
3. **`returnedSlotsNeverOverlapABooking` does create a trailing gap, but only checks
   half the property — observability gap.** Its booking ends before `DAY_END`, so the
   bug is exercised, but the assertion only checks that returned slots don't overlap
   a booking. It never checks that all free time was actually returned, so a result
   missing the trailing slot still passes.

High JaCoCo coverage did not catch the bug either: coverage shows that the code that
*exists* was executed, not that the suite supplied all important inputs or observed
all required behavior. The missing trailing-gap statement wasn't in the source before
the fix, so there was no line for coverage to flag as unexecuted.

## Where things are

- Component: `src/main/java/edu/cmu/cs214/availability/`
- Example-based tests: `src/test/java/edu/cmu/cs214/availability/AvailabilityCalculatorTest.java`
- Property-based tests: `src/test/java/edu/cmu/cs214/availability/AvailabilityProperties.java`
- Setup: `SETUP.md`

See the Lab 2 handout on the course page for the three milestones you show a TA.

## Tools and models used

Claude Sonnet 5 (Claude Code) and ChatGPT (GPT-5.6 Sol).
