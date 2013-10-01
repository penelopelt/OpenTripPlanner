/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.trippattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class TripTimesTest {
    private static final AgencyAndId tripId = new AgencyAndId("agency", "testtrip");

    private static final AgencyAndId stop_a = new AgencyAndId("agency", "A"); // 0
    private static final AgencyAndId stop_b = new AgencyAndId("agency", "B"); // 1
    private static final AgencyAndId stop_c = new AgencyAndId("agency", "C"); // 2
    private static final AgencyAndId stop_d = new AgencyAndId("agency", "D"); // 3
    private static final AgencyAndId stop_e = new AgencyAndId("agency", "E"); // 4
    private static final AgencyAndId stop_f = new AgencyAndId("agency", "F"); // 5
    private static final AgencyAndId stop_g = new AgencyAndId("agency", "G"); // 6
    private static final AgencyAndId stop_h = new AgencyAndId("agency", "H"); // 7

    private static final AgencyAndId[] stops =
        {stop_a, stop_b, stop_c, stop_d, stop_e, stop_f, stop_g, stop_h};

    private static final ServiceDate serviceDate = new ServiceDate();

    private static final TripTimes originalTripTimes;

    static {
        Trip trip = new Trip();
        trip.setId(tripId);

        List<StopTime> stopTimes = new LinkedList<StopTime>();

        for(int i =  0; i < stops.length; ++i) {
            StopTime stopTime = new StopTime();

            Stop stop = new Stop();
            stop.setId(stops[i]);
            stopTime.setStop(stop);
            stopTime.setArrivalTime(i * 60);
            stopTime.setDepartureTime(i * 60);
            stopTime.setStopSequence(i);
            stopTimes.add(stopTime);
        }

        originalTripTimes = new TripTimes(trip, stopTimes);
    }

    @Test
    public void testStopCancellingUpdate() {
        TripUpdateList tripUpdateList;

        List<Update> updates = new LinkedList<Update>();
        updates.add(new Update(tripId, null, 0, 0, 0, Update.Status.PLANNED, 0, serviceDate));
        updates.add(new Update(tripId, null, 1, 0, 0, Update.Status.PLANNED, 0, serviceDate));
        updates.add(new Update(tripId, null, 2, 0, 0, Update.Status.CANCEL , 0, serviceDate));
        updates.add(new Update(tripId, null, 3, 0, 0, Update.Status.CANCEL , 0, serviceDate));

        tripUpdateList = TripUpdateList.forUpdatedTrip(tripId, 0, serviceDate, updates);

        TripTimes updatedTripTimesA = new TripTimes(originalTripTimes);
        updatedTripTimesA.apply(tripUpdateList);

        assertTrue(updatedTripTimesA.timesIncreasing());

        assertEquals(0 * 60            , updatedTripTimesA.getDepartureTime(0));
        assertEquals(1 * 60            , updatedTripTimesA.getDepartureTime(1));
        assertEquals(TripTimes.CANCELED, updatedTripTimesA.getDepartureTime(2));
        assertEquals(TripTimes.CANCELED, updatedTripTimesA.getDepartureTime(3));
        assertEquals(4 * 60            , updatedTripTimesA.getDepartureTime(4));
        assertEquals(5 * 60            , updatedTripTimesA.getDepartureTime(5));
        assertEquals(6 * 60            , updatedTripTimesA.getDepartureTime(6));

        assertEquals(1 * 60            , updatedTripTimesA.getArrivalTime(0));
        assertEquals(TripTimes.CANCELED, updatedTripTimesA.getArrivalTime(1));
        assertEquals(TripTimes.CANCELED, updatedTripTimesA.getArrivalTime(2));
        assertEquals(4 * 60            , updatedTripTimesA.getArrivalTime(3));
        assertEquals(5 * 60            , updatedTripTimesA.getArrivalTime(4));
        assertEquals(6 * 60            , updatedTripTimesA.getArrivalTime(5));
        assertEquals(7 * 60            , updatedTripTimesA.getArrivalTime(6));

        assertEquals( 60, updatedTripTimesA.getRunningTime(0));
        assertEquals(  0, updatedTripTimesA.getRunningTime(1));
        assertEquals(  0, updatedTripTimesA.getRunningTime(2));
        assertEquals(180, updatedTripTimesA.getRunningTime(3));
        assertEquals( 60, updatedTripTimesA.getRunningTime(4));
        assertEquals( 60, updatedTripTimesA.getRunningTime(5));
        assertEquals( 60, updatedTripTimesA.getRunningTime(6));
    }

    @Test
    public void testStopUpdate() {
        TripUpdateList tripUpdateList;

        List<Update> updates = new LinkedList<Update>();
        updates.add(new Update(tripId, null, 3, 190, 190, Update.Status.PREDICTION , 0, serviceDate));
        updates.add(new Update(tripId, null, 5, 311, 312, Update.Status.PREDICTION , 0, serviceDate));

        tripUpdateList = TripUpdateList.forUpdatedTrip(tripId, 0, serviceDate, updates);

        TripTimes updatedTripTimesA = new TripTimes(originalTripTimes);
        updatedTripTimesA.apply(tripUpdateList);

        assertEquals(TripTimes.PASSED, updatedTripTimesA.getDepartureTime(0));
        assertEquals(TripTimes.PASSED, updatedTripTimesA.getDepartureTime(1));
        assertEquals(TripTimes.PASSED, updatedTripTimesA.getDepartureTime(2));
        assertEquals(3 * 60 + 10, updatedTripTimesA.getDepartureTime(3));
        assertEquals(4 * 60 + 10, updatedTripTimesA.getDepartureTime(4));
        assertEquals(5 * 60 + 12, updatedTripTimesA.getDepartureTime(5));
        assertEquals(6 * 60 + 12, updatedTripTimesA.getDepartureTime(6));

        assertEquals(TripTimes.PASSED, updatedTripTimesA.getArrivalTime(0));
        assertEquals(TripTimes.PASSED, updatedTripTimesA.getArrivalTime(1));
        assertEquals(3 * 60 + 10, updatedTripTimesA.getArrivalTime(2));
        assertEquals(4 * 60 + 10, updatedTripTimesA.getArrivalTime(3));
        assertEquals(5 * 60 + 11, updatedTripTimesA.getArrivalTime(4));
        assertEquals(6 * 60 + 12, updatedTripTimesA.getArrivalTime(5));
        assertEquals(7 * 60 + 12, updatedTripTimesA.getArrivalTime(6));
    }

    @Test
    public void testPassedUpdate() {
        TripUpdateList tripUpdateList;

        List<Update> updates = new LinkedList<Update>();
        updates.add(new Update(tripId, null, 0, 0, 0, Update.Status.PASSED, 0, serviceDate));

        tripUpdateList = TripUpdateList.forUpdatedTrip(tripId, 0, serviceDate, updates);

        TripTimes updatedTripTimesA = new TripTimes(originalTripTimes);
        updatedTripTimesA.apply(tripUpdateList);

        assertEquals(TripTimes.PASSED, updatedTripTimesA.getDepartureTime(0));
        assertEquals(60, updatedTripTimesA.getArrivalTime(0));
    }

    @Test
    public void testNonIncreasingUpdate() {
        TripUpdateList tripUpdateList;

        List<Update> updates = new LinkedList<Update>();
        updates.add(new Update(tripId, null, 1, 60, 59, Update.Status.PREDICTION , 0, serviceDate));

        tripUpdateList = TripUpdateList.forUpdatedTrip(tripId, 0, serviceDate, updates);

        TripTimes updatedTripTimesA = new TripTimes(originalTripTimes);
        updatedTripTimesA.apply(tripUpdateList);

        assertFalse(updatedTripTimesA.timesIncreasing());

        updates = new LinkedList<Update>();
        updates.add(new Update(tripId, null, 0, 0, 0, Update.Status.PLANNED, 0, serviceDate));
        updates.add(new Update(tripId, null, 7, 359, 360, Update.Status.PREDICTION , 0, serviceDate));

        tripUpdateList = TripUpdateList.forUpdatedTrip(tripId, 0, serviceDate, updates);

        TripTimes updatedTripTimesB = new TripTimes(originalTripTimes);
        updatedTripTimesB.apply(tripUpdateList);

        assertFalse(updatedTripTimesB.timesIncreasing());
    }

    @Test
    public void testDelay() {
        TripUpdateList tripUpdateList;

        List<Update> updates = new LinkedList<Update>();
        updates.add(new Update(tripId, null, 0, 10, Update.Status.PREDICTION, 0, serviceDate));
        updates.add(new Update(tripId, null, 5, 13, Update.Status.PREDICTION, 0, serviceDate));

        tripUpdateList = TripUpdateList.forUpdatedTrip(tripId, 0, serviceDate, updates);

        TripTimes updatedTripTimesA = new TripTimes(originalTripTimes);
        updatedTripTimesA.apply(tripUpdateList);

        assertEquals(0 * 60 + 10, updatedTripTimesA.getDepartureTime(0));
        assertEquals(1 * 60 + 10, updatedTripTimesA.getDepartureTime(1));
        assertEquals(2 * 60 + 10, updatedTripTimesA.getDepartureTime(2));
        assertEquals(3 * 60 + 10, updatedTripTimesA.getDepartureTime(3));
        assertEquals(4 * 60 + 10, updatedTripTimesA.getDepartureTime(4));
        assertEquals(5 * 60 + 13, updatedTripTimesA.getDepartureTime(5));
        assertEquals(6 * 60 + 13, updatedTripTimesA.getDepartureTime(6));

        assertEquals(1 * 60 + 10, updatedTripTimesA.getArrivalTime(0));
        assertEquals(2 * 60 + 10, updatedTripTimesA.getArrivalTime(1));
        assertEquals(3 * 60 + 10, updatedTripTimesA.getArrivalTime(2));
        assertEquals(4 * 60 + 10, updatedTripTimesA.getArrivalTime(3));
        assertEquals(5 * 60 + 13, updatedTripTimesA.getArrivalTime(4));
        assertEquals(6 * 60 + 13, updatedTripTimesA.getArrivalTime(5));
        assertEquals(7 * 60 + 13, updatedTripTimesA.getArrivalTime(6));
    }

    @Test
    public void testCancel() {
        TripTimes updatedTripTimesA = new TripTimes(originalTripTimes);
        updatedTripTimesA.cancel();

        for (int i = 0; i < stops.length - 1; i++) {
            assertEquals(originalTripTimes.getDepartureTime(i),
                    updatedTripTimesA.getScheduledDepartureTime(i));
            assertEquals(originalTripTimes.getArrivalTime(i),
                    updatedTripTimesA.getScheduledArrivalTime(i));
            assertEquals(TripTimes.CANCELED, updatedTripTimesA.getDepartureTime(i));
            assertEquals(TripTimes.CANCELED, updatedTripTimesA.getArrivalTime(i));
        }
    }

    @Test
    public void testApply() {
        Trip trip = new Trip();
        trip.setId(tripId);

        List<StopTime> stopTimes = new LinkedList<StopTime>();

        StopTime stopTime0 = new StopTime();
        StopTime stopTime1 = new StopTime();
        StopTime stopTime2 = new StopTime();

        Stop stop0 = new Stop();
        Stop stop1 = new Stop();
        Stop stop2 = new Stop();

        stop0.setId(stops[0]);
        stop1.setId(stops[1]);
        stop2.setId(stops[2]);

        stopTime0.setStop(stop0);
        stopTime0.setDepartureTime(0);
        stopTime0.setStopSequence(0);

        stopTime1.setStop(stop1);
        stopTime1.setArrivalTime(30);
        stopTime1.setDepartureTime(60);
        stopTime1.setStopSequence(1);

        stopTime2.setStop(stop2);
        stopTime2.setArrivalTime(90);
        stopTime2.setStopSequence(2);

        stopTimes.add(stopTime0);
        stopTimes.add(stopTime1);
        stopTimes.add(stopTime2);

        TripTimes differingTripTimes = new TripTimes(trip, stopTimes);

        TripUpdateList tripUpdateList;

        List<Update> updates = new LinkedList<Update>();
        updates.add(new Update(tripId, null, 2, 89, 98, Update.Status.PREDICTION , 0, serviceDate));
        updates.add(new Update(tripId, null, 3, 99, 99, Update.Status.PREDICTION , 0, serviceDate));

        tripUpdateList = TripUpdateList.forUpdatedTrip(tripId, 0, serviceDate, updates);

        TripTimes updatedTripTimesA = new TripTimes(differingTripTimes);

        assertFalse(updatedTripTimesA.apply(tripUpdateList));
    }

    @Test
    public void testGetRunningTime() {
        TripUpdateList tripUpdateList;

        List<Update> updates = new LinkedList<Update>();
        updates.add(new Update(tripId, null, 0, 0, 0, Update.Status.CANCEL , 0, serviceDate));

        tripUpdateList = TripUpdateList.forUpdatedTrip(tripId, 0, serviceDate, updates);

        TripTimes updatedTripTimesA = new TripTimes(originalTripTimes);
        updatedTripTimesA.apply(tripUpdateList);

        assertEquals(0, updatedTripTimesA.getRunningTime(0));
    }

    @Test
    public void testGetDwellTime() {
        assertEquals(-1, originalTripTimes.getDwellTime(Integer.MIN_VALUE));
        assertEquals(-1, originalTripTimes.getDwellTime(-1));
        assertEquals(-1, originalTripTimes.getDwellTime(0));
        assertEquals(0, originalTripTimes.getDwellTime(1));
        assertEquals(0, originalTripTimes.getDwellTime(stops.length - 2));
        assertEquals(-1, originalTripTimes.getDwellTime(stops.length - 1));
        assertEquals(-1, originalTripTimes.getDwellTime(stops.length));
        assertEquals(-1, originalTripTimes.getDwellTime(Integer.MAX_VALUE));
    }
}
