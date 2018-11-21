package test.java.api;

import org.junit.Test;

import main.java.api.SaveAttendanceRequest;

public class SaveAttendanceRequestTest {

	@Test
	public void testIsValid() {
		SaveAttendanceRequest request = new SaveAttendanceRequest();
		request.setBarcode("123456789");
		request.setYear(2017);
		request.setMonth(1);
		request.setDate(1);
		request.setHour(1);
		request.setMinute(1);
		
		assert(request.isValid());
	}

}
