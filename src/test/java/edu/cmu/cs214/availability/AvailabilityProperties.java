package edu.cmu.cs214.availability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Property-based tests for {@link AvailabilityCalculator}.
 *
 * <p>One example property is provided below: it checks that no returned free slot
 * overlaps a booking, and it passes. In Milestone 1 you add a stronger property
 * that pins down what "correct availability" actually means. See the lab handout.
 */
class AvailabilityProperties {

    private final AvailabilityCalculator calc = new AvailabilityCalculator();

    /** Provided example: every returned free slot is genuinely free (overlaps no booking). */
    @Property
    void freeSlotsNeverOverlapABooking(@ForAll("scenarios") Scenario s) {
        List<TimeInterval> free = calc.freeSlots(s.dayStart(), s.dayEnd(), s.bookings());
        for (TimeInterval slot : free) {
            for (TimeInterval booking : s.bookings()) {
                assertFalse(slot.overlaps(booking),
                    () -> "free slot " + slot + " overlaps booking " + booking);
            }
        }
    }

    /**
     * Milestone 1: every minute of the business day is either booked or free,
     * never both and never neither.
     */
    @Property
    void everyMinuteIsBookedXorFree(@ForAll("scenarios") Scenario s) {
        List<TimeInterval> free = calc.freeSlots(s.dayStart(), s.dayEnd(), s.bookings());
        for (int minute = s.dayStart(); minute < s.dayEnd(); minute++) {
            boolean booked = isCovered(minute, s.bookings());
            boolean freeMinute = isCovered(minute, free);
            int m = minute;
            assertTrue(booked != freeMinute,
                () -> "minute " + m + " is " + (booked ? "booked" : "not booked")
                    + " and " + (freeMinute ? "free" : "not free"));
        }
    }

    /** Is {@code minute} contained in any of the given half-open intervals? */
    private static boolean isCovered(int minute, List<TimeInterval> intervals) {
        for (TimeInterval interval : intervals) {
            if (interval.start() <= minute && minute < interval.end()) {
                return true;
            }
        }
        return false;
    }

    /** Generates a business day plus a list of bookings (possibly unsorted, overlapping, or outside hours). */
    @Provide
    Arbitrary<Scenario> scenarios() {
        Arbitrary<Integer> minutes = Arbitraries.integers().between(0, 1440);
        Arbitrary<TimeInterval> intervals = Combinators.combine(minutes, minutes)
            .as((a, b) -> new TimeInterval(Math.min(a, b), Math.max(a, b) + 1));
        Arbitrary<List<TimeInterval>> bookings = intervals.list().ofMaxSize(6);
        return Combinators.combine(minutes, minutes, bookings)
            .as((a, b, bk) -> new Scenario(Math.min(a, b), Math.max(a, b) + 1, bk));
    }

    record Scenario(int dayStart, int dayEnd, List<TimeInterval> bookings) {
    }
}
